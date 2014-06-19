package de.gesellix.gradle.docker

import de.gesellix.gradle.docker.tasks.DockerBuildTask
import de.gesellix.gradle.docker.tasks.DockerDeployTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore("only for exploring the docker api")
class GradleDockerIntegrationTest extends Specification {

  @Shared
  Project project

  def setup() {
    project = ProjectBuilder.builder().withName('example').build()
  }

  def "test build"() {
    given:
    URL resource = getClass().getResource('build.tar')
    def task = project.task('testBuild', type: DockerBuildTask)
    task.imageName = "buildTest"
    task.buildContext = new File(resource.toURI())

    when:
    def buildResult = task.build()

    then:
    buildResult == "foo"
  }

  def "test deploy"() {
    given:
    def task = project.task('dockerDeploy', type: DockerDeployTask)
    task.imageName = 'scratch'

    when:
    def deployResult = task.deploy()

    then:
    deployResult == '511136ea3c5a'
  }
}
