package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerCleanupTask extends DockerTask {

    @Input
    @Optional
    def shouldKeepContainer

    DockerCleanupTask() {
        description = "Removes stopped containers and dangling images"
        group = "Docker"
    }

    @TaskAction
    def cleanup() {
        logger.info "docker cleanup"
        def keepContainer = getShouldKeepContainer() ?: { container -> false }
        dockerClient.cleanupStorage keepContainer
    }
}
