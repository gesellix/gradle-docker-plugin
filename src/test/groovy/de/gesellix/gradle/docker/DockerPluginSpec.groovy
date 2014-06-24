package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.DockerDeployTask
import de.gesellix.gradle.docker.tasks.TestTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

class DockerPluginSpec extends Specification {

  private Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  def "DockerPluginExtension is added to project"() {
    when:
    project.apply plugin: 'docker'
    then:
    project["docker"] instanceof DockerPluginExtension
  }

  def "configuration is passed to tasks"() {
    given:
    project.apply plugin: 'docker'
    project.docker.dockerHost = "http://example.org:2375"

    when:
    def task = project.tasks.create("testTask", TestTask)

    then:
    task.dockerHost == "http://example.org:2375"
  }

  @Ignore("we'll see, if we need it")
  def "DockerDeployTask is available"() {
    given:
    project.apply plugin: 'docker'
    when: "project is evaluated"
    project.evaluate()
    then:
    project.tasks.findByName("dockerDeploy") in DockerDeployTask
  }
}
