/*
 * Copyright 2018 Adrian Rumpold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dslab.gradle.ecore.tasks

import groovy.text.XmlTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

/**
 * Generates an Ant build script to generate Java source code from Ecore models.
 * <p/>
 * The task discovers pairs of Ecore models and genmodels, extracts the necessary
 * metadata (such as namespaces and package names), and produces a buildscript
 * for the EMF Ecore2Java Ant task.
 * <p/>
 * For details on the Ant generator tasks provided by Eclipse EMF, refer to the
 * following Javadoc pages:
 * <ul>
 *   <li><a href="http://download.eclipse.org/modeling/emf/emf/javadoc/2.9.0/index.html?org/eclipse/emf/ant/taskdefs/codegen/ecore/GeneratorTask.html">GeneratorTask</a></li>
 *   <li><a href="http://download.eclipse.org/modeling/emf/emf/javadoc/2.9.0/index.html?org/eclipse/emf/importer/ecore/taskdefs/EcoreGeneratorTask.html">EcoreGeneratorTask</a></li>
 * </ul>
 *
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class GenerateBuildDescriptorTask extends DefaultTask {
    @OutputFile
    final RegularFileProperty descriptor = newOutputFile()

    @InputDirectory
    final DirectoryProperty modelPath = newInputDirectory()

    FileCollection models
    FileCollection genmodels

    private final LogLevel level = LogLevel.DEBUG

    @TaskAction
    def generateDescriptor() {
        discoverModels()

        logger.log(level, "----- generateDescriptor() -----")
        logger.log(level, "Output file: {}", descriptor)
        logger.log(level, "Ecore models: {}", models)
        logger.log(level, "Genmodel: {}", genmodels)
        logger.log(level, '-' * 40)

        models.forEach { model ->
            // Find corresponding Genmodel
            final def candidates = owner.genmodels.filter {
                it.name.equals(model.name.replace(".ecore", ".genmodel"))
            }
            if (candidates.isEmpty()) {
                // TODO: Error handling
                return
            }
            final def genmodel = candidates.first()
            owner.writeDescriptor(model, genmodel)
        }
    }

    void writeDescriptor(File model, File genmodel) {
        // Extract model metadata from Ecore model
        def modelXml = new XmlSlurper().parse(model)
        def nsUri = modelXml.@nsURI
        def nsPrefix = modelXml.@name

        // Extract Java package information from Genmodel
        def gmXml = new XmlSlurper().parse(genmodel)
        def pkgName = gmXml.genPackages.@basePackage

        def modelBase = Paths.get(modelPath.asFile.get().toURI())

        // Data binding for template rendering
        def binding = [
                project: project,
                package: [
                        nsUri   : nsUri,
                        nsPrefix: nsPrefix,
                        name    : pkgName,
                ],
                model  : [
                        ecore   : modelBase.relativize(Paths.get(model.toURI())),
                        genmodel: modelBase.relativize(Paths.get(genmodel.toURI())),
                ]
        ]

        def template = getClass().getResource("/templates/build.xml")
        def engine = new XmlTemplateEngine().createTemplate(template)
        def rendered = engine.make(binding)
        descriptor.asFile.get().withWriter {
            it.write(rendered)
        }
    }

    private void discoverModels() {
        final FileTree tree = modelPath.get().asFileTree
        models = tree.filter { it.name.endsWith ".ecore" }
        genmodels = tree.filter { it.name.endsWith ".genmodel" }
    }
}
