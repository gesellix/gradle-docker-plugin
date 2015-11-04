package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerVolumesTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerVolumesTask)

    def volumes

    DockerVolumesTask() {
        description = "List volumes from all volume drivers"
        group = "Docker"
    }

    @TaskAction
    def volumes() {
        logger.info "docker volume ls"
        volumes = getDockerClient().volumes()
    }
}
