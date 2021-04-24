package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerWaitTask extends GenericDockerTask {

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
  DockerWaitTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Block until a container stops, then print its exit code."

    containerId = objectFactory.property(String)
  }

  @TaskAction
  def awaitStop() {
    logger.info "docker wait"
    result = getDockerClient().wait(getContainerId().get())
    return result
  }
}
