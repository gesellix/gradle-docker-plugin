package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientException
import de.gesellix.docker.engine.EngineResponse
import de.gesellix.docker.engine.EngineResponseStatus
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerDisposeContainerTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerDispose', type: DockerDisposeContainerTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient (w/o removing the parent image)"() {
    given:
    task.containerId = "4712"

    when:
    task.dispose()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712", [v: 0])
    and:
    0 * dockerClient.rmi(_)
  }

  def "delegates to dockerClient (w/ removing the parent image)"() {
    given:
    task.containerId = "4712"
    task.rmiParentImage = true
    dockerClient.inspectContainer("4712") >> [content: [Image: "an-image-id"]]

    when:
    task.dispose()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712", [v: 0])
    then:
    1 * dockerClient.rmi("an-image-id")
  }

  def "catches DockerClientException when container is not present"() {
    given:
    task.containerId = "4711"

    when:
    task.dispose()

    then:
    1 * dockerClient.inspectContainer("4711") >> {
      throw new DockerClientException(new IllegalArgumentException("foo"), new EngineResponse(status: new EngineResponseStatus(code: 404)))
    }
    then:
    0 * dockerClient._
  }

  def "allows to removeVolumes"() {
    given:
    task.containerId = "4712"
    task.removeVolumes = true

    when:
    task.dispose()

    then:
    1 * dockerClient.rm("4712", [v: 1])
  }

  def "does not remove Volumes by default"() {
    given:
    task.containerId = "4712"

    when:
    task.dispose()

    then:
    task.removeVolumes.get() == false
    1 * dockerClient.rm("4712", [v: 0])
  }
}
