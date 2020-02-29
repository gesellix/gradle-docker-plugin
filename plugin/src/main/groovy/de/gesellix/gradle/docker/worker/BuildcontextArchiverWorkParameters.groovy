package de.gesellix.gradle.docker.worker

import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkParameters

interface BuildcontextArchiverWorkParameters extends WorkParameters {

    RegularFileProperty getSourceDirectory()

    RegularFileProperty getArchivedTargetFile()
}
