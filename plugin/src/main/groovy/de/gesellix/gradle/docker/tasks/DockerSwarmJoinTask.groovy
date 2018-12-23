package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerSwarmJoinTask extends GenericDockerTask {

    @Input
    @Optional
    def config = [:]

    @Internal
    def response

    @TaskAction
    def joinSwarm() {
        logger.info "docker swarm join"

        response = getDockerClient().joinSwarm(getConfig())
        return response
    }
}
