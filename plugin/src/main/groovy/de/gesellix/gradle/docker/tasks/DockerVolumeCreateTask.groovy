package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerVolumeCreateTask extends DockerTask {

    @Input
    def volumeConfig = [:]

    def response

    DockerVolumeCreateTask() {
        description = "Create a volume"
        group = "Docker"
    }

    @TaskAction
    def createVolume() {
        logger.info "docker volume create"

        response = getDockerClient().createVolume(getVolumeConfig() ?: [:])
        return response
    }
}
