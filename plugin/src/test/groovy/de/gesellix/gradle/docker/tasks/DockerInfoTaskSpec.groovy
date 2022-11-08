package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.SystemInfo
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInfoTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerInfo', DockerInfoTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def response = new EngineResponseContent(new SystemInfo())

    when:
    task.info()

    then:
    1 * dockerClient.info() >> response

    and:
    task.info == response
  }
}
