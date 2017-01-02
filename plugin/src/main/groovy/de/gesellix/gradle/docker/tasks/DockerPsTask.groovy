package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerPsTask extends DockerTask {

    def containers

    DockerPsTask() {
        description = "List containers"
        group = "Docker"
    }

    @TaskAction
    def ps() {
        logger.info "docker ps"
        containers = getDockerClient().ps()
    }
}
