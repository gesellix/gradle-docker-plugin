package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerRmVolumeTask extends DockerTask {

    @Input
    def volumeName

    def response

    @TaskAction
    def rmVolume() {
        logger.info "docker volume rm"

        response = getDockerClient().rmVolume(getVolumeName())
        return response
    }
}
