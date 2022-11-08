package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.SystemVersion
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerVersionTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerVersion', DockerVersionTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def response = new EngineResponseContent(new SystemVersion())

    when:
    task.version()

    then:
    1 * dockerClient.version() >> response

    and:
    task.version == response
  }
}
