package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerKillTask extends GenericDockerTask {

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
  DockerKillTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Kill a running container"

    containerId = objectFactory.property(String)
  }

  @TaskAction
  def kill() {
    logger.info "docker kill"
    result = getDockerClient().kill(getContainerId().get())
    return result
  }
}
