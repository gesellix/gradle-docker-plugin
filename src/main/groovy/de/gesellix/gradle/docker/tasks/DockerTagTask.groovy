package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerTagTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerTagTask)

  @Input
  def imageId
  @Input
  def tag
  @Input
  def force = false

  DockerTagTask() {
    description = "tag an image into a repository"
    group = "Docker"
  }

  @TaskAction
  def tag() {
    logger.info "running tag..."
    return getDockerClient().tag(getImageId(), getTag(), getForce())
  }
}
