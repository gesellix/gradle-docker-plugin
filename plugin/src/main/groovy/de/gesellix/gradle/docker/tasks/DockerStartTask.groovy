package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerStartTask extends GenericDockerTask {

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
  DockerStartTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Start a stopped container"

    containerId = objectFactory.property(String)
  }

  @TaskAction
  def start() {
    logger.info "docker start"
    result = getDockerClient().startContainer(getContainerId().get())
    return result
  }
}
