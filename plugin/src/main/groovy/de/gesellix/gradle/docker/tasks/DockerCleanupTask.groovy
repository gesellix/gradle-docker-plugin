package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerCleanupTask extends GenericDockerTask {

  @Input
  @Optional
  def shouldKeepContainer

  @Input
  @Optional
  def shouldKeepVolume = { volume -> true }

  @Inject
  DockerCleanupTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Removes stopped containers, dangling images, and dangling volumes"
  }

  @TaskAction
  def cleanup() {
    logger.info "docker cleanup"
    def keepContainer = getShouldKeepContainer() ?: { container -> false }
    def keepVolume = getShouldKeepVolume() ?: { volume -> true }
    dockerClient.cleanupStorage keepContainer, keepVolume
  }
}
