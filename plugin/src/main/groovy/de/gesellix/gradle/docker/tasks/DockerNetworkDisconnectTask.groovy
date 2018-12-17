package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerNetworkDisconnectTask extends GenericDockerTask {

    @Input
    String networkName

    @Input
    String containerName

    def response

    DockerNetworkDisconnectTask() {
        description = "Disconnects container from a network"
        group = "Docker"
    }

    @TaskAction
    def disconnectNetwork() {
        logger.info "docker network disconnect"
        response = getDockerClient().disconnectNetwork(getNetworkName(), getContainerName())
    }
}
