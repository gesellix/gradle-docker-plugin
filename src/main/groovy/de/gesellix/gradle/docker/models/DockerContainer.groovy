package de.gesellix.gradle.docker.models

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import de.gesellix.docker.client.DockerClient

class DockerContainer {
    private static Logger logger = Logging.getLogger(DockerContainer)

    String name
    String imageName
    DockerClient client
    def config

    String id
    String imageId
    boolean running
    boolean exists

    DockerContainer(DockerClient client, String name, String imageName, def config) {
        this.client = client
        this.name = name
        this.imageName = imageName
        this.config = config

        // Force Image to be ImageName in config
        this.config.Image = imageName
    }

    /**
     * Inspect container
     *
     * @return container configuration or null
     */
    def inspect() {
        if(id == null) {
            def containers = client.ps([filters: [name: [name]]]).content.findAll {
                "/" + name in it.Names
            }
            if (containers.size() > 0) {
                id = containers[0].Id
            } else {
                id = null
                imageId = null
                running = false
                exists = false
                return null
            }
        }

        logger.info "Container[{}]: Inspecting container {}", name, id
        try {
            def response = client.inspectContainer(id)

            exists = true
            imageId = response.content.Image
            running = response.content.State.Running

            return response.content

        } catch(FileNotFoundException ignored) {
            id = null
            imageId = null
            running = false
            exists = false
        } catch(IllegalStateException ignored) {
            id = null
            imageId = null
            running = false
            exists = false
        }

        return null
    }

    /**
     * Create container
     */
    void create() {
        logger.lifecycle("Container[${name}]: Creating")
        def response = client.createContainer(config, [name: name])

        if (!response.status.success) {
            throw new IllegalStateException("docker create failed: ${response.status.text}")
        }

        id = response.content.Id

        inspect()
    }

    /**
     * Start container
     */
    void start() {
        logger.lifecycle("Container[${name}]: Starting")
        def response = client.startContainer(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker start failed: ${response.status.text}")
        }

        inspect()
    }

    /**
     * Stop container
     */
    void stop() {
        logger.lifecycle("Container[${name}]: Stopping")
        def response = client.stop(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker stop failed: ${response.status.text}")
        }

        inspect()
    }

    /**
     * Remove container
     */
    void remove() {
        logger.lifecycle("Container[${name}]: Removing")
        def response = client.rm(id)

        if (!response.status.success) {
            throw new IllegalStateException("docker rm failed: ${response.status.text}")
        }

        id = null
        imageId = null
        running = false
        exists = false
    }


    /**
     * Reload container
     */
    void reload(String reason) {
        logger.lifecycle("Container[${name}]: Reloading, ${reason}")
        inspect()

        if (running) {
            client.stop(id)
        }
        if (exists) {
            client.rm(id)
        }
        create()
        start()
    }

    /**
     * Verify container is present, and create if not
     * Note: does not check configuration matches
     *
     * @return changed
     */
    boolean present() {
        logger.info "Container[{}].present()", name

        if(!inspect()) {
            create()
            return true
        }

        logger.info "Container[{}]: already present.", name

        return false
    }

    /**
     * Verify container is present and running, and create and start if not
     * Note: does not check configuration matches
     *
     * @return changed
     */
    boolean started() {
        logger.info "Container[{}].started()", name

        boolean changed = false

        if(!inspect()) {
            create()
            changed = true
        }

        if (!running) {
            start()
            changed = true
        }

        if (!changed) {
            logger.info "Container[{}]: already running.", name
        }

        return changed
    }

    /**
     * Verify container is present, running and configuration matches, and
     * stop, remove, create and start if not
     *
     * @return changed
     */
    boolean reloaded() {
        logger.info "Container[{}].reloaded()", name
        def current = inspect()

        if(!current) {
            reload("Container does not exist")
            return true
        }

        if(!running) {
            reload("Container is not running")
            return true
        }

        def response = client.inspectImage(imageName)

        if (!response.status.success) {
            reload("Image does not exist locally, new pull request")
            return true
        }
        def image = response.content

        // Image (by identifier for newer image versions with same tag)
        if(current.Image != image.Id) {
            reload("Image identifiers differ: ${current.Image} != ${image.Id}")
            return true
        }

        // Exposed Ports
        def expectedExposed = (image.ContainerConfig.ExposedPorts ?: [:]).keySet() +
                              (config.ExposedPorts ?: [:]).keySet()
        def currentExposed = (current.Config.ExposedPorts ?: [:]).keySet()
        if (currentExposed != expectedExposed) {
            reload("Exposed ports do not match: ${currentExposed} != ${expectedExposed}")
            return true
        }

        // Volumes
        def expectedVolumes = (image.ContainerConfig.Volumes ?: [:]).keySet() +
                              (config.Volumes ?: [:]).keySet()
        def currentVolumes = (current.Config.Volumes ?: [:]).keySet()
        if (currentVolumes != expectedVolumes) {
            reload("Volumes do not match: ${currentVolumes} != ${expectedVolumes}")
            return true
        }

        // Environment
        def expectedEnv = splitEnv((Collection<String>)image.ContainerConfig.Env) +
                          splitEnv((Collection<String>)config.Env)
        def currentEnv = splitEnv((Collection<String>)current.Config.Env)
        if (currentEnv != expectedEnv) {
            reload("Env does not match: ${currentEnv} != ${expectedEnv}")
            return true
        }

        // -- Host Configuration

        // Binds
        if (current.HostConfig.Binds != config.HostConfig.Binds) {
            reload("Binds do not match: ${current.HostConfig.Binds} != " +
                   "${config.HostConfig.Binds}")
            return true
        }

        // Port Bindings
        if (current.HostConfig.PortBindings != config.HostConfig.PortBindings) {
            reload("Port Bindings do not match: ${current.HostConfig.PortBindings} != " +
                   "${config.HostConfig.PortBindings}")
            return true
        }

        // Links
        if (current.HostConfig.Links != config.HostConfig.Links) {
            reload("Links do not match: ${current.HostConfig.Links} != " +
                   "${config.HostConfig.Links}")
            return true
        }

        // Privileged
        if (current.HostConfig.Privileged != config.HostConfig.Privileged) {
            reload("Privileged does not match: ${current.HostConfig.Privileged} != " +
                   "${config.HostConfig.Privileged}")
            return true
        }

        // ExtraHosts
        if (current.HostConfig.ExtraHosts != config.HostConfig.ExtraHosts) {
            reload("ExtraHosts do not match: ${current.HostConfig.ExtraHosts} != " +
                   "${config.HostConfig.ExtraHosts}")
            return true
        }

        logger.info "Container[{}]: is up-to-date and running, no reload necessary.", name
        return false
    }

    /**
     * Unconditionally restart or start the container
     *
     * @return changed (always true)
     */
    boolean restarted() {
        reload("Unconditional restart")
        return true
    }

    /**
     * Stops the container if it exists and is running
     *
     * @return changed
     */
    boolean stopped() {
        if (!inspect()) {
            logger.info "Container[{}]: does not exist, no stop necessary.", name
            return false
        }

        if (running) {
            stop()
            return true
        }

        logger.info "Container[{}]: already stopped.", name
        return false
    }

    /**
     * Stops and removes the container if it exists
     *
     * @return changed
     */
    boolean absent() {
        if (!inspect()) {
            logger.info "Container[{}]: does not exist, no removal necessary.", name
            return false
        }

        if (running) {
            stop()
        }
        remove()

        return true
    }

    /**
     * Split a list of environment variables ("name=value") to a map
     *
     * @param list of environment variables
     * @return map
     */
    private static Map<String,String> splitEnv(Collection<String> env) {
        if (!env) {
            return [:]
        }

        env.collectEntries { e->
            def parts = e.tokenize("=")
            [parts[0], parts[1]]
        }
    }
}
