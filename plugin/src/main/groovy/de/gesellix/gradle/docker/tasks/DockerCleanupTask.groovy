package de.gesellix.gradle.docker.tasks

import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * @deprecated this task will be removed in a future release.
 */
@Deprecated
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

  @Override
  Task configure(Closure closure) {
    logger.warn("This task is deprecated and will be removed in a future release.")
    return super.configure(closure)
  }

  @TaskAction
  void cleanup() {
    logger.info("docker cleanup")
    def keepContainer = getShouldKeepContainer() ?: { container -> false }
    def keepVolume = getShouldKeepVolume() ?: { volume -> true }
    dockerClient.cleanupStorage keepContainer, keepVolume
  }
}
