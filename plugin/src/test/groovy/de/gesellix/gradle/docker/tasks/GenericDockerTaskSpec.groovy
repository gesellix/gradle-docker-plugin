package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.authentication.AuthConfig
import de.gesellix.docker.engine.DockerEnv
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GenericDockerTaskSpec extends Specification {

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
    DockerClientImpl dockerClient = task.dockerClient

    then:
    dockerClient.env.dockerHost == DockerEnv.dockerHostOrDefault
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

  def "getAuthConfig with plain Map (deprecated)"() {
    when:
    task.authConfigPlain = [identitytoken: "foo"]

    then:
    task.getEncodedAuthConfig() == "eyJpZGVudGl0eXRva2VuIjoiZm9vIn0="
  }

  def "getAuthConfig with plain AuthConfig"() {
    when:
    task.authConfigPlain = new AuthConfig(identitytoken: "foo")

    then:
    task.getEncodedAuthConfig() == "eyJpZGVudGl0eXRva2VuIjoiZm9vIn0="
  }

  def "getAuthConfig with encoded AuthConfig"() {
    when:
    task.authConfigEncoded = "--auth.base64--"

    then:
    thrown(UnsupportedOperationException)
  }

  def "getAuthConfig without AuthConfig"() {
    when:
    task.authConfigPlain = null

    then:
    task.getEncodedAuthConfig() == ''
  }
}
