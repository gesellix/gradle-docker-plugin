package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerCleanupTask extends GenericDockerTask {

  @Input
  @Optional
  def shouldKeepContainer

  @Input
  @Optional
  def shouldKeepVolume = { volume -> true }

  DockerCleanupTask() {
    description = "Removes stopped containers, dangling images, and dangling volumes"
    group = "Docker"
  }

  @TaskAction
  def cleanup() {
    logger.info "docker cleanup"
    def keepContainer = getShouldKeepContainer() ?: { container -> false }
    def keepVolume = getShouldKeepVolume() ?: { volume -> true }
    dockerClient.cleanupStorage keepContainer, keepVolume
  }
}
