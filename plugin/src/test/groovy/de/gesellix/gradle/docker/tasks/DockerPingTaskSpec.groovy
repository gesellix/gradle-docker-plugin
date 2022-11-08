package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPingTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerPing', DockerPingTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def response = new EngineResponseContent("OK")

    when:
    task.ping()

    then:
    1 * dockerClient.ping() >> response

    and:
    task.result == response
  }
}
