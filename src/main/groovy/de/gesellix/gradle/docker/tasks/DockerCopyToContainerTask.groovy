package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerCopyToContainerTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerCopyToContainerTask)

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
