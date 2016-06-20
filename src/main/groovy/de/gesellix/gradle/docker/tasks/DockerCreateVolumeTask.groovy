package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerCreateVolumeTask extends DockerTask {

    @Input
    def volumeConfig = [:]

    def response

    @TaskAction
    def createVolume() {
        logger.info "docker volume create"

        response = getDockerClient().createVolume(getVolumeConfig() ?: [:])
        return response
    }
}
