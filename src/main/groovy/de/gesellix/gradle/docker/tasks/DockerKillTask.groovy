package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerKillTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerKillTask)

    @Input
    def containerId

    def result

    DockerKillTask() {
        description = "Kill a running container"
        group = "Docker"
    }

    @TaskAction
    def kill() {
        logger.info "docker kill"
        result = getDockerClient().kill(getContainerId())
        return result
    }
}
