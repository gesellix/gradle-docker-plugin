package de.gesellix.gradle.docker.client

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import org.junit.Rule
import spock.lang.Ignore
import spock.lang.Specification

class DockerClientImplSpec extends Specification {

  DockerClient dockerClient

  @Rule
  Recorder recorder = new Recorder()

  def setup() {
    dockerClient = new DockerClientImpl()
    BetamaxRoutePlanner.configure(dockerClient.client.client)
  }

  @Ignore
  @Betamax(tape = 'build image')
  def "build image"() {
    when:
    def imageId = dockerClient.build()

    then:
    imageId == "47110815"
  }

  @Betamax(tape = 'pull image')
  def "pull image"() {
    when:
    def imageId = dockerClient.pull("scratch")

    then:
    imageId == "511136ea3c5a"
  }
}
