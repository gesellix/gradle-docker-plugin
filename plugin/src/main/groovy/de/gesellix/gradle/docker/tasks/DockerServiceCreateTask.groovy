package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerServiceCreateTask extends GenericDockerTask {

  @Input
  MapProperty<String, Object> serviceConfig

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getServiceConfig()
   */
  @Deprecated
  void setServiceConfig(Map<String, Object> serviceConfig) {
    this.serviceConfig.set(serviceConfig)
  }

  @Internal
  def response

  @Inject
  DockerServiceCreateTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Create a service"

    serviceConfig = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def createService() {
    logger.info "docker service create"
    response = getDockerClient().createService(new HashMap(getServiceConfig().get()))
  }
}
