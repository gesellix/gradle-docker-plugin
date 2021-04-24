package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClientException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerDisposeContainerTask extends GenericDockerTask {

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
  Property<Boolean> rmiParentImage

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getRmiParentImage()
   */
  @Deprecated
  void setRmiParentImage(boolean rmiParentImage) {
    this.rmiParentImage.set(rmiParentImage)
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

  @Inject
  DockerDisposeContainerTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Stops and removes a container and optionally its parent image"

    containerId = objectFactory.property(String)
    rmiParentImage = objectFactory.property(Boolean)
    rmiParentImage.convention(false)
    removeVolumes = objectFactory.property(Boolean)
    removeVolumes.convention(false)
  }

  @TaskAction
  def dispose() {
    logger.info "docker dispose"

    String containerId = getContainerId().get()
    def containerDetails
    try {
      containerDetails = getDockerClient().inspectContainer(containerId)
    }
    catch (DockerClientException e) {
      if (e.detail?.status?.code == 404) {
        logger.info("couldn't dispose container because it doesn't exists")
        return
      }
      else {
        throw e
      }
    }
    getDockerClient().stop(containerId)
    getDockerClient().wait(containerId)
    getDockerClient().rm(containerId, ["v": getRemoveVolumes().getOrElse(false) ? 1 : 0])
    if (getRmiParentImage().getOrElse(false)) {
      getDockerClient().rmi(containerDetails.content.Image as String)
    }
  }
}
