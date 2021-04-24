package de.gesellix.gradle.docker.tasks

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class DockerImagesTask extends GenericDockerTask {

  @Internal
  def images

  @Inject
  DockerImagesTask(ObjectFactory objectFactory) {
    super(objectFactory)
    description = "List images"
  }

  @TaskAction
  def images() {
    logger.info "docker images"
    images = getDockerClient().images()
  }
}
