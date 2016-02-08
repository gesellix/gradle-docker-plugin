package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerInfoTask extends DockerTask {

    def info

    DockerInfoTask() {
        description = "Display system-wide information"
        group = "Docker"
    }

    @TaskAction
    def info() {
        logger.info "docker info"
        info = getDockerClient().info()
    }
}
