package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPauseTask extends GenericDockerTask {

  @Input
  Property<String> containerId

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getContainerId()
   */
  @Deprecated
  void setContainerId(String containerId) {
    this.containerId.set(containerId)
  }

  @Internal
  def result

  @Inject
  DockerPauseTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Pause a running container"

    containerId = objectFactory.property(String)
  }

  @TaskAction
  def pause() {
    logger.info "docker pause"
    result = getDockerClient().pause(getContainerId().get())
    return result
  }
}
