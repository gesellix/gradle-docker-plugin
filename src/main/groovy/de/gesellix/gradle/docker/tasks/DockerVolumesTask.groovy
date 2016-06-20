package de.gesellix.gradle.docker.tasks

import groovy.json.JsonBuilder
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerVolumesTask extends DockerTask {

    @Input
    def query = [:]

    def volumes

    DockerVolumesTask() {
        description = "List volumes from all volume drivers"
        group = "Docker"
    }

    @TaskAction
    def volumes() {
        logger.info "docker volume ls"
        volumes = getDockerClient().volumes(getQuery() ?: [:])
    }
}
