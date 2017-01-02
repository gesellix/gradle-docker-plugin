package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerNetworksTask extends DockerTask {

    def networks

    DockerNetworksTask() {
        description = "Lists all networks"
        group = "Docker"
    }

    @TaskAction
    def networks() {
        logger.info "docker network ls"
        networks = getDockerClient().networks()
    }
}
