package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerInspectContainerTask extends GenericDockerTask {

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
  def containerInfo

  @Inject
  DockerInspectContainerTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Return low-level information on a container"

    containerId = objectFactory.property(String)
  }

  @TaskAction
  def inspect() {
    logger.info "docker inspect"

    containerInfo = getDockerClient().inspectContainer(getContainerId().get())
  }
}
