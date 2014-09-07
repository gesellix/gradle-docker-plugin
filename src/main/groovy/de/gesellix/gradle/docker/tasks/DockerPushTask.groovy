package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPushTask extends AbstractDockerTask {

  private static Logger logger = LoggerFactory.getLogger(DockerPushTask)

  @Input
  def repositoryName
  @Input
  @Optional
  def registry

  DockerPushTask() {
    description = "pushes a repository to a registry"
    group = "Docker"
  }

  @TaskAction
  def push() {
    logger.info "running push..."
    dockerClient.push(getRepositoryName(), getAuthConfig(), getRegistry())
  }
}
