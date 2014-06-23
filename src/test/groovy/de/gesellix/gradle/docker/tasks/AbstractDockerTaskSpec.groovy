package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AbstractDockerTaskSpec extends Specification {

  def task

  def setup() {
    def project = ProjectBuilder.builder().build()
    task = project.task('dockerTask', type: TestTask)
  }

  def "creates dockerClient only once"() {
    def clientMock = Mock(DockerClient)
    given:
    task.dockerClient = clientMock

    when:
    def dockerClient = task.dockerClient

    then:
    dockerClient == clientMock
  }

  def "delegates to dockerClient with default dockerHost"() {
    when:
    def dockerClient = task.dockerClient

    then:
    dockerClient.dockerHost == "http://127.0.0.1:2375/"
  }

  def "delegates to dockerClient with configured dockerHost"() {
    when:
    task.dockerHost = "http://example.org:4243/"
    def dockerClient = task.dockerClient

    then:
    dockerClient.dockerHost == "http://example.org:4243/"
  }
}
