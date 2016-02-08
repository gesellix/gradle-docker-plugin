package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClientException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerDisposeContainerTask extends DockerTask {

    @Input
    def containerId
    @Input
    @Optional
    def rmiParentImage = false

    DockerDisposeContainerTask() {
        description = "Stops and removes a container and optionally its parent image"
        group = "Docker"
    }

    @TaskAction
    def dispose() {
        logger.info "docker dispose"

        def containerId = getContainerId()
        def containerDetails
        try {
            containerDetails = getDockerClient().inspectContainer(containerId)
        }
        catch (DockerClientException e) {
            if (e.detail?.status?.code == 404) {
                logger.info("couldn't dispose container because it doesn't exists")
                return
            } else {
                throw e
            }
        }
        getDockerClient().stop(containerId)
        getDockerClient().wait(containerId)
        getDockerClient().rm(containerId)
        if (getRmiParentImage()) {
            getDockerClient().rmi(containerDetails.content.Image)
        }
    }
}
