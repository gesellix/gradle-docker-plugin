package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerNetworkCreateTask extends DockerTask {

    @Input
    String networkName

    @Input
    def networkConfig = [:]

    def response

    DockerNetworkCreateTask() {
        description = "Create a new network"
        group = "Docker"
    }

    @TaskAction
    def createNetwork() {
        logger.info "docker network create"
        response = getDockerClient().createNetwork(getNetworkName(), getNetworkConfig() ?: [:])
    }
}
