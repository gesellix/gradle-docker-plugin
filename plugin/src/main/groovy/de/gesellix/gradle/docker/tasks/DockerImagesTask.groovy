package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.TaskAction

class DockerImagesTask extends GenericDockerTask {

    def images

    DockerImagesTask() {
        description = "List images"
        group = "Docker"
    }

    @TaskAction
    def images() {
        logger.info "docker images"
        images = getDockerClient().images()
    }
}
