package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerVolumeRmTask extends GenericDockerTask {

  @Input
  def volumeName

  @Internal
  def response

  DockerVolumeRmTask() {
    description = "Remove a volume"
    group = "Docker"
  }

  @TaskAction
  def rmVolume() {
    logger.info "docker volume rm"

    response = getDockerClient().rmVolume(getVolumeName())
    return response
  }
}
