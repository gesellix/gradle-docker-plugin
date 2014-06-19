package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.DockerDeployTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradleDockerPluginSpec extends Specification {

  private Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  def "GradleDockerPluginExtension is added to project"() {
    when:
    project.apply plugin: 'docker'
    then:
    project["docker"] instanceof GradleDockerPluginExtension
  }

  def "DockerDeployTask is available"() {
    given:
    project.apply plugin: 'docker'
    when: "project is evaluated"
    project.evaluate()
    then:
    project.tasks.findByName("dockerDeploy") in DockerDeployTask
  }
}
