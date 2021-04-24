package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerVolumesTask extends GenericDockerTask {

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
  def volumes

  @Inject
  DockerVolumesTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "List volumes from all volume drivers"

    query = objectFactory.mapProperty(String, Object)
  }

  @TaskAction
  def volumes() {
    logger.info "docker volume ls"
    volumes = getDockerClient().volumes(new HashMap<>(getQuery().get()))
  }
}
