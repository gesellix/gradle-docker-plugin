package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.remote.api.SwarmJoinRequest
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerSwarmJoinTaskSpec extends Specification {

  def project
  DockerSwarmJoinTask task
  def dockerClient = Mock(DockerClient)
  private SwarmJoinRequest swarmJoinRequest

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('joinSwarm', DockerSwarmJoinTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    swarmJoinRequest = new SwarmJoinRequest(
        "0.0.0.0:4500",
        null,
        null,
        ["node1:4500"],
        null)
    task.config.set(swarmJoinRequest)

    when:
    task.joinSwarm()

    then:
    1 * dockerClient.joinSwarm(swarmJoinRequest)
  }
}
