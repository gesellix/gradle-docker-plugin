package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.remote.api.SwarmInitRequest
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerSwarmInitTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('initSwarm', type: DockerSwarmInitTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def swarmConfig = new SwarmInitRequest().tap {
      listenAddr = "0.0.0.0:80"
    }
    task.swarmconfig.set(swarmConfig)

    when:
    task.initSwarm()

    then:
    1 * dockerClient.initSwarm(swarmConfig) >> [content: "swarm-result"]

    and:
    task.response.content == "swarm-result"
  }
}
