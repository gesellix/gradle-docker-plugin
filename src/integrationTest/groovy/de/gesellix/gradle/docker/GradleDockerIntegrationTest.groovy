package de.gesellix.gradle.docker

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

@Ignore("only for exploring the docker api")
class GradleDockerIntegrationTest extends Specification {

  def "test"() {
    given:
    Project project = ProjectBuilder.builder().build()
    when:
    project.apply plugin: 'gradle-docker'
    def task = project.task('dockerDeploy', type: DockerDeployTask)

    then:
    task.deploy() == null
  }
}
