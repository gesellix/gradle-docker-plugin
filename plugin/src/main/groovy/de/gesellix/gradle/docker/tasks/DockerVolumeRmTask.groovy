package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerVolumeRmTask extends GenericDockerTask {

  @Input
  Property<String> volumeName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getVolumeName()
   */
  @Deprecated
  void setVolumeName(String volumeName) {
    this.volumeName.set(volumeName)
  }

  @Internal
  def response

  @Inject
  DockerVolumeRmTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Remove a volume"

    volumeName = objectFactory.property(String)
  }

  @TaskAction
  def rmVolume() {
    logger.info "docker volume rm"

    response = getDockerClient().rmVolume(getVolumeName().get())
    return response
  }
}
