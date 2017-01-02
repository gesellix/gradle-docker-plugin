package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerPushTask extends DockerTask {

    @Input
    def repositoryName
    @Input
    @Optional
    def registry

    def result

    DockerPushTask() {
        description = "Push an image or a repository to a Docker registry server"
        group = "Docker"
    }

    @TaskAction
    def push() {
        logger.info "docker push"
        result = dockerClient.push(getRepositoryName(), getAuthConfig(), getRegistry())
        return result
    }
}
