package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerNetworkRmTask extends DockerTask {

    @Input
    String networkName

    def response

    DockerNetworkRmTask() {
        description = "Remove a network"
        group = "Docker"
    }

    @TaskAction
    def rmNetwork() {
        logger.info "docker network rm"
        response = getDockerClient().rmNetwork(getNetworkName())
    }
}
