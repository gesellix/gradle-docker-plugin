package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerPushTask extends GenericDockerTask {

    @Input
    def repositoryName
    @Input
    @Optional
    def registry

    @Internal
    def result

    DockerPushTask() {
        description = "Push an image or a repository to a Docker registry server"
        group = "Docker"
    }

    @TaskAction
    def push() {
        logger.info "docker push"
        result = dockerClient.push(getRepositoryName() as String, getAuthConfig() as String, getRegistry() as String)
        return result
    }
}
