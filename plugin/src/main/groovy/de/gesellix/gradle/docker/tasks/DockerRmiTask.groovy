package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerRmiTask extends DockerTask {

    @Input
    def imageId

    DockerRmiTask() {
        description = "Remove one or more images"
        group = "Docker"
    }

    @TaskAction
    def rmi() {
        logger.info "docker rmi"
        getDockerClient().rmi(getImageId())
    }
}
