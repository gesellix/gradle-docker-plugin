package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.authentication.AuthConfig
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.engine.DockerEnv
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GenericDockerTaskSpec extends Specification {

  def task

  def setup() {
    def project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerTask', TestTask).get()
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
    DockerClientImpl dockerClient = task.dockerClient

    then:
    dockerClient.env.dockerHost == DockerEnv.getDefaultDockerHost()
  }

  def "delegates to dockerClient with configured dockerHost"() {
    when:
    task.dockerHost.set("http://example.org:4243")
    def dockerClient = task.dockerClient

    then:
    dockerClient.env.dockerHost == "http://example.org:4243"
  }

  def "delegates to dockerClient with configured certPath"() {
    when:
    task.certPath.set("/path/to/certs")
    def dockerClient = task.dockerClient

    then:
    dockerClient.env.certPath.endsWith "/path/to/certs".replaceAll('/', "\\${File.separator}")
  }

  def "getAuthConfig with plain AuthConfig"() {
    when:
    task.authConfig = new AuthConfig(identitytoken: "foo")

    then:
    task.getEncodedAuthConfig() == "eyJpZGVudGl0eXRva2VuIjoiZm9vIn0="
  }

  def "getAuthConfig without AuthConfig"() {
    when:
    task.authConfig = null

    then:
    task.getEncodedAuthConfig() == ''
  }
}
