package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.gradle.docker.tasks.*
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

@IgnoreIf({ !System.env.DOCKER_HOST })
class DockerPluginIntegrationTest extends Specification {

    @Shared
    Project project

//  def defaultDockerHost = "unix:///var/run/docker.sock"
    def defaultDockerHost = System.env.DOCKER_HOST?.replaceFirst("tcp://", "http://")
    def DOCKER_HOST = defaultDockerHost

    def setup() {
//        System.setProperty("docker.cert.path", "/Users/gesellix/.docker/machine/machines/default")
//        DOCKER_HOST = "tcp://192.168.99.100:2376"
        project = ProjectBuilder.builder().withName('example').build()
        project.apply plugin: 'de.gesellix.docker'
        project.docker.dockerHost = DOCKER_HOST
    }

    def "test info"() {
        given:
        def task = project.task('testInfo', type: DockerInfoTask)

        when:
        task.execute()

        then:
        task.info.status.code == 200
    }

    def "test build"() {
        given:
//    def resource = getClass().getResourceAsStream('build.tar')
        def resource = getClass().getResource('/docker/Dockerfile')
        def task = project.task('testBuild', type: DockerBuildTask) {
            imageName = "buildTest"
//    buildContext = resource
            buildContextDirectory = new File(resource.toURI()).parentFile
        }
        task.tarOfBuildcontextTask.execute()

        when:
        task.execute()

        then:
        task.imageId ==~ "[a-z0-9]+"

        cleanup:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        dockerClient.rmi("buildTest")
    }

    def "test pull"() {
        given:
        def task = project.task('testPull', type: DockerPullTask) {
            imageName = 'gesellix/docker-client-testimage'
            tag = 'latest'
        }

        when:
        task.execute()

        then:
        task.imageId == 'ed9f0eb28ab34add30d4a2bfea3f548ba991d7702315b33f7309a64cd5d56390'
    }

    @Ignore
    def "test pull with auth"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testPull', type: DockerPullTask) {
            authConfigPlain = dockerClient.readAuthConfig(null, null)
            imageName = 'gesellix/private-repo'
            tag = 'latest'
        }

        when:
        task.execute()

        then:
        task.imageId == 'ed9f0eb28ab34add30d4a2bfea3f548ba991d7702315b33f7309a64cd5d56390'
    }

    def "test push"() {
        given:
        def authDetails = ["username"     : "gesellix",
                           "password"     : "-yet-another-password-",
                           "email"        : "tobias@gesellix.de",
                           "serveraddress": "https://index.docker.io/v1/"]
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def authConfig = dockerClient.encodeAuthConfig(authDetails)
        dockerClient.pull("gesellix/docker-client-testimage")
        dockerClient.tag("gesellix/docker-client-testimage", "gesellix/example", true)

        def task = project.task('testPush', type: DockerPushTask) {
            repositoryName = 'gesellix/example'
//    authConfigPlain = authDetails
            authConfigEncoded = authConfig
            // don't access the official registry,
            // so that we don't try (and fail) to authenticate for each test execution.
            // for a real test, this line needs to be commented or removed, though.
            registry = 'example.com:5000'
        }

        when:
        task.execute()

        then:
        //pushResult.status ==~ "Pushing tag for rev \\[[a-z0-9]+\\] on \\{https://registry-1.docker.io/v1/repositories/gesellix/example/tags/latest\\}"
        //pushResult.error ==~ "Error: Status 401 trying to push repository gesellix/example: \"\""
        def exc = thrown(Exception)
        exc.cause.cause.message == "docker push failed"
        exc.cause.detail?.content?.status?.any { it?.contains "example.com:5000/gesellix/example" }

        cleanup:
        dockerClient.rmi("gesellix/example")
        dockerClient.rmi("example.com:5000/gesellix/example")
    }

    def "test run"() {
        given:
        def task = project.task('testRun', type: DockerRunTask) {
            dockerHost = DOCKER_HOST
            containerConfiguration = ["Cmd": ["true"]]
            imageName = 'gesellix/docker-client-testimage'
            tag = 'latest'
        }

        when:
        task.execute()

        then:
        task.result.container.content.Id ==~ "[a-z0-9]+"
        and:
        task.result.status.status.code == 204

        cleanup:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        dockerClient.stop(task.result.container.content.Id)
        dockerClient.wait(task.result.container.content.Id)
        dockerClient.rm(task.result.container.content.Id)
    }

    def "test stop"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def runResult = dockerClient.run(
                'gesellix/docker-client-testimage',
                ["Cmd": ["ping", "127.0.0.1"]],
                'latest')
        def task = project.task('testStop', type: DockerStopTask) {
            containerId = runResult.container.content.Id
        }

        when:
        task.execute()

        then:
        task.result.status.code == 204 || task.result.status.code == 304

        cleanup:
        dockerClient.wait(runResult.container.content.Id)
        dockerClient.rm(runResult.container.content.Id)
    }

    def "test rm"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def runResult = dockerClient.run('gesellix/docker-client-testimage', ["Cmd": ["true"]], 'latest')
        def runningContainerId = runResult.container.content.Id
        dockerClient.stop(runningContainerId)
        dockerClient.wait(runningContainerId)
        def task = project.task('testRm', type: DockerRmTask) {
            containerId = runningContainerId
        }

        when:
        task.execute()

        then:
        task.result.status.code == 204
    }

    def "test start"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        dockerClient.pull("gesellix/docker-client-testimage", "latest")
        def containerInfo = dockerClient.createContainer(["Image": "gesellix/docker-client-testimage:latest", "Cmd": ["true"]])
        def task = project.task('testStart', type: DockerStartTask) {
            containerId = containerInfo.content.Id
        }

        when:
        task.execute()

        then:
        task.result.status.code == 204

        cleanup:
        dockerClient.stop(containerInfo.content.Id)
        dockerClient.wait(containerInfo.content.Id)
        dockerClient.rm(containerInfo.content.Id)
    }

    def "test ps"() {
        given:
        def task = project.task('testPs', type: DockerPsTask) {
            dockerHost = DOCKER_HOST
        }
        def uuid = UUID.randomUUID().toString()
        def cmd = "true || $uuid".toString()
        def containerInfo = new DockerClientImpl(dockerHost: DOCKER_HOST).run('gesellix/docker-client-testimage', ["Cmd": [cmd]], 'latest')

        when:
        task.execute()

        then:
        task.containers.content.findAll {
            it.Command == cmd
        }.size() == 1

        cleanup:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        dockerClient.stop(containerInfo.container.content.Id)
        dockerClient.wait(containerInfo.container.content.Id)
        dockerClient.rm(containerInfo.container.content.Id)
    }

    def "test images"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        dockerClient.pull("gesellix/docker-client-testimage")
        dockerClient.tag("gesellix/docker-client-testimage", "gesellix/images-list", true)
        def task = project.task('testImages', type: DockerImagesTask) {
            dockerHost = DOCKER_HOST
        }

        when:
        task.execute()

        then:
        task.images.content.findAll {
            it.RepoTags.contains "gesellix/images-list:latest"
        }.size() == 1

        cleanup:
        dockerClient.rmi("gesellix/images-list:latest")
    }

    def "test container reloaded"() {
        given:
        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = ["80:8080"]
            env = ["TMP=1"]
            cmd = ["ping", "127.0.0.1"]
            volumes = ["/tmp:/data:ro"]
            extraHosts = ["dockerhost:127.0.0.1"]
        }
        def task2 = project.task('testContainer2', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = ["80:8080"]
            env = ["TMP=1"]
            cmd = ["ping", "127.0.0.1"]
            volumes = ["/tmp:/data:ro"]
            extraHosts = ["dockerhost:127.0.0.1"]
        }

        when:
        task.execute()
        task2.execute()

        then:
        task.changed == true
        task2.changed == false
        task.container.id == task2.container.id

        cleanup:
        dockerClient.stop(task.container.id)
        dockerClient.wait(task.container.id)
        dockerClient.rm(task.container.id)
    }

    // TODO: Add UpToDate tests with GradleRunner (from gradleTestKit)

    def "test container tcp health check"() {
        given:
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();

        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = [port.toString() + ":8080"]
            cmd = ["nc", "-l", "-p", "8080"]
            healthChecks = [
                    [
                            type         : "tcp",
                            containerPort: 8080
                    ]
            ]
        }

        when:
        task.execute()

        then:
        task.changed == true

        cleanup:
        dockerClient.stop(task.container.id)
        dockerClient.wait(task.container.id)
        dockerClient.rm(task.container.id)
    }

    def "test container http health check"() {
        given:
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();

        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = [port.toString() + ":8080"]
            cmd = [
                    "/bin/sh", "-c",
                    "while true; do nc -l -p 8080 -e " +
                            "echo -e \"HTTP/1.1 204 No Content\\r\\nConnection: close\\r\\n\"; done"
            ]
            healthChecks = [
                    [
                            type         : "http",
                            containerPort: 8080
                    ]
            ]
        }

        when:
        task.execute()

        then:
        task.changed == true

        cleanup:
        dockerClient.stop(task.container.id)
        dockerClient.wait(task.container.id)
        dockerClient.rm(task.container.id)
    }

    def "test container tcp health check - timeout"() {
        given:
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();

        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = [port.toString() + ":8080"]
            cmd = ["ping", "127.0.0.1"]
            healthChecks = [
                    [
                            type         : "tcp",
                            containerPort: 8080
                    ]
            ]
        }

        when:
        task.execute()

        then:
        def e = thrown(TaskExecutionException)
        e.cause.class == IllegalStateException
        e.cause.message == "HealthCheck: Container not healthy."
        task.changed == true

        cleanup:
        dockerClient.stop(task.container.id)
        dockerClient.wait(task.container.id)
        dockerClient.rm(task.container.id)
    }

    def "test container http health check - timeout"() {
        given:
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();

        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = [port.toString() + ":8080"]
            cmd = [
                    "/bin/sh", "-c",
                    "while true; do nc -l -p 8080 -e " +
                            "echo -e \"HTTP/1.1 503 Service Unavailable\\r\\n" +
                            "Connection: close\\r\\n" +
                            "Content-Length: 0\\r\\n\"; done"
            ]
            healthChecks = [
                    [
                            type         : "http",
                            containerPort: 8080
                    ]
            ]
        }

        when:
        task.execute()

        then:
        def e = thrown(TaskExecutionException)
        e.cause.class == IllegalStateException
        e.cause.message == "HealthCheck: Container not healthy."
        task.changed == true

        cleanup:
        dockerClient.stop(task.container.id)
        dockerClient.wait(task.container.id)
        dockerClient.rm(task.container.id)
    }

    def "test container tcp health check - stopped"() {
        given:
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();

        def dockerClient = new DockerClientImpl(dockerHost: DOCKER_HOST)
        def task = project.task('testContainer', type: DockerContainerTask) {
            dockerHost = DOCKER_HOST
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/docker-client-testimage"
            tag = "latest"
            ports = [port.toString() + ":8080"]
            cmd = ["echo", "dummy"]
            healthChecks = [
                    [
                            type         : "tcp",
                            containerPort: 8080
                    ]
            ]
        }

        when:
        task.execute()

        then:
        def e = thrown(TaskExecutionException)
        e.cause.class == IllegalStateException
        e.cause.message == "HealthCheck: Container is not running." || e.cause.message == "HealthCheck: Container not healthy."
        task.changed == true

        cleanup:
        dockerClient.rm(task.container.id)
    }
}
