package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerPauseTask extends DockerTask {

    @Input
    def containerId

    def result

    DockerPauseTask() {
        description = "Pause a running container"
        group = "Docker"
    }

    @TaskAction
    def pause() {
        logger.info "docker pause"
        result = getDockerClient().pause(getContainerId())
        return result
    }
}
