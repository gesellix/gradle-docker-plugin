package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerSwarmInitTask extends GenericDockerTask {

  /**
   * @see #swarmconfig
   * @param config
   * @deprecated use #swarmconfig
   */
  @Deprecated
  void setConfig(def config) {
    this.swarmconfig = config
  }

  @Input
  def swarmconfig = [:]

  @Internal
  def response

  @TaskAction
  def initSwarm() {
    logger.info "docker swarm init"

    response = getDockerClient().initSwarm(getSwarmconfig())
    return response
  }
}
