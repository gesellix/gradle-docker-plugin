package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerInspectImageTask extends GenericDockerTask {

    @Input
    def imageId

    @Internal
    def imageInfo

    DockerInspectImageTask() {
        description = "Return low-level information on image"
        group = "Docker"
    }

    @TaskAction
    def inspect() {
        logger.info "docker inspect"

        imageInfo = getDockerClient().inspectImage(getImageId())
    }
}
