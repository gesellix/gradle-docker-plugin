package de.gesellix.gradle.docker

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
    project.apply plugin: 'gradle-docker'
    then:
    project["gradle-docker"] instanceof GradleDockerPluginExtension
  }
}
