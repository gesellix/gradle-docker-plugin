package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRunTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerRunTask)

  @Input
  def imageName
  @Input
  @Optional
  def tag
  @Input
  @Optional
  def containerName
  @Input
  @Optional
  def containerConfiguration

  @TaskAction
  def run() {
    logger.info "running run..."
    getDockerClient().run(getContainerConfiguration() ?: [:], getImageName(), getTag(), getContainerName())
  }
}
