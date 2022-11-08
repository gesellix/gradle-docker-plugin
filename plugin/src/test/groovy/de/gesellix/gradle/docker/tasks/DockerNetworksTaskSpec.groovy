package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerNetworksTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerNetworks', DockerNetworksTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def response = new EngineResponseContent([])

    when:
    task.networks()

    then:
    1 * dockerClient.networks() >> response

    and:
    task.networks == response
  }
}
