package de.gesellix.gradle.docker

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.LocalDocker
import de.gesellix.gradle.docker.tasks.DockerBuildTask
import de.gesellix.gradle.docker.tasks.DockerContainerTask
import de.gesellix.gradle.docker.tasks.DockerImagesTask
import de.gesellix.gradle.docker.tasks.DockerInfoTask
import de.gesellix.gradle.docker.tasks.DockerNetworkCreateTask
import de.gesellix.gradle.docker.tasks.DockerPsTask
import de.gesellix.gradle.docker.tasks.DockerPullTask
import de.gesellix.gradle.docker.tasks.DockerPushTask
import de.gesellix.gradle.docker.tasks.DockerRmTask
import de.gesellix.gradle.docker.tasks.DockerRunTask
import de.gesellix.gradle.docker.tasks.DockerServiceCreateTask
import de.gesellix.gradle.docker.tasks.DockerStartTask
import de.gesellix.gradle.docker.tasks.DockerStopTask
import de.gesellix.gradle.docker.tasks.DockerSwarmInitTask
import de.gesellix.gradle.docker.tasks.DockerTask
import de.gesellix.gradle.docker.tasks.DockerVolumeCreateTask
import de.gesellix.gradle.docker.tasks.DockerVolumeRmTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

@Requires({ LocalDocker.available() })
class DockerPluginIntegrationTest extends Specification {

    @Shared
    Project project

    def setup() {
        project = ProjectBuilder.builder().withName('example').build()
        project.apply plugin: 'de.gesellix.docker'
//        System.setProperty("docker.cert.path", "${System.getProperty('user.home')}/.docker/machine/machines/default")
//        project.docker.dockerHost = System.env.DOCKER_HOST ?: "tcp://192.168.99.100:2376"
//        project.docker.dockerHost = System.env.DOCKER_HOST ?: "unix:///var/run/docker.sock"
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
        def dockerClient = new DockerClientImpl()
        dockerClient.rmi("buildTest")
    }

    def "test pull"() {
        given:
        def task = project.task('testPull', type: DockerPullTask) {
            imageName = 'gesellix/testimage'
            tag = 'os-linux'
        }

        when:
        task.execute()

        then:
        task.imageId == 'sha256:0ce18ad10d281bef97fe2333a9bdcc2dbf84b5302f66d796fed73aac675320db'
    }

    @Ignore
    "test pull with auth"() {
        given:
        def dockerClient = new DockerClientImpl()
        def task = project.task('testPull', type: DockerPullTask) {
            authConfigPlain = dockerClient.readAuthConfig(null, null)
            imageName = 'gesellix/private-repo'
            tag = 'latest'
        }

        when:
        task.execute()

        then:
        task.imageId == 'sha256:6b552ee013ffc56b05df78b83a7b9717ebb99aa32224cf012c5dbea811b42334'
    }

    def "test push"() {
        given:
        def authDetails = ["username"     : "gesellix",
                           "password"     : "-yet-another-password-",
                           "email"        : "tobias@gesellix.de",
                           "serveraddress": "https://index.docker.io/v1/"]
        def dockerClient = new DockerClientImpl()
        def authConfig = dockerClient.encodeAuthConfig(authDetails)
        dockerClient.pull("gesellix/testimage", "os-linux")
        dockerClient.tag("gesellix/testimage:os-linux", "gesellix/example")

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
            containerConfiguration = ["Cmd": ["true"]]
            imageName = 'gesellix/testimage'
            tag = 'os-linux'
        }

        when:
        task.execute()

        then:
        task.result.container.content.Id ==~ "[a-z0-9]+"
        and:
        task.result.status.status.code == 204

        cleanup:
        def dockerClient = new DockerClientImpl()
        dockerClient.stop(task.result.container.content.Id)
        dockerClient.wait(task.result.container.content.Id)
        dockerClient.rm(task.result.container.content.Id)
    }

    def "test stop"() {
        given:
        def dockerClient = new DockerClientImpl()
        def runResult = dockerClient.run(
                'gesellix/testimage',
                ["Cmd": ["ping", "127.0.0.1"]],
                'os-linux')
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
        def dockerClient = new DockerClientImpl()
        def runResult = dockerClient.run('gesellix/testimage', ["Cmd": ["true"]], 'os-linux')
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
        def dockerClient = new DockerClientImpl()
        dockerClient.pull("gesellix/testimage", "os-linux")
        def containerInfo = dockerClient.createContainer([
                "Image": "gesellix/testimage:os-linux",
                "Cmd"  : ["true"]])
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
        def task = project.task('testPs', type: DockerPsTask)
        def uuid = UUID.randomUUID().toString()
        def cmd = "true || $uuid".toString()
        def containerInfo = new DockerClientImpl().run('gesellix/testimage', ["Cmd": [cmd]], 'os-linux')

        when:
        task.execute()

        then:
        task.containers.content.findAll {
            it.Command == cmd
        }.size() == 1

        cleanup:
        def dockerClient = new DockerClientImpl()
        dockerClient.stop(containerInfo.container.content.Id)
        dockerClient.wait(containerInfo.container.content.Id)
        dockerClient.rm(containerInfo.container.content.Id)
    }

    def "test images"() {
        given:
        def dockerClient = new DockerClientImpl()
        dockerClient.pull("gesellix/testimage", "os-linux")
        dockerClient.tag("gesellix/testimage:os-linux", "gesellix/images-list")
        def task = project.task('testImages', type: DockerImagesTask)

        when:
        task.execute()

        then:
        task.images.content.findAll {
            it.RepoTags?.contains "gesellix/images-list:latest"
        }.size() == 1

        cleanup:
        dockerClient.rmi("gesellix/images-list:latest")
    }

    def "test run with data volumes"() {
        given:
        def hostDir = "/tmp"
        def dockerClient = new DockerClientImpl()
        dockerClient.pull("gesellix/testimage", "os-linux")
        dockerClient.tag("gesellix/testimage", "gesellix/run-with-data-volumes")
        dockerClient.createContainer([
                "Cmd"       : ["-"],
                "Image"     : "gesellix/run-with-data-volumes",
                "HostConfig": [
                        "Binds": [
                                "$hostDir:/data"
                        ]
                ],
        ], [name: "the-data-example"])
        def task = project.task('testRun', type: DockerRunTask) {
            containerConfiguration = ["Cmd"       : ["true"],
                                      "HostConfig": ["VolumesFrom": ["the-data-example"]]]
            imageName = 'gesellix/run-with-data-volumes'
            tag = 'latest'
            containerName = 'the-service-example'
        }

        when:
        task.execute()

        then:
        task.result.status.status.success

        cleanup:
        dockerClient.stop("the-service-example")
        dockerClient.wait("the-service-example")
        dockerClient.rm("the-service-example")
        dockerClient.rm("the-data-example")
        dockerClient.rmi("gesellix/run-with-data-volumes:latest")
    }

    def "test volume create and remove"() {
        given:
        def createVolume = project.task('createVolume', type: DockerVolumeCreateTask) {
            volumeConfig = [
                    Name      : "my-volume",
                    Driver    : "local",
                    DriverOpts: [:]
            ]
        }
        def rmVolume = project.task('rmVolume', type: DockerVolumeRmTask) {
            volumeName = "my-volume"
        }
        createVolume.execute()

        when:
        rmVolume.execute()

        then:
        createVolume.response.status.code == 201
        rmVolume.response.status.code == 204
    }

    def "test swarm with services"() {
//    docker swarm init
//    docker network create -d overlay my-network
//    docker service create --name my-service --replicas 2 --network my-network -p 80:80/tcp nginx
        given:
        def dockerClient = new DockerClientImpl()

        def swarmConfig = [
                "ListenAddr"     : "0.0.0.0:80",
                "ForceNewCluster": false,
                "Spec"           : [
                        "AcceptancePolicy": [
                                "Policies": [
                                        ["Role": "MANAGER", "Autoaccept": false],
                                        ["Role": "WORKER", "Autoaccept": true]
                                ]
                        ]
                ]
        ]
        def initSwarm = project.task('initSwarm', type: DockerSwarmInitTask) {
            config = swarmConfig
        }
        initSwarm.execute()

        def createNetwork = project.task('createNetwork', type: DockerNetworkCreateTask) {
            networkName = "my-network"
            networkConfig = [
                    Driver: "overlay",
                    "IPAM": [
                            "Driver": "default"
                    ]]
        }
        createNetwork.execute()

        def config = [
                "Name"        : "my-service",
                "TaskTemplate": [
                        "ContainerSpec": ["Image": "nginx"],
                        "RestartPolicy": ["Condition": "on-failure"]
                ],
                "Mode"        : ["Replicated": ["Replicas": 2]],
                "Networks"    : [["Target": "my-network"]],
                "EndpointSpec": [
                        "Ports": [
                                [
                                        "Protocol"     : "tcp",
                                        "TargetPort"   : 80,
                                        "PublishedPort": 80
                                ]
                        ]
                ]
        ]

        def createService = project.task('createService', type: DockerServiceCreateTask) {
            serviceConfig = config
        }

        when:
        createService.execute()

        then:
        createService.response.status.code == 201

        cleanup:
        dockerClient.rmService("my-service")
        dockerClient.rmNetwork("my-network")
        dockerClient.leaveSwarm([force: true])
    }

    def "test container reloaded"() {
        given:
        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["80:8080"]
            env = ["TMP=1"]
            ignoredEnvKeys = ["no_proxy"]
            cmd = ["ping", "127.0.0.1"]
            volumes = ["/tmp:/data:ro"]
            extraHosts = ["dockerhost:127.0.0.1"]
        }
        def task2 = project.task('testContainer2', type: DockerContainerTask) {
            containerName = "docker-test"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["80:8080"]
            env = ["TMP=1"]
            ignoredEnvKeys = ["no_proxy"]
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
        ServerSocket ss = new ServerSocket(0)
        int port = ss.getLocalPort()
        ss.close()

        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["$port:8080"]
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
        ServerSocket ss = new ServerSocket(0)
        int port = ss.getLocalPort()
        ss.close()

        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["$port:8080"]
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
        ServerSocket ss = new ServerSocket(0)
        int port = ss.getLocalPort()
        ss.close()

        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["$port:8080"]
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
        ServerSocket ss = new ServerSocket(0)
        int port = ss.getLocalPort()
        ss.close()

        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["$port:8080"]
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
        ServerSocket ss = new ServerSocket(0)
        int port = ss.getLocalPort()
        ss.close()

        def dockerClient = new DockerClientImpl()
        def task = project.task('testContainer', type: DockerContainerTask) {
            containerName = "docker-test-health"
            targetState = "reloaded"
            image = "gesellix/testimage"
            tag = "os-linux"
            ports = ["$port:8080"]
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

    def "test certPath"() {
        given:
        def task = project.task('testTask', type: DockerTask) {
            certPath = "${System.getProperty('user.home')}/.docker/machine/machines/default"
        }
        task.actions.add(new Action<Task>() {
            @Override
            void execute(Task t) {
                def version = t.getDockerClient().version()
                t.extensions.add("version", version)
            }
        })

        when:
        task.execute()

        then:
        task.extensions.getByName('version').content.ApiVersion == '1.29'
    }
}
