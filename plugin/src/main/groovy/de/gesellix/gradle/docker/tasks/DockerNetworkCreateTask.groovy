package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerNetworkCreateTask extends GenericDockerTask {

  @Input
  Property<String> networkName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getNetworkName()
   */
  @Deprecated
  void setNetworkName(String networkName) {
    this.networkName.set(networkName)
  }

  @Input
  MapProperty<String, Object> networkConfig

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getNetworkConfig()
   */
  @Deprecated
  void setNetworkConfig(Map<String, Object> networkConfig) {
    this.networkConfig.set(networkConfig)
  }

  @Internal
  def response

  @Inject
  DockerNetworkCreateTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Create a new network"

    networkName = objectFactory.property(String)
    networkConfig = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def createNetwork() {
    logger.info "docker network create"
    response = getDockerClient().createNetwork(getNetworkName().get(), new HashMap<>(getNetworkConfig().getOrElse([:])))
  }
}
