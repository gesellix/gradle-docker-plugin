package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerVersionTask extends DockerTask {

    def version

    DockerVersionTask() {
        description = "Show the Docker version information"
        group = "Docker"
    }

    @TaskAction
    def version() {
        logger.info "docker version"
        version = getDockerClient().version()
    }
}
