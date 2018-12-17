package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerNetworkConnectTask extends GenericDockerTask {

    @Input
    String networkName

    @Input
    String containerName

    def response

    DockerNetworkConnectTask() {
        description = "Connects a container to a network"
        group = "Docker"
    }

    @TaskAction
    def connectNetwork() {
        logger.info "docker network connect"
        response = getDockerClient().connectNetwork(getNetworkName(), getContainerName())
    }
}
