package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class DockerCommitTaskSpec extends Specification {

    def project
    def task
    def container
    def dockerClient = Mock(DockerClient)

    def setup() {
        project = ProjectBuilder.builder().build()
        //task = project.task('dockerCommit', type: DockerCommitTask)
        task = project.task('dockerRun', type: DockerRunTask)
        task.dockerClient = dockerClient
    }

    @Unroll
    def "delegates to dockerClient with registry=#registry"() {
        given:
        def repo = "your.local.repo"
        def tag = "container-changed:1.0"
        task.imageName = "hello-world"
        task.tag = "latest"
        task.containerName = "container-to-be-changed"
        

        when:
        task.execute()

        then:
        1 * dockerClient.run(
                "hello-world",
                [],
                "latest", "container-to-be-changed")
		
		1 * dockerClient.commit(
				"container-to-be-changed", [
				repo   : getRepo(),
				tag    : getTag(),
				comment: 'a test',
				author : 'Tue Dissing <tue@somersault.dk>'
		]
			)

    }
}
