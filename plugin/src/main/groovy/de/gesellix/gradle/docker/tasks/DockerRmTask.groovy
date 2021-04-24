package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerRmTask extends GenericDockerTask {

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

  @Input
  @Optional
  Property<Boolean> removeVolumes

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getRemoveVolumes()
   */
  @Deprecated
  void setRemoveVolumes(boolean removeVolumes) {
    this.removeVolumes.set(removeVolumes)
  }

  @Internal
  def result

  @Inject
  DockerRmTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Remove one or more containers"

    containerId = objectFactory.property(String)
    removeVolumes = objectFactory.property(Boolean)
    removeVolumes.convention(false)
  }

  @TaskAction
  def rm() {
    logger.info "docker rm"
    result = getDockerClient().rm(getContainerId().get(), ["v": getRemoveVolumes().get() ? 1 : 0])
    return result
  }
}
