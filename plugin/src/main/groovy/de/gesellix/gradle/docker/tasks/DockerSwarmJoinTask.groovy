package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerSwarmJoinTask extends GenericDockerTask {

    @Input
    def config = [:]

    def response

    @TaskAction
    def joinSwarm() {
        logger.info "docker swarm join"

        response = getDockerClient().joinSwarm(getConfig())
        return response
    }
}
