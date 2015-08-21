package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPsTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerPsTask)

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
