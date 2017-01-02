package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerPullTask extends DockerTask {

    @Input
    def imageName
    @Input
    @Optional
    def tag
    @Input
    @Optional
    def registry

    def imageId

    DockerPullTask() {
        description = "Pull an image or a repository from a Docker registry server"
        group = "Docker"
    }

    @TaskAction
    def pull() {
        logger.info "docker pull"
        imageId = dockerClient.pull(getImageName(), getTag(), getAuthConfig(), getRegistry())
        return imageId
    }
}
