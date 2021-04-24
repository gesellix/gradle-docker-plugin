package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerPingTask extends GenericDockerTask {

  @Internal
  def result

  @Inject
  DockerPingTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "Ping the docker server"
  }

  @TaskAction
  def ping() {
    logger.info "docker ping"
    result = getDockerClient().ping()
  }
}
