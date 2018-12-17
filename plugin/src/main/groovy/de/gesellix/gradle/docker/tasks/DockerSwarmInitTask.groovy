package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerSwarmInitTask extends GenericDockerTask {

    @Input
    def config = [:]

    def response

    @TaskAction
    def initSwarm() {
        logger.info "docker swarm init"

        response = getDockerClient().initSwarm(getConfig())
        return response
    }
}
