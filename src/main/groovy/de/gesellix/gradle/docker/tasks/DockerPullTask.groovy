package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerPullTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerPullTask)

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
