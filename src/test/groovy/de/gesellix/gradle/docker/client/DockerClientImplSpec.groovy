package de.gesellix.gradle.docker.client

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import org.junit.Rule
import spock.lang.Specification

class DockerClientImplSpec extends Specification {

  DockerClient dockerClient

  @Rule
  Recorder recorder = new Recorder()

  def setup() {
    dockerClient = new DockerClientImpl()
    BetamaxRoutePlanner.configure(dockerClient.client.client)
  }

  @Betamax(tape = 'build image')
  def "build image"() {
    given:
    def buildContext = getClass().getResourceAsStream("build/build.tar")

    when:
    def buildResult = dockerClient.build(buildContext)

    then:
    buildResult == "Successfully built 3f076777da89"
  }

  @Betamax(tape = 'tag image')
  def "tag image"() {
    given:
    def imageId = dockerClient.pull("scratch")
    def repositoryName = "yetAnotherTag"

    when:
    def buildResult = dockerClient.tag(imageId, repositoryName)

    then:
    buildResult == 201
  }

  @Betamax(tape = 'pull image')
  def "pull image"() {
    when:
    def imageId = dockerClient.pull("scratch")

    then:
    imageId == "511136ea3c5a"
  }

  @Betamax(tape = 'list images')
  def "get images"() {
    when:
    def images = dockerClient.images()

    then:
    ["Created"    : 1371157430,
     "Id"         : "511136ea3c5a64f264b78b5433614aec563103b4d4702f3ba7d4d2698e22c158",
     "ParentId"   : "",
     "RepoTags"   : ["scratch:latest", "yetAnotherTag:latest"],
     "Size"       : 0,
     "VirtualSize": 0] in images
  }

  @Betamax(tape = 'create container')
  def "create container"() {
    given:
    def imageId = dockerClient.pull("busybox")

    when:
    def containerCreateInfo = dockerClient.createContainer(imageId)

    then:
    containerCreateInfo.Id == "5f3510d90e8dea56b8a80f70fad75330dad0aa4d4aea5b2b2ed16f5f766925fd"
  }

  @Betamax(tape = 'start container')
  def "start container"() {
    given:
    def imageId = dockerClient.pull("busybox")
    def containerId = dockerClient.createContainer(imageId).Id

    when:
    def startContainerResult = dockerClient.startContainer(containerId)

    then:
    startContainerResult == 204
  }
}
