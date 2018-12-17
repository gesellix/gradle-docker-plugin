package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerPingTask extends GenericDockerTask {

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
