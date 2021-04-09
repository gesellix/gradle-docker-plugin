package de.gesellix.gradle.docker.models

import de.gesellix.docker.client.DockerClient
import spock.lang.Specification

class DockerContainerSpec extends Specification {

  def dockerClient = Mock(DockerClient)

  def "reloaded w/ different image"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [:]
    ])

    when:
    def upToDate = container.isReloaded()
    container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
        content: [[Names: ["/example"], Id: "123"]]
    ]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image: "image1",
            State: [Running: true]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [Id: "image0"]
    ]
    1 * container.reload(_) >> { String msg ->
      println msg
      assert msg.startsWith("Image identifiers differ")
    }

    and:
    upToDate == false
  }

  def "reloaded w/ different exposed ports"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [:]
    ])

    when:
    def upToDate = container.isReloaded()
    container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
        content: [[Names: ["/example"], Id: "123"]]
    ]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image : "image1",
            State : [Running: true],
            Config: [ExposedPorts: ["8080/tcp": []]]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [Id: "image1", ContainerConfig: [ExposedPorts: []]]
    ]
    1 * container.reload(_) >> { String msg ->
      println msg
      assert msg.startsWith("Exposed ports do not match")
    }

    and:
    upToDate == false
  }

  def "reloaded w/ different cmd"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [Cmd: ["true"]]
    ])

    when:
    def upToDate = container.isReloaded()
    container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [content: [[Names: ["/example"], Id: "123"]]]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image : "image1",
            State : [Running: true],
            Config: [
                ExposedPorts: ["8080/tcp": []],
                Cmd         : ["echo", "false"]
            ]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [
            Id             : "image1",
            ContainerConfig: [ExposedPorts: ["8080/tcp": []]],
            Config         : [
                Entrypoint: "echo",
                Cmd       : "false"
            ]
        ]
    ]
    1 * container.reload(_) >> { String msg ->
      println msg
      assert msg.startsWith("Entrypoints and Cmd do not match")
    }

    and:
    upToDate == false
  }

  def "reloaded w/ different volumes"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [
            ExposedPorts: ["8080/tcp": []],
            Volumes     : ["/data": []]
        ]
    ])

    when:
    def upToDate = container.isReloaded()
    container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [content: [[Names: ["/example"], Id: "123"]]]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image : "image1",
            State : [Running: true],
            Config: [ExposedPorts: ["8080/tcp": []]]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [Id: "image1", ContainerConfig: [ExposedPorts: []]]
    ]
    1 * container.reload(_) >> { String msg ->
      println msg
      assert msg.startsWith("Volumes do not match")
    }

    and:
    upToDate == false
  }

  def "reloaded w/ different envs"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [
            ExposedPorts: ["8080/tcp": []],
            Volumes     : ["/data": []],
            Env         : ["TMP=1"]
        ]
    ])

    when:
    def upToDate = container.isReloaded()
    container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
        content: [[Names: ["/example"], Id: "123"]]
    ]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image : "image1",
            State : [Running: true],
            Config: [
                ExposedPorts: ["8080/tcp": []],
                Volumes     : ["/data": [], "/spec": []],
                Env         : ["TMP=1"]
            ]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [
            Id             : "image1",
            ContainerConfig: [
                ExposedPorts: [],
                Volumes     : ["/spec": []],
                Env         : ["MYVAR=myval"]
            ]
        ]
    ]
    1 * container.reload(_) >> { String msg ->
      println msg
      assert msg.startsWith("Env does not match")
    }

    and:
    upToDate == false
  }

  def "reloaded w/ everything same"() {
    given:
    def container = Spy(DockerContainer, constructorArgs: [
        dockerClient,
        "example",
        "testImage:latest",
        [
            ExposedPorts: ["8080/tcp": []],
            Volumes     : ["/data": []],
            Env         : ["TMP=1"],
            HostConfig  : [Binds: ["/data:/data"]]
        ]
    ])

    when:
    def upToDate = container.isReloaded()
    def changed = container.ensureReloaded()

    then:
    1 * dockerClient.ps([filters: [name: ["example"]]]) >> [content: [[Names: ["/example"], Id: "123"]]]
    2 * dockerClient.inspectContainer("123") >> [
        content: [
            Image     : "image1",
            State     : [Running: true],
            Config    : [
                ExposedPorts: ["8080/tcp": []],
                Volumes     : ["/data": [], "/spec": []],
                Env         : ["TMP=1", "MYVAR=myval"],
            ],
            HostConfig: [
                Binds: ["/data:/data"]
            ]]
    ]
    2 * dockerClient.inspectImage("testImage:latest") >> [
        status : [success: true],
        content: [
            Id             : "image1",
            ContainerConfig: [
                ExposedPorts: [],
                Volumes     : ["/spec": []],
                Env         : ["MYVAR=myval"]
            ],
            Config         : []
        ]
    ]
    0 * container.reload(_)

    and:
    upToDate == true
    changed == false
  }
}
