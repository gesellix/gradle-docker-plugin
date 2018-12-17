package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerCopyToContainerTask extends GenericDockerTask {

    @Input
    def container
    @Input
    def targetPath
    @Input
    InputStream tarInputStream

    DockerCopyToContainerTask() {
        description = "Copy files/folders from your host to a container."
        group = "Docker"
    }

    @TaskAction
    def copyToContainer() {
        logger.info "docker cp to container"

        getDockerClient().putArchive(getContainer(), getTargetPath(), getTarInputStream())
    }
}
