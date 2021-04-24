package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerServiceRmTask extends GenericDockerTask {

  @Input
  Property<String> serviceName

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getServiceName()
   */
  @Deprecated
  void setServiceName(String serviceName) {
    this.serviceName.set(serviceName)
  }

  @Internal
  def response

  @Inject
  DockerServiceRmTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Remove a service"

    serviceName = objectFactory.property(String)
  }

  @TaskAction
  def rmService() {
    logger.info "docker service rm"
    response = getDockerClient().rmService(getServiceName().get())
  }
}
