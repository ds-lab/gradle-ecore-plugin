package org.dslab.gradle.ecore.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

class EmfCodegenTask extends DefaultTask {
    /**
     * The actual Eclipse binary to be invoked for code generation, relative to {@code eclPath}
     */
    @Internal
    final RegularFileProperty eclipseCommand = newInputFile()

    /**
     * The input directory containing the Ecore model(s)
     */
    @InputDirectory
    @SkipWhenEmpty
    final DirectoryProperty modelDir = newInputDirectory()

    /**
     * The Ant build script used to invoke the Ecore2Java code-gen task
     *
     * By marking it as an explicit dependency, this task will
     * by out-of-date whenever the Ant script changes
     */
    @InputFile
    final RegularFileProperty buildScript = newInputFile()

    /**
     * The target directory for generated code
     */
    @OutputDirectory
    final DirectoryProperty outputDir = newOutputDirectory()

    @TaskAction
    void generate() {
        logger.info("---- Task configuration ----")
        logger.info("eclipseCommand: {}", eclipseCommand)
        logger.info("modelDir: {}", modelDir)
        logger.info("buildScript: {}", buildScript)
        logger.info("outputDir: {}", outputDir)

        assert eclipseCommand.asFile.getOrNull()?.canExecute()

        project.exec {
            workingDir project.projectDir
            commandLine "${eclipseCommand.get()}",
                    '-noSplash',
                    '-data', project.rootDir,
                    '-application', 'org.eclipse.ant.core.antRunner',
                    '-Dmodel.dir="' + modelDir.asFile.get() + '"',
                    '-Doutput.path="' + outputDir.asFile.get() + '"'
        }
    }
}
