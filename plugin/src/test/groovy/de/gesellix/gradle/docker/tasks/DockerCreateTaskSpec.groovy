package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.remote.api.ContainerCreateRequest
import de.gesellix.docker.remote.api.HostConfig
import de.gesellix.docker.remote.api.PortBinding
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerCreateTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerCreate', DockerCreateTask).get()
    task.dockerClient = dockerClient
  }

  def "delegates to dockerClient"() {
    given:
    task.imageName = "anImage"
    task.imageTag = "aTag"
    task.containerName = "aContainerName"
    task.containerConfiguration = new ContainerCreateRequest().tap {
      exposedPorts = [
          "8889/tcp": [],
          "9300/tcp": []
      ]
      hostConfig = new HostConfig().tap {
        portBindings = [
            "8889/tcp": [new PortBinding("0.0.0.0", "8889")]
        ]
      }
    }
    def containerConfig = new ContainerCreateRequest().tap {
      image = "anImage:aTag"
      exposedPorts = [
          "8889/tcp": [],
          "9300/tcp": []
      ]
      hostConfig = new HostConfig().tap {
        portBindings = [
            "8889/tcp": [new PortBinding("0.0.0.0", "8889")]
        ]
      }
    }

    when:
    task.create()

    then:
    1 * dockerClient.createContainer(containerConfig, "aContainerName", "")
  }

  def "parses env-file to containerConfig.Env"() {
    URL envfile = getClass().getResource('/env-files/env-test.properties')

    given:
    task.imageName = "anImage"
    task.containerConfiguration = new ContainerCreateRequest().tap {
      hostConfig = new HostConfig().tap { publishAllPorts = true }
    }
    task.environmentFiles = [new File(envfile.toURI())]
    def containerConfig = new ContainerCreateRequest().tap {
      env = ['THE_WIND=CAUGHT_IT', 'FOO=BAR Baz']
      image = "anImage"
      hostConfig = new HostConfig().tap { publishAllPorts = true }
    }

    when:
    task.create()

    then:
    1 * dockerClient.createContainer(containerConfig, '', '')
  }

  def "maps env and port properties to actual containerConfig"() {
    given:
    task.imageName = "anImage"
    task.imageTag = "aTag"
    task.containerName = "aContainerName"
    task.env = ["foo=bar"]
    task.ports = ["8080:80", "8889:8889"]
    def containerConfig = new ContainerCreateRequest().tap {
      env = ["foo=bar"]
      image = "anImage:aTag"
      exposedPorts = [
          "80/tcp"  : [:],
          "8889/tcp": [:]
      ]
      hostConfig = new HostConfig().tap {
        portBindings = [
            "80/tcp"  : [new PortBinding("0.0.0.0", "8080")],
            "8889/tcp": [new PortBinding("0.0.0.0", "8889")]
        ]
      }
    }

    when:
    task.create()

    then:
    1 * dockerClient.createContainer(containerConfig, "aContainerName", "")
  }
}
