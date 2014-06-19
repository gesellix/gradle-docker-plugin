package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerBuildTask extends DefaultTask {

  private static Logger logger = LoggerFactory.getLogger(DockerBuildTask)

  DockerClient dockerClient

  @Input
  @Optional
  def imageName
  @InputFile
  def buildContext

  DockerBuildTask() {
    dockerClient = new DockerClientImpl("172.17.42.1", 4243)
  }

  @TaskAction
  def build() {
    logger.info "running build..."
    def imageId = dockerClient.build(new FileInputStream(getBuildContext()))
    dockerClient.tag(imageId, getImageName())
    return imageId
  }
}
