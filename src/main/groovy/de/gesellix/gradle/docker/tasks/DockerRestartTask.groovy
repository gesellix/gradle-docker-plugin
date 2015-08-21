package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerRestartTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerRestartTask)

    @Input
    def containerId

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
