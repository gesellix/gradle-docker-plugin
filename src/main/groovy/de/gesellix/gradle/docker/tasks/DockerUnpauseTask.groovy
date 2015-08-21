package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerUnpauseTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerUnpauseTask)

    @Input
    def containerId

    def result

    DockerUnpauseTask() {
        description = "Unpause a paused container"
        group = "Docker"
    }

    @TaskAction
    def unpause() {
        logger.info "docker unpause"
        result = getDockerClient().unpause(getContainerId())
        return result
    }
}
