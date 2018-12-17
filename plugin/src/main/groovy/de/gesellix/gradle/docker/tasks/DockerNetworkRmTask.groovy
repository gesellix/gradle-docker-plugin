package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerNetworkRmTask extends GenericDockerTask {

    @Input
    String networkName
    @Input
    @Optional
    boolean ignoreError = false

    def response

    DockerNetworkRmTask() {
        description = "Remove a network"
        group = "Docker"
    }

    @TaskAction
    def rmNetwork() {
        logger.info "docker network rm"
        try {
            response = getDockerClient().rmNetwork(getNetworkName())
        } catch (Exception e) {
            if (!ignoreError) {
                throw new RuntimeException(e)
            } else {
                if (logger.isInfoEnabled()) {
                    logger.warn("docker network rm ${networkName} failed", e)
                } else {
                    logger.warn("docker network rm ${networkName} failed")
                }
                response = null
            }
        }
    }
}
