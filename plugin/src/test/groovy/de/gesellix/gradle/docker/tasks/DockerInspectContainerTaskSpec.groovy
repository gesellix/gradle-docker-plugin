package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ContainerInspectResponse
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerInspectContainerTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerInspect', DockerInspectContainerTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and returns result"() {
    given:
    task.containerId = "4711"
    def expectedResponse = new EngineResponseContent(new ContainerInspectResponse().tap { id = "123" })

    when:
    task.inspect()

    then:
    1 * dockerClient.inspectContainer("4711") >> expectedResponse
    and:
    task.containerInfo == expectedResponse
  }
}
