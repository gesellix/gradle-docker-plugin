package de.gesellix.gradle.docker.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPublishTask extends AbstractDockerTask {

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

  def imageNameWithTag

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
    configureResult.getTaskDependencies().values.each { parentTaskDependency ->
      if (buildImageTask != parentTaskDependency) {
        buildImageTask.mustRunAfter parentTaskDependency
      }
    }
    configureResult.dependsOn buildImageTask

    getTargetRegistries().each { name, targetRegistry ->
      def pushTask = project.task(["type": DockerPushTask], "pushImageTo${name.capitalize()}Internal") {
        repositoryName = imageNameWithTag
        registry = targetRegistry
        dependsOn buildImageTask
      }
      configureResult.dependsOn pushTask

      def rmiTask = project.task(["type": DockerRmiTask], "rmi${name.capitalize()}Image") {
        imageId = "${targetRegistry}/${imageNameWithTag}"
      }
      pushTask.finalizedBy rmiTask
    }

    return configureResult
  }

  private def configureBuildImageTask() {
    def buildImageTaskName = "buildImageFor${this.name.capitalize()}"

    def existingBuildTasks = project.tasks.findAll {
      it instanceof DockerBuildTask && it.name == buildImageTaskName
    }
    if (existingBuildTasks.size() > 1) {
      throw new IllegalStateException("unique build task expected, found ${existingBuildTasks.size()}.")
    }

    def buildImageTask

    if (existingBuildTasks) {
      buildImageTask = existingBuildTasks.first()
    }
    else {
      buildImageTask = project.task(["type": DockerBuildTask, "overwrite": true], buildImageTaskName)
    }

    def buildImageConfiguration = {
      imageName = imageNameWithTag
      buildContext = this.getBuildContext()
      buildContextDirectory = this.getBuildContextDirectory()
    }
    buildImageTask.configure(buildImageConfiguration)
    return buildImageTask
  }

  @TaskAction
  def publish() {
    logger.info "running publish..."
  }
}
