package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.EngineResponseContent
import de.gesellix.docker.remote.api.ServiceCreateResponse
import de.gesellix.docker.remote.api.ServiceSpec
import de.gesellix.docker.remote.api.TaskSpec
import de.gesellix.docker.remote.api.TaskSpecContainerSpec
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerServiceCreateTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('createService', type: DockerServiceCreateTask)
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient and saves result"() {
    given:
    def response = new EngineResponseContent(new ServiceCreateResponse())
    def serviceSpec = new ServiceSpec().tap {
      name = "a-service"
      taskTemplate = new TaskSpec().tap {
        containerSpec = new TaskSpecContainerSpec().tap {
          image = "nginx"
        }
      }
    }
    task.serviceConfig = serviceSpec

    when:
    task.createService()

    then:
    1 * dockerClient.createService(serviceSpec) >> response

    and:
    task.response == response
  }
}
