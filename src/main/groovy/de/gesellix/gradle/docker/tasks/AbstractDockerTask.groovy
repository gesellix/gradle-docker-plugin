package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class AbstractDockerTask extends DefaultTask {

  @Input
  @Optional
  def dockerHost

  DockerClient dockerClient

  def getDockerClient() {
    if (!dockerClient) {
      if (getDockerHost()) {
        dockerClient = new DockerClientImpl(dockerHost: getDockerHost())
      }
      else {
        dockerClient = new DockerClientImpl()
      }
    }
    dockerClient
  }
}
