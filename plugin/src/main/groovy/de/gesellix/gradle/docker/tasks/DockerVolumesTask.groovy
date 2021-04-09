package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerVolumesTask extends GenericDockerTask {

  @Input
  @Optional
  def query = [:]

  @Internal
  def volumes

  DockerVolumesTask() {
    description = "List volumes from all volume drivers"
    group = "Docker"
  }

  @TaskAction
  def volumes() {
    logger.info "docker volume ls"
    volumes = getDockerClient().volumes(getQuery() ?: [:])
  }
}
