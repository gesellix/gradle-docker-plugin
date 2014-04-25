package de.gesellix.gradle.docker

interface DockerClient {

  def pull(imageName)
}
