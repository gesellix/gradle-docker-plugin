package de.gesellix.gradle.docker.tasks

import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * @deprecated please use the DockerCopyFromContainerTask
 * @see DockerCopyFromContainerTask
 */
@Deprecated
class DockerCopyTask extends DockerTask {

    @Input
    def containerId
    @Input
    def filename
    @Input
    @Optional
    def targetFilename

    def content

    DockerCopyTask() {
        description = "Copy files/folders from a container's filesystem to the host path"
        group = "Docker"
    }

    @TaskAction
    def cp() {
        logger.info "docker cp"

        content = getDockerClient().copyFile(getContainerId(), getFilename())
        if (getTargetFilename()) {
            def outputStream = new FileOutputStream(getTargetFilename() as String)
            IOUtils.write(content as byte[], outputStream)
            IOUtils.closeQuietly(outputStream)
        }
    }
}
