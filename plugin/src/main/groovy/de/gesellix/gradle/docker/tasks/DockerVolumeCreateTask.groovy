package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerVolumeCreateTask extends GenericDockerTask {

  @Input
  @Optional
  MapProperty<String, Object> volumeConfig

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getVolumeConfig()
   */
  @Deprecated
  void setVolumeConfig(Map<String, Object> volumeConfig) {
    this.volumeConfig.set(volumeConfig)
  }

  @Internal
  def response

  @Inject
  DockerVolumeCreateTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Create a volume"

    volumeConfig = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def createVolume() {
    logger.info "docker volume create"

    response = getDockerClient().createVolume(new HashMap<>(getVolumeConfig().get()))
    return response
  }
}
