package de.gesellix.gradle.docker.tasks

import de.gesellix.gradle.docker.models.DockerContainer
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class DockerContainerTask extends GenericDockerTask {

  enum State {

    PRESENT     // Ensures container exists
    , STARTED   // Ensures container exists and is running
    , RELOADED  // Ensures container is running and restarted if any configuration is out of date
    , RESTARTED // Ensures container is unconditionally restarted (or started)
    , STOPPED   // Ensures container is stopped
    , ABSENT    // Ensures container is stopped and removed
  }

  @Input
  State targetState = State.STARTED

  @Input
  String image

  @Input
  String containerName

  @Input
  @Optional
  String tag = ""

  /**
   * Publish a container᾿s port to the host
   * format: ip:hostPort:containerPort | ip::containerPort | hostPort:containerPort | containerPort
   */
  @Input
  @Optional
  def ports = []

  /**
   * A list of environment variables in the form of VAR=value
   */
  @Input
  @Optional
  List<String> env = []

  /**
   * A list of environment variable keys to be ignored when searching changes.
   */
  @Input
  @Optional
  List<String> ignoredEnvKeys = []

  /**
   * A command to run specified as an array of strings.
   */
  @Input
  @Optional
  List<String> cmd = null

  /**
   * An entry point for the container as an array of strings
   */
  @Input
  @Optional
  List<String> entrypoint = null

  /**
   * A list of links for the container. Each link entry should be of of the form
   * "container_name:alias".
   */
  @Input
  @Optional
  def links = [:]

  /**
   * Create a bind mount with: [host-dir]:[container-dir]:[rw|ro].
   * If "container-dir" is missing, then docker creates a new volume.
   */
  @Input
  @Optional
  def volumes = null

  /**
   * Gives the container full access to the host. Specified as a boolean value.
   */
  @Input
  @Optional
  Boolean privileged = false

  /**
   * A list of blocking health checks to run against the container.
   *
   * [
   *   containerPort: container port, i.e. "80" or "80/tcp",
   *   type: http or tcp (default is tcp),
   *   timeout: timeout in seconds (default is 5)
   *   retries: number of retries (default is 15 tries)
   *   interval: interval between retries in seconds (default is 2)
   *   path: path element for http request
   * ]
   */
  @Input
  @Optional
  def healthChecks = []

  /**
   * A list of ["hostname:IP"] mappings to be added to the container's /etc/hosts file.
   */
  @Input
  @Optional
  def extraHosts = []

  @Internal
  boolean changed = false

  @Internal
  DockerContainer container

  DockerContainerTask() {
    description = "Manage the lifecycle of docker container"
    group = "Docker"

    outputs.upToDateWhen { t -> t.checkIfUpToDate() }
  }

  /**
   * Check if task is up-to-date
   *
   * @return if task is up to date
   */
  boolean checkIfUpToDate() {
    if (!containerName) {
      throw new GradleException("containerName is mandatory")
    }

    container = new DockerContainer(getDockerClient(), containerName, image, createConfig(), getIgnoredEnvKeys())

    switch (targetState) {
      case State.PRESENT:
        changed = !container.isPresent()
        break
      case State.STARTED:
        changed = !container.isStarted()
        if (!changed) {
          try {
            doHealthChecks()
          }
          catch (Exception e) {
            throw new GradleException("UpToDate check failed", e)
          }
        }
        break
      case State.RELOADED:
        changed = !container.isReloaded()
        if (!changed) {
          try {
            doHealthChecks()
          }
          catch (Exception e) {
            throw new GradleException("UpToDate check failed", e)
          }
        }
        break
      case State.RESTARTED:
        changed = true
        break
      case State.STOPPED:
        changed = !container.isStopped()
        break
      case State.ABSENT:
        changed = !container.isAbsent()
        break
    }

    return !changed
  }

  @TaskAction
  def run() {
    if (!containerName) {
      throw new GradleException("containerName is mandatory")
    }

    container = new DockerContainer(getDockerClient(), containerName, image, createConfig(), getIgnoredEnvKeys())

    switch (targetState) {
      case State.PRESENT:
        changed = container.ensurePresent()
        break
      case State.STARTED:
        changed = container.ensureStarted()
        doHealthChecks()
        break
      case State.RELOADED:
        changed = container.ensureReloaded()
        doHealthChecks()
        break
      case State.RESTARTED:
        changed = container.ensureRestarted()
        doHealthChecks()
        break
      case State.STOPPED:
        changed = container.ensureStopped()
        break
      case State.ABSENT:
        changed = container.ensureAbsent()
        break
    }

    return !changed
  }

  Map createConfig() {
    def config = [:]

    config.Image = image
    config.HostConfig = [:]

    if (env) {
      config.Env = env
    }

    if (cmd) {
      config.Cmd = cmd
    }

    if (entrypoint) {
      config.Entrypoint = entrypoint
    }

    if (links) {
      config.HostConfig.Links = links
    }

    if (getPrivileged() == null) {
      setPrivileged(false)
    }
    config.HostConfig.Privileged = getPrivileged()

    if (ports) {
      config.ExposedPorts = [:]
      config.HostConfig.PortBindings = config.HostConfig.PortBindings ?: [:]
      ports.each { p ->
        ArrayList<String> parts = p.tokenize(":")
        String container_port = parts.last().toString()
        if (container_port.toString().indexOf("/") == -1) {
          container_port = container_port.toString() + "/tcp"
        }

        config.ExposedPorts[container_port] = [:]

        if (config.HostConfig.PortBindings[container_port] == null) {
          config.HostConfig.PortBindings[container_port] = []
        }

        if (parts.size() == 1) {
          config.HostConfig.PortBindings[container_port] << [HostIp: "0.0.0.0", HostPort: ""]
        }
        else if (parts.size() == 2) {
          config.HostConfig.PortBindings[container_port] << [HostIp  : "0.0.0.0",
                                                             HostPort: parts.get(0).toString()]
        }
        else if (parts.size() == 3) {
          if (parts.get(1)) {
            config.HostConfig.PortBindings[container_port] << [HostIp  : parts.get(1).toString(),
                                                               HostPort: parts.get(0).toString()]
          }
          else {
            config.HostConfig.PortBindings[container_port] << [HostIp  : "0.0.0.0",
                                                               HostPort: parts.get(0).toString()]
          }
        }
      }
    }

    if (volumes) {
      config.Volumes = [:]
      config.HostConfig.Binds = []
      volumes.each { String v ->
        config.Volumes[getMountTarget(v)] = [:]
        if (isHostMount(v)) {
          config.HostConfig.Binds << v
        }
      }
    }

    if (extraHosts) {
      config.HostConfig.ExtraHosts = extraHosts
    }

    return config
  }

  String getMountTarget(String volume) {
    List<String> parts = tokenizeVolume(volume)
    return parts.size() == 1 ? parts.get(0) : parts.get(1)
  }

  String isHostMount(String volume) {
    return tokenizeVolume(volume).size() != 1
  }

  List<String> tokenizeVolume(String volume) {
    def normalized = isWindowsDrive(volume) ? volume.substring(2, volume.length()) : volume
    def parts = normalized.tokenize(":")
    if (parts.size() > 3) {
      throw new IllegalArgumentException("Malformed volume string: ${volume}")
    }
    if (parts.size() == 3) {
      if (!(parts.get(2) in ['ro'])) {
        throw new IllegalArgumentException('any third argument in bind must be "ro"')
      }
    }
    return parts
  }

  boolean isWindowsDrive(String source) {
    if (!source || source.length() < 2) {
      return false
    }
    return source.charAt(0).isLetter() && source.charAt(1) == (char) ':'
  }

  def doHealthChecks() {
    if (healthChecks.isEmpty()) {
      return false
    }

    String containerHost = new URI(dockerHost).host

    logger.info "Running Health checks on host ${containerHost}"

    def current = container.inspect()

    if (!container.running) {
      throw new IllegalStateException("HealthCheck: Container is not running.")
    }

    healthChecks.each { healthCheck ->
      if (!healthCheck.containerPort) {
        throw new IllegalArgumentException("ContainerPort is required.")
      }

      if (healthCheck.containerPort.toString().indexOf("/") == -1) {
        healthCheck.containerPort = "${healthCheck.containerPort}/tcp"
      }

      healthCheck.type = healthCheck.type ?: "tcp"
      healthCheck.timeout = healthCheck.timeout ?: 5
      healthCheck.retries = healthCheck.retries ?: 15
      healthCheck.interval = healthCheck.interval ?: 2
      healthCheck.path = healthCheck.path ?: "/"

      def portBinding = current.NetworkSettings.Ports[(String) healthCheck.containerPort]
      if (!portBinding) {
        throw new IllegalArgumentException("Port \"${healthCheck.containerPort}\" is not bound to host.")
      }

      String host = containerHost
      if (!host) {
        host = portBinding[0].HostIp
      }
      int port = portBinding[0].HostPort.toInteger()

      int counter = 0

      switch (healthCheck.type) {
        case "tcp":
          def address = new InetSocketAddress(host, port)
          logger.info "HealthCheck/tcp: Connecting ${address}, (timeout ${healthCheck.timeout}, retries ${healthCheck.retries}, interval ${healthCheck.interval})"
          while (counter < (int) healthCheck.retries) {
            try {
              def socket = new Socket()
              socket.connect(address, (int) healthCheck.timeout)
              logger.info "HealthCheck/tcp: Container is healthy."
              socket.close()
              return true
            }
            catch (SocketTimeoutException ignored) {
            }
            catch (IOException ignored) {
            }

            counter = counter + 1
            sleep((int) healthCheck.interval * 1000)
            logger.info "Attempt #${counter + 1}"
          }

          logger.error "container unhealthy after ${(int) healthCheck.retries} checks"
          break
        case "http":
          def url = new URL("http", host, port, (String) healthCheck.path)
          logger.info "HealthCheck/http: Connecting ${url}, (timeout ${healthCheck.timeout}, retries ${healthCheck.retries}, interval ${healthCheck.interval})"
          while (counter < (int) healthCheck.retries) {
            URLConnection connection = null
            try {
              connection = url.openConnection()
              connection.setConnectTimeout(((int) healthCheck.timeout * 1000))
              connection.connect()
              if (((int) (connection.getResponseCode() / 100)) == 2) {
                logger.info "HealthCheck/http: Container is healthy: ${connection.getResponseCode()}"
                return true
              }
            }
            catch (SocketTimeoutException ignored) {
            }
            catch (IOException ignored) {
            }
            finally {
              if (connection) {
                connection.disconnect()
              }
            }

            counter = counter + 1
            sleep((int) healthCheck.interval * 1000)
            logger.info "Attempt #${counter + 1}"
          }

          logger.error "container unhealthy after ${(int) healthCheck.retries} checks"
          break
        default:
          throw new IllegalArgumentException("Unsupported healthcheck type: ${healthCheck.type}")
      }

      throw new IllegalStateException("HealthCheck: Container not healthy.")
    }
  }
}
