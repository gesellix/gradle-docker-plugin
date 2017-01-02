package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerCopyFromContainerTask extends DockerTask {

    @Input
    def container
    @Input
    def sourcePath

    def content

    DockerCopyFromContainerTask() {
        description = "Copy files/folders from a container to your host."
        group = "Docker"
    }

    @TaskAction
    def copyFromContainer() {
        logger.info "docker cp from container"

        content = getDockerClient().getArchive(getContainer(), getSourcePath())
    }
}
