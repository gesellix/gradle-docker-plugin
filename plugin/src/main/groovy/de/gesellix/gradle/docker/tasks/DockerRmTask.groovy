package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerRmTask extends DockerTask {

    @Input
    def containerId

    def result

    DockerRmTask() {
        description = "Remove one or more containers"
        group = "Docker"
    }

    @TaskAction
    def rm() {
        logger.info "docker rm"
        result = getDockerClient().rm(getContainerId())
        return result
    }
}
