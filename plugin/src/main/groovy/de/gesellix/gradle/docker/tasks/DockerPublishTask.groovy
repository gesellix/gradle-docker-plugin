package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.authentication.AuthConfig
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPublishTask extends GenericDockerTask {

  @InputDirectory
  @Optional
  DirectoryProperty buildContextDirectory

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContextDirectory()
   */
  @Deprecated
  void setBuildContextDirectory(File buildContextDirectory) {
    this.buildContextDirectory.set(buildContextDirectory)
  }

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContextDirectory()
   */
  @Deprecated
  void setBuildContextDirectory(String buildContextDirectory) {
    this.buildContextDirectory.set(project.file(buildContextDirectory))
  }

  @Input
  @Optional
  Property<InputStream> buildContext

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getBuildContext()
   */
  @Deprecated
  void setBuildContext(InputStream buildContext) {
    this.buildContext.set(buildContext)
  }

  @Input
  Property<String> imageName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageName()
   */
  @Deprecated
  void setImageName(String imageName) {
    this.imageName.set(imageName)
  }

  @Input
  @Optional
  Property<String> imageTag

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getImageTag()
   */
  @Deprecated
  void setImageTag(String imageTag) {
    this.imageTag.set(imageTag)
  }

  @Input
  @Optional
  MapProperty<String, String> targetRegistries

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getTargetRegistries()
   */
  @Deprecated
  void setTargetRegistries(Map<String, String> targetRegistries) {
    this.targetRegistries.set(targetRegistries)
  }

  private Property<String> imageNameWithTag

  @Internal
  Property<String> getImageNameWithTag() {
    return imageNameWithTag
  }

  @Inject
  DockerPublishTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "builds and publishes an image"

    buildContextDirectory = objectFactory.directoryProperty()
    buildContext = objectFactory.property(InputStream)
    imageName = objectFactory.property(String)
    imageTag = objectFactory.property(String)
    imageNameWithTag = objectFactory.property(String)
    targetRegistries = objectFactory.mapProperty(String, String)
  }

  @Override
  Task configure(Closure closure) {
    def configuredTask = super.configure(closure)

    imageNameWithTag.set("${configuredTask.getImageName().get()}")
    if (configuredTask.getImageTag().isPresent()) {
      imageNameWithTag.set("${configuredTask.getImageName().get()}:${configuredTask.getImageTag().get()}")
    }

    def buildImageTask = configureBuildImageTask()
    configuredTask.getDependsOn().each { parentTaskDependency ->
      if (buildImageTask != parentTaskDependency) {
        buildImageTask.mustRunAfter parentTaskDependency
      }
    }
    configuredTask.dependsOn buildImageTask

    if ((getTargetRegistries().getOrElse([:])).isEmpty()) {
      logger.warn("No targetRegistries configured, image won't be pushed to any registry.")
    }
    getTargetRegistries().get().each { String registryName, String targetRegistry ->
      def pushTask = createUniquePushTask(registryName, targetRegistry, imageNameWithTag, authConfig)
      pushTask.dependsOn buildImageTask
      configuredTask.dependsOn pushTask
    }

    return configuredTask
  }

  Task createUniquePushTask(String registryName, String targetRegistry, Property<String> imageNameWithTag, Property<AuthConfig> authConfig) {
    def pushTask = createUniqueTask(DockerPushTask, "pushImageTo${registryName.capitalize()}For${this.name.capitalize()}").configure { DockerPushTask t ->
      t.repositoryName.set(imageNameWithTag)
      t.registry.set(targetRegistry)
      t.authConfig = authConfig
    }
    def rmiTask = createUniqueTask(DockerRmiTask, "rmi${registryName.capitalize()}ImageFor${this.name.capitalize()}").configure { DockerRmiTask t ->
      t.imageId.set("${targetRegistry}/${imageNameWithTag.get()}")
    }
    pushTask.finalizedBy rmiTask
    return pushTask
  }

  private def configureBuildImageTask() {
    def buildImageTaskName = "buildImageFor${this.name.capitalize()}"
    def buildImageTask = createUniqueTask(DockerBuildTask, buildImageTaskName).configure { DockerBuildTask t ->
      t.imageName.set(this.getImageNameWithTag())
      t.buildContext.set(this.getBuildContext())
      t.buildContextDirectory.set(this.getBuildContextDirectory())
    }
    return buildImageTask
  }

  def createUniqueTask(taskType, String name) {
    def existingTasks = project.tasks.findAll {
      taskType.isInstance(it) && it.name == name
    }
    if (existingTasks.size() > 1) {
      throw new IllegalStateException("unique task expected, found ${existingTasks.size()}.")
    }

    if (existingTasks) {
      return existingTasks.first()
    }
    else {
      return project.task([type: taskType, group: getGroup()], name)
    }
  }

  @TaskAction
  def publish() {
    logger.info "running publish..."
  }
}
