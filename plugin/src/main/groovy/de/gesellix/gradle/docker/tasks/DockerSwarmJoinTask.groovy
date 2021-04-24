package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerSwarmJoinTask extends GenericDockerTask {

  @Input
  @Optional
  MapProperty<String, Object> config

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getConfig()
   */
  @Deprecated
  void setConfig(Map<String, Object> config) {
    this.config.set(config)
  }

  @Internal
  def response

  @Inject
  DockerSwarmJoinTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Join a swarm as a node and/or manager"

    config = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def joinSwarm() {
    logger.info "docker swarm join"

    response = getDockerClient().joinSwarm(new HashMap(getConfig().get()))
    return response
  }
}
