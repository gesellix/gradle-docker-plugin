package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPingTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerPingTask)

    def result

    DockerPingTask() {
        description = "Ping the docker server"
        group = "Docker"
    }

    @TaskAction
    def ping() {
        logger.info "docker ping"
        result = getDockerClient().ping()
    }
}
