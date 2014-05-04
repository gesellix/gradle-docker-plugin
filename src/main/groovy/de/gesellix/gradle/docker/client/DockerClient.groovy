package de.gesellix.gradle.docker.client

interface DockerClient {

  def auth(def authDetails)

  def build(InputStream buildContext)

  def tag(imageId, repositoryName)

  def push(repositoryName)

  def pull(imageName)

  def stop()

  def rm()

  def rmi()

  def run()

  def ps()

  def images()

  def createContainer(fromImage, cmd)

  def startContainer(containerId)
}
