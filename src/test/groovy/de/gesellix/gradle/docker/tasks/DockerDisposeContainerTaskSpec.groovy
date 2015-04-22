package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientException
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
    task.execute()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712")
    and:
    0 * dockerClient.rmi(_)
  }

  def "delegates to dockerClient (w/ removing the parent image)"() {
    given:
    task.containerId = "4712"
    task.rmiParentImage = true
    dockerClient.inspectContainer("4712") >> [content: [Image: "an-image-id"]]

    when:
    task.execute()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712")
    then:
    1 * dockerClient.rmi("an-image-id")
  }

  def "catches DockerClientException when container is not present"() {
    given:
    task.containerId = "4711"

    when:
    task.execute()

    then:
    1 * dockerClient.inspectContainer("4711") >> {
      throw new DockerClientException(new IllegalArgumentException("foo"), [status: [code: 404]])
    }
    then:
    0 * dockerClient._
  }
}
