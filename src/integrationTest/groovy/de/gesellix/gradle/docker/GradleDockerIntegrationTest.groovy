package de.gesellix.gradle.docker

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore("only for exploring the docker api")
class GradleDockerIntegrationTest extends Specification {

  @Shared
  Project project
  @Shared
  def projectDir

  def setup() {
    URL resource = getClass().getResource('/example.gradle')
    projectDir = new File(resource.toURI()).getParentFile()
    project = ProjectBuilder.builder().withName('example').withProjectDir(projectDir).build()
  }

  def "test deploy"() {
    when:
    def task = project.task('dockerDeploy', type: DockerDeployTask)
    task.imageName = 'scratch'

    then:
    task.deploy() == '511136ea3c5a'
  }
}
