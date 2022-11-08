package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.authentication.AuthConfig
import de.gesellix.docker.client.DockerClient
import de.gesellix.gradle.docker.worker.BuildcontextArchiver
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.temporal.ChronoUnit

class DockerBuildTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.register('dockerBuild', DockerBuildTask).get()
    task.dockerClient = dockerClient
    task.buildTimeout = Duration.of(1, ChronoUnit.SECONDS)
  }

  def "should archive the buildcontext in a worker thread"() {
    URL dockerfile = getClass().getResource('/docker/Dockerfile')
    def baseDir = new File(dockerfile.toURI()).parentFile

    given:
    def buildTaskDependency = project.task('buildTaskDependency', type: TestTask)
    task.dependsOn buildTaskDependency
    task.buildContextDirectory = baseDir
    task.imageName = "busybox"
    def workerExecutor = Mock(WorkerExecutor)
    def workQueue = Mock(WorkQueue)
    task.workerExecutor = workerExecutor

    when:
    task.build()

    then:
    project.tasks.findByName("dockerBuild").getDependsOn().contains project.tasks.findByName("buildTaskDependency")
    and:
    1 * workerExecutor.noIsolation() >> workQueue
    1 * workQueue.submit(BuildcontextArchiver, _) >> { task.targetFile = new File(dockerfile.toURI()) }
    1 * workerExecutor.await()
    and:
    1 * dockerClient.build(*_)
  }

  def "delegates to dockerClient with buildContext"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           null, "imageName",
                           null, null, null, true,
                           null, null, null,
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildParams"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.buildParams = [
        buildargs : [AN_ARGUMENT: "a value"],
        dockerfile: './custom.Dockerfile',
        nocache   : true,
        pull      : "true",
        quiet     : true,
        rm        : false,
    ]
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           "./custom.Dockerfile",
                           "imageName",
                           true,
                           true,
                           "true",
                           false,
                           '{"AN_ARGUMENT":"a value"}',
                           null,
                           null,
                           null,
                           inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildOptions"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.buildOptions = [EncodedRegistryConfig: "base-64"]
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           null, "imageName",
                           null, null, null, true,
                           null, null, "base-64",
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "does not override rm build param if given"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: false, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           "./custom.Dockerfile", "imageName",
                           null, null, null, false,
                           null, null, null,
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  @Unroll
  def "should accept boolean for 'pull' build param"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: false, pull: pull, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           "./custom.Dockerfile", "imageName",
                           null, null, pull.toString(), false,
                           null, null, null,
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()

    where:
    pull << [true, false]
  }

  def "uses auth configs if not overridden via build options"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, AuthConfig> authConfigs = ["host.name": new AuthConfig(username: "user-name", password: "a secret")]

    given:
    task.authConfigs = authConfigs
    dockerClient.encodeAuthConfigs(authConfigs) >> "encoded-auth"
    task.buildContext = inputStream
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           null, "imageName",
                           null, null, null, true,
                           null, null, "encoded-auth",
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext (with logs)"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.imageName = "imageName"
    task.enableBuildLog = true

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           null, "imageName",
                           null, null, null, true,
                           null, null, null,
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildParams (with logs)"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: true, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"
    task.enableBuildLog = true

    when:
    task.build()

    then:
    1 * dockerClient.build(_, _,
                           "./custom.Dockerfile", "imageName",
                           null, null, null, true,
                           null, null, null,
                           null, inputStream)

    and:
    task.outputs.files.isEmpty()
  }

  def "normalizedImageName should match [a-z0-9-_.]"() {
    expect:
    task.getNormalizedImageName() ==~ "[a-z0-9-_.]+"
  }

  def parentDir(URL resource) {
    new File(resource.toURI()).parentFile
  }

  def wrapInClosure(value) {
    new Closure(null) {

      @Override
      Object call() {
        value
      }
    }
  }
}
