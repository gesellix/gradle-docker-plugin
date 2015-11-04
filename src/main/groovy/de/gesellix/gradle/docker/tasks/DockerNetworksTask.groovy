package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerNetworksTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerNetworksTask)

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
