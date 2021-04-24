package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerSwarmInitTask extends GenericDockerTask {

  @Input
  MapProperty<String, Object> swarmconfig

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getSwarmconfig()
   */
  @Deprecated
  void setSwarmconfig(Map<String, Object> swarmconfig) {
    this.swarmconfig.set(swarmconfig)
  }

  @Internal
  def response

  @Inject
  DockerSwarmInitTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Initialize a swarm"

    swarmconfig = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def initSwarm() {
    logger.info "docker swarm init"

    response = getDockerClient().initSwarm(new HashMap(getSwarmconfig().get()))
    return response
  }
}
