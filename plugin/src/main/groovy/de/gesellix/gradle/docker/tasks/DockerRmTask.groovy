package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerRmTask extends GenericDockerTask {

    @Input
    def containerId

    @Input
    @Optional
    Boolean removeVolumes = false

    @Internal
    def result

    DockerRmTask() {
        description = "Remove one or more containers"
        group = "Docker"
    }

    @TaskAction
    def rm() {
        logger.info "docker rm"
        if (getRemoveVolumes() == null) {
            setRemoveVolumes(false)
        }
        result = getDockerClient().rm(getContainerId(), ["v": getRemoveVolumes() ? 1 : 0])
        return result
    }
}
