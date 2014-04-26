package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.client.DockerClientImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerDeployTask extends DefaultTask {

  private static Logger logger = LoggerFactory.getLogger(DockerDeployTask)

  def dockerClient

  @Input
  def imageName

  DockerDeployTask() {
    dockerClient = new DockerClientImpl("172.17.42.1", 4243)
  }

  @TaskAction
  def deploy() {
    logger.info "running deploy..."
    dockerClient.pull(imageName)
  }
}
