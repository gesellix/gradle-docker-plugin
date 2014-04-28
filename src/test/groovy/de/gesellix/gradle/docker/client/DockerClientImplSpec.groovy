package de.gesellix.gradle.docker.client

import co.freeside.betamax.Betamax
import co.freeside.betamax.MatchRule
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

  @Betamax(tape = 'build image')
  def "build image"() {
    given:
    def buildContext = getClass().getResourceAsStream("build/build.tar")

    when:
    def buildResult = dockerClient.build(buildContext)

    then:
    buildResult == "Successfully built 3f076777da89"
  }

  @Ignore
  @Betamax(tape = 'tag image', match = [MatchRule.path])
  def "tag image"() {
    given:
    def imageId = dockerClient.pull("scratch")
    def repositoryName = "yetAnotherTag"

    when:
    // /images/test/tag?repo=myrepo&force=0
    def buildResult = dockerClient.tag(imageId, repositoryName)

    then:
    buildResult == "OK"
  }

  @Betamax(tape = 'pull image')
  def "pull image"() {
    when:
    def imageId = dockerClient.pull("scratch")

    then:
    imageId == "511136ea3c5a"
  }
}
