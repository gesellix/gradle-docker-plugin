package de.gesellix.gradle.docker.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class DockerExecTask extends DockerTask {

    @Input
    def containerId
    @Input
    def commandLine

    def result

    DockerExecTask() {
        description = "Run a command in a running container"
        group = "Docker"
    }

    @TaskAction
    def exec() {
        logger.info "docker exec"

        def execCreateConfig = [
                "AttachStdin" : false,
                "AttachStdout": true,
                "AttachStderr": true,
                "Tty"         : false,
                "Cmd"         : getCommandLine()
        ]
        if (!(getCommandLine() instanceof Collection<String>)) {
            String[] cmd = ['sh', '-c', getCommandLine()?.toString()]
            execCreateConfig.Cmd = cmd
        }
        logger.debug("exec cmd: '${execCreateConfig.Cmd}'")
        def execCreateResult = dockerClient.createExec(getContainerId(), execCreateConfig)

        def execStartConfig = [
                "Detach": false,
                "Tty"   : false]
        result = dockerClient.startExec(execCreateResult.content.Id, execStartConfig)
    }
}
