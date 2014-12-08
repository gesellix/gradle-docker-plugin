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

  @Input
  def targetRegistries

  DockerPublishTask() {
    description = "builds and publishes an image"
    group = "Docker"
  }

  @Override
  Task configure(Closure closure) {
    def configureResult = super.configure(closure)
    def newRepositoryName = "${configureResult.getImageName()}:${configureResult.getImageTag()}"
    def buildImageTask = project.task(["type": DockerBuildTask, "overwrite": true], "buildImageInternal") {
      imageName = newRepositoryName
      buildContext = configureResult.getBuildContext()
      buildContextDirectory = configureResult.getBuildContextDirectory()
    }
    getTargetRegistries().each { name, targetRegistry ->
      def pushTask = project.task(["type": DockerPushTask], "pushImageTo${name.capitalize()}Internal") {
        repositoryName = newRepositoryName
        registry = targetRegistry
        dependsOn buildImageTask
      }
      configureResult.dependsOn pushTask

      def rmiTask = project.task(["type": DockerRmiTask], "rmi${name.capitalize()}Image") {
        imageId = "${targetRegistry}/${newRepositoryName}"
      }
      pushTask.finalizedBy rmiTask
    }
    return configureResult
  }

  @TaskAction
  def publish() {
    logger.info "running publish..."
  }
}
