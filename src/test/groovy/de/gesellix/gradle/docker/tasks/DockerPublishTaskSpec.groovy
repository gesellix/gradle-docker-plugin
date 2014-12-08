package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPublishTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerPublish', type: DockerPublishTask)
    task.dockerClient = dockerClient
  }

  def "delegates to DockerBuildTask"() {
    given:
    task.configure {
      imageName = "busybox"
    }

    when:
    task.execute()

    then:
    project.tasks.findByName("dockerPublish") instanceof DockerPublishTask
    and:
    project.tasks.findByName("buildImageInternal") instanceof DockerBuildTask
    and:
    project.tasks.findByName("buildImageInternal").imageName == "busybox"
    and:
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("buildImageInternal")
  }

  def "delegates to DockerPushTask when targetRegistries are configured"() {
    given:
    task.configure {
      imageName = "busybox"
      targetRegistries = ["private": "registry.example.internal:5000"]
    }

    when:
    task.execute()

    then:
    project.tasks.findByName("dockerPublish") instanceof DockerPublishTask
    and:
    project.tasks.findByName("buildImageInternal") instanceof DockerBuildTask
    and:
    project.tasks.findByName("pushImageToPrivateInternal") instanceof DockerPushTask
    and:
    project.tasks.findByName("rmiPrivateImage") instanceof DockerRmiTask
    and:
    project.tasks.findByName("pushImageToPrivateInternal").getDependsOn().contains project.tasks.findByName("buildImageInternal")
    and:
    project.tasks.findByName("pushImageToPrivateInternal").getFinalizedBy().values.contains project.tasks.findByName("rmiPrivateImage")
    and:
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("buildImageInternal")
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("pushImageToPrivateInternal")
  }
}
