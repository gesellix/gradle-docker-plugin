package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerCleanupTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerCleanupTask)

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
