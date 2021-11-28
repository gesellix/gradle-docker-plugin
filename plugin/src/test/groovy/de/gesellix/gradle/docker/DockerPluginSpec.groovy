package de.gesellix.gradle.docker

import de.gesellix.docker.authentication.AuthConfig
import de.gesellix.gradle.docker.tasks.TestTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DockerPluginSpec extends Specification {

  private Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  def "DockerPluginExtension is added to project"() {
    when:
    project.apply plugin: 'de.gesellix.docker'
    then:
    project["docker"] instanceof DockerPluginExtension
  }

  def "configuration is passed to tasks"() {
    given:
    project.apply plugin: 'de.gesellix.docker'
    project.docker.dockerHost = "http://example.org:2375"
    project.docker.certPath = 'foo'
    project.docker.authConfig = new AuthConfig(username: "plain example")

    when:
    def task = project.tasks.create("testTask", TestTask)

    then:
    task.dockerHost.get() == "http://example.org:2375"
    task.certPath.get() == project.file('foo').absolutePath
    task.authConfig.get().username == "plain example"
  }

  def "returns the absolute certification path"() {
    given:
    project.apply plugin: 'de.gesellix.docker'

    when:
    project.docker.certPath = 'foo'

    then:
    project.docker.certPath == project.file('foo').absolutePath
  }
}
