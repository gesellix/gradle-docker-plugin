package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerWaitTask extends DockerTask {

    @Input
    def containerId

    def result

    DockerWaitTask() {
        description = "Block until a container stops, then print its exit code."
        group = "Docker"
    }

    @TaskAction
    def awaitStop() {
        logger.info "docker wait"
        result = getDockerClient().wait(getContainerId())
        return result
    }
}
