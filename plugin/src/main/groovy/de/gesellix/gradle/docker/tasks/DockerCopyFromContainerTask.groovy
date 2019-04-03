package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.engine.EngineResponse
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class DockerCopyFromContainerTask extends GenericDockerTask {

    @Input
    String container
    @Input
    String sourcePath

    @Internal
    EngineResponse content

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
