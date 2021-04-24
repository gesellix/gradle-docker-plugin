package de.gesellix.gradle.docker.tasks

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.authentication.AuthConfig
import de.gesellix.docker.client.image.BuildConfig
import de.gesellix.docker.client.image.BuildResult
import de.gesellix.gradle.docker.worker.BuildcontextArchiver
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import spock.lang.Specification

class DockerBuildTaskSpec extends Specification {

  def project
  def task
  def dockerClient = Mock(DockerClient)

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.task('dockerBuild', type: DockerBuildTask)
    task.dockerClient = dockerClient
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
    1 * dockerClient.build(*_) >> new BuildResult(imageId: "4711")
  }

  def "delegates to dockerClient with buildContext"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName"]
    Map<String, Object> options = [:]

    given:
    task.buildContext = inputStream
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711")

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildParams"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName", dockerfile: './custom.Dockerfile']
    Map<String, Object> options = [:]

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: true, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711")

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildOptions"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName"]
    Map<String, Object> options = [EncodedRegistryConfig: "base-64"]

    given:
    task.buildContext = inputStream
    task.buildOptions = [EncodedRegistryConfig: "base-64"]
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711")

    and:
    task.outputs.files.isEmpty()
  }

  def "does not override rm build param if given"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: false, t: "imageName", dockerfile: './custom.Dockerfile']
    Map<String, Object> options = [:]

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: false, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711")

    and:
    task.outputs.files.isEmpty()
  }

  def "uses auth configs if not overridden via build options"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName"]
    Map<String, AuthConfig> authConfigs = ["host.name": new AuthConfig(username: "user-name", password: "a secret")]
    Map<String, Object> options = [EncodedRegistryConfig: "encoded-auth"]

    given:
    task.authConfigs = authConfigs
    dockerClient.encodeAuthConfigs(authConfigs) >> "encoded-auth"
    task.buildContext = inputStream
    task.imageName = "imageName"

    when:
    task.build()

    then:
    1 * dockerClient.build(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711")

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext (with logs)"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName"]
    Map<String, Object> options = [:]

    given:
    task.buildContext = inputStream
    task.imageName = "imageName"
    task.enableBuildLog = true

    when:
    task.build()

    then:
    1 * dockerClient.buildWithLogs(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711", log: [])

    and:
    task.outputs.files.isEmpty()
  }

  def "delegates to dockerClient with buildContext and buildParams (with logs)"() {
    def inputStream = new FileInputStream(File.createTempFile("docker", "test"))
    Map<String, Object> query = [rm: true, t: "imageName", dockerfile: './custom.Dockerfile']
    Map<String, Object> options = [:]

    given:
    task.buildContext = inputStream
    task.buildParams = [rm: true, dockerfile: './custom.Dockerfile']
    task.imageName = "imageName"
    task.enableBuildLog = true

    when:
    task.build()

    then:
    1 * dockerClient.buildWithLogs(inputStream, new BuildConfig(query: query, options: options)) >> new BuildResult(imageId: "4711", log: [])

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
