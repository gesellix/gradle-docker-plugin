package de.gesellix.gradle.docker.tasks

import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerCopyFromContainerTask extends DockerTask {

    private static Logger logger = LoggerFactory.getLogger(DockerCopyFromContainerTask)

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
