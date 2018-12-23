package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerRestartTask extends GenericDockerTask {

    @Input
    def containerId

    @Internal
    def result

    DockerRestartTask() {
        description = "Restart a running container"
        group = "Docker"
    }

    @TaskAction
    def restart() {
        logger.info "docker restart"
        result = getDockerClient().restart(getContainerId())
        return result
    }
}
