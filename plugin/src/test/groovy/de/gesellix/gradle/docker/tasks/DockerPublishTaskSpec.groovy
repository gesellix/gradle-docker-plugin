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
    task.publish()

    then:
    project.tasks.findByName("dockerPublish") instanceof DockerPublishTask
    and:
    project.tasks.findByName("buildImageForDockerPublish") instanceof DockerBuildTask
    and:
    project.tasks.findByName("buildImageForDockerPublish").imageName.get() == "busybox"
    and:
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("buildImageForDockerPublish")
  }

  def "buildImageInternal must run after dockerPublish dependencies"() {
    given:
    def publishTaskDependency = project.task('publishTaskDependency', type: TestTask)
    task.configure {
      imageName = "busybox"
      dependsOn publishTaskDependency
    }

    when:
    task.publish()

    then:
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("buildImageForDockerPublish")
    and:
    project.tasks.findByName("buildImageForDockerPublish").getMustRunAfter().mutableValues.contains project.tasks.findByName("publishTaskDependency")
  }

  def "delegates to DockerPushTask when targetRegistries are configured"() {
    given:
    task.configure {
      imageName = "busybox"
      targetRegistries = ["private": "registry.example.internal:5000"]
    }

    when:
    task.publish()

    then:
    project.tasks.findByName("dockerPublish") instanceof DockerPublishTask
    and:
    project.tasks.findByName("buildImageForDockerPublish") instanceof DockerBuildTask
    and:
    project.tasks.findByName("pushImageToPrivateForDockerPublish") instanceof DockerPushTask
    and:
    project.tasks.findByName("rmiPrivateImageForDockerPublish") instanceof DockerRmiTask
    and:
    project.tasks.findByName("pushImageToPrivateForDockerPublish").getDependsOn().contains project.tasks.findByName("buildImageForDockerPublish")
    and:
    project.tasks.findByName("pushImageToPrivateForDockerPublish").getFinalizedBy().mutableValues.contains project.tasks.findByName("rmiPrivateImageForDockerPublish")
    and:
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("buildImageForDockerPublish")
    project.tasks.findByName("dockerPublish").getDependsOn().contains project.tasks.findByName("pushImageToPrivateForDockerPublish")
  }
}
