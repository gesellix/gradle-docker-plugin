package de.gesellix.gradle.docker.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPublishTask extends DockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRmiTask)

  @Input
  @Optional
  def buildContext
  @InputDirectory
  @Optional
  File buildContextDirectory

  @Input
  def imageName
  @Input
  @Optional
  def imageTag

  String imageNameWithTag

  @Input
  @Optional
  def targetRegistries

  DockerPublishTask() {
    description = "builds and publishes an image"
    group = "Docker"
  }

  @Override
  Task configure(Closure closure) {
    def configureResult = super.configure(closure)

    imageNameWithTag = "${configureResult.getImageName()}"
    if (configureResult.getImageTag()) {
      imageNameWithTag += ":${configureResult.getImageTag()}"
    }

    def buildImageTask = configureBuildImageTask()
    configureResult.getDependsOn().each { parentTaskDependency ->
      if (buildImageTask != parentTaskDependency) {
        buildImageTask.mustRunAfter parentTaskDependency
      }
    }
    configureResult.dependsOn buildImageTask

    getTargetRegistries().each { name, targetRegistry ->
      def pushTask = createUniquePushTask(name, targetRegistry, imageNameWithTag, getAuthConfig())
      pushTask.dependsOn buildImageTask
      configureResult.dependsOn pushTask
    }

    return configureResult
  }

  def createUniquePushTask(name, targetRegistry, imageNameWithTag, authConfig) {
    def pushTask = createUniqueTask(DockerPushTask, "pushImageTo${name.capitalize()}Internal").configure {
      repositoryName = imageNameWithTag
      registry = targetRegistry
      authConfigEncoded = authConfig
    }
    def rmiTask = createUniqueTask(DockerRmiTask, "rmi${name.capitalize()}Image").configure {
      imageId = "${targetRegistry}/${imageNameWithTag}"
    }
    pushTask.finalizedBy rmiTask
  }

  private def configureBuildImageTask() {
    def buildImageTaskName = "buildImageFor${this.name.capitalize()}"
    def buildImageTask = createUniqueTask(DockerBuildTask, buildImageTaskName).configure {
      imageName = imageNameWithTag
      buildContext = this.getBuildContext()
      buildContextDirectory = this.getBuildContextDirectory()
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
