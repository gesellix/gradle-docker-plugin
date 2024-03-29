package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ContainerInspectResponse
import de.gesellix.docker.remote.api.core.ClientException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.FailsWith
import spock.lang.Specification

class DockerDisposeContainerTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerDispose', DockerDisposeContainerTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient (w/o removing the parent image)"() {
    given:
    task.containerId = "4712"
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse())

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
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse().tap { image = "an-image-id" })

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

  @FailsWith(RuntimeException)
  def "fails when removing the parent image has errors"() {
    given:
    task.containerId = "4712"
    task.rmiParentImage = true
    // default: false
//    task.rmiParentImageIgnoreError = false
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse().tap { image = "an-image-id" })

    when:
    task.dispose()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712", [v: 0])
    then:
    1 * dockerClient.rmi("an-image-id") >> { throw new RuntimeException("expected error") }
  }

  def "can ignore errors when removing the parent image"() {
    given:
    task.containerId = "4712"
    task.rmiParentImage = true
    task.rmiParentImageIgnoreError = true
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse().tap { image = "an-image-id" })

    when:
    task.dispose()

    then:
    1 * dockerClient.stop("4712")
    then:
    1 * dockerClient.wait("4712")
    then:
    1 * dockerClient.rm("4712", [v: 0])
    then:
    1 * dockerClient.rmi("an-image-id") >> { throw new RuntimeException("expected error") }
    notThrown(Exception)
  }

  def "catches ClientException when container is not present"() {
    given:
    task.containerId = "4711"

    when:
    task.dispose()

    then:
    1 * dockerClient.inspectContainer("4711") >> {
      throw new ClientException("foo", 404, null)
    }
    then:
    0 * dockerClient._
  }

  def "allows to removeVolumes"() {
    given:
    task.containerId = "4712"
    task.removeVolumes = true
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse())

    when:
    task.dispose()

    then:
    1 * dockerClient.rm("4712", [v: 1])
  }

  def "does not remove Volumes by default"() {
    given:
    task.containerId = "4712"
    dockerClient.inspectContainer("4712") >> new EngineResponseContent<>(new ContainerInspectResponse())

    when:
    task.dispose()

    then:
    task.removeVolumes.get() == false
    1 * dockerClient.rm("4712", [v: 0])
  }
}
