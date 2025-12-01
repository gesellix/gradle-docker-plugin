package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.IPAM
import de.gesellix.docker.remote.api.NetworkCreateRequest
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerNetworkCreateTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('createNetwork', DockerNetworkCreateTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    task.networkName = "a-network"
    task.networkConfig = new NetworkCreateRequest(
        "a-network", null,
        "overlay", null, null, null, null, null, null,
        new IPAM("default", null, null),
        null, null, null)
    def expectedResult = new EngineResponseContent("result")

    when:
    task.createNetwork()

    then:
    1 * dockerClient.createNetwork(new NetworkCreateRequest(
        "a-network", null,
        "overlay", null, null, null, null, null, null,
        new IPAM("default", null, null),
        null, null, null)) >> expectedResult

    and:
    task.response == expectedResult
  }
}
