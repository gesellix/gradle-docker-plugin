package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerUnpauseTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerUnpause', DockerUnpauseTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.containerId = "4711"

    when:
    task.unpause()

    then:
    1 * dockerClient.unpause("4711")
  }
}
