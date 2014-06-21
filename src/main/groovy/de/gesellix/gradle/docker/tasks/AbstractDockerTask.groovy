package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClientImpl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class AbstractDockerTask extends DefaultTask {

  @Input
  @Optional
  def dockerHostname
  @Input
  @Optional
  def dockerPort

  def dockerClient

  def getDockerClient() {
    if (!dockerClient) {
      if (getDockerHostname()) {
        if (getDockerPort()) {
          dockerClient = new DockerClientImpl(getDockerHostname(), getDockerPort())
        }
        dockerClient = new DockerClientImpl(getDockerHostname())
      }
      dockerClient = new DockerClientImpl()
    }
    dockerClient
  }
}
