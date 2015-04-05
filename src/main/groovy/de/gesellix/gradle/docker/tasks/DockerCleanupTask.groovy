package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerCleanupTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerCleanupTask)

  @Input
  @Optional
  def shouldKeepContainer = { container -> false }

  DockerCleanupTask() {
    description = "Removes stopped containers and dangling images"
    group = "Docker"
  }

  @TaskAction
  def cleanup() {
    logger.info "docker cleanup"
    def allContainers = dockerClient.ps([filters: [status: "exited"]]).content
    allContainers.findAll { Map container ->
      !getShouldKeepContainer().call(container)
    }.each { container ->
      logger.info "docker rm ${container.Id} (${container.Names.first()})"
      dockerClient.rm(container.Id)
    }

    dockerClient.images([filters: [dangling: true]]).content.each { image ->
      logger.info "docker rmi ${image.Id}"
      dockerClient.rmi(image.Id)
    }
  }
}
