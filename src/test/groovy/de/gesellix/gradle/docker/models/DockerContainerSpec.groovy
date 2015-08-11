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
        container.reloaded()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ]]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [ Id: "image0" ]
        ]
        1 * container.reload(_) >> { String msg ->
            println msg
            assert msg.startsWith("Image identifiers differ")
        }
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
        container.reloaded()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ],
                        Config: [
                                ExposedPorts: [
                                        "8080/tcp": []
                                ]]]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [ Id: "image1", ContainerConfig: [ ExposedPorts: [] ] ]
        ]
        1 * container.reload(_) >> { String msg ->
            println msg
            assert msg.startsWith("Exposed ports do not match")
        }
    }

    def "reloaded w/ different volumes"() {
        given:
        def container = Spy(DockerContainer, constructorArgs: [
                dockerClient,
                "example",
                "testImage:latest",
                [
                        ExposedPorts: [
                                "8080/tcp" : []
                        ],
                        Volumes: [ "/data" : [] ]
                ]
        ])

        when:
        container.reloaded()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ],
                        Config: [
                                ExposedPorts: [ "8080/tcp": [] ]
                        ]]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [ Id: "image1", ContainerConfig: [ ExposedPorts: [] ] ]
        ]
        1 * container.reload(_) >> { String msg ->
            println msg
            assert msg.startsWith("Volumes do not match")
        }
    }

    def "reloaded w/ different envs"() {
        given:
        def container = Spy(DockerContainer, constructorArgs: [
                dockerClient,
                "example",
                "testImage:latest",
                [
                        ExposedPorts: [
                                "8080/tcp" : []
                        ],
                        Volumes: [ "/data" : [] ],
                        Env: [ "TMP=1" ]
                ]
        ])

        when:
        container.reloaded()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ],
                        Config: [
                                ExposedPorts: [ "8080/tcp": [] ],
                                Volumes: [
                                        "/data": [],
                                        "/spec": []
                                ],
                                Env: [ "TMP=1" ]
                        ]]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [
                        Id: "image1",
                        ContainerConfig: [
                                ExposedPorts: [],
                                Volumes: [ "/spec": [] ],
                                Env: [ "MYVAR=myval" ]
                        ]
                ]
        ]
        1 * container.reload(_) >> { String msg ->
            println msg
            assert msg.startsWith("Env does not match")
        }
    }

    def "reloaded w/ everything same"() {
        given:
        def container = Spy(DockerContainer, constructorArgs: [
                dockerClient,
                "example",
                "testImage:latest",
                [
                        ExposedPorts: [
                                "8080/tcp" : []
                        ],
                        Volumes: [ "/data" : [] ],
                        Env: [ "TMP=1" ],
                        HostConfig: [
                                Binds: [ "/data:/data" ]
                        ]
                ]
        ])

        when:
        def changed = container.reloaded()

        then:
        1 * dockerClient.ps([filters: [name: ["example"]]]) >> [
                content: [[ Names: [ "/example" ], Id: "123" ]]
        ]
        1 * dockerClient.inspectContainer("123") >> [
                content: [
                        Image: "image1",
                        State: [
                                Running: true
                        ],
                        Config: [
                                ExposedPorts: [ "8080/tcp": [] ],
                                Volumes: [
                                        "/data": [],
                                        "/spec": []
                                ],
                                Env: [ "TMP=1", "MYVAR=myval" ],
                        ],
                        HostConfig: [
                                Binds: [ "/data:/data" ]
                        ]]
        ]
        1 * dockerClient.inspectImage("testImage:latest") >> [
                status: [ success: true ],
                content: [
                        Id: "image1",
                        ContainerConfig: [
                                ExposedPorts: [],
                                Volumes: [ "/spec": [] ],
                                Env: [ "MYVAR=myval" ]
                        ]
                ]
        ]
        0 * container.reload(_)

        and:
        changed == false
    }
}
