package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerSwarmLeaveTask extends GenericDockerTask {

  @Input
  @Optional
  MapProperty<String, Object> query

  /**
   * @deprecated This setter will be removed, please use the Property<> instead.
   * @see #getQuery()
   */
  @Deprecated
  void setQuery(Map<String, Object> query) {
    this.query.set(query)
  }

  @Internal
  def response

  @Inject
  DockerSwarmLeaveTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Leave the swarm"

    query = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def leaveSwarm() {
    logger.info "docker swarm leave"

    response = getDockerClient().leaveSwarm(new HashMap<>(getQuery().get()))
    return response
  }
}
