package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerPauseTask extends GenericDockerTask {

    @Input
    def containerId

    @Internal
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
