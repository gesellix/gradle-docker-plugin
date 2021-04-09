package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerTagTask extends GenericDockerTask {

  @Input
  def imageId
  @Input
  def tag

  DockerTagTask() {
    description = "Tag an image into a repository"
    group = "Docker"
  }

  @TaskAction
  def tag() {
    logger.info "docker tag"
    return getDockerClient().tag(getImageId(), getTag())
  }
}
