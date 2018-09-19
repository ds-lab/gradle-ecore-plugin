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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

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
 *   <li><a href="http://download.eclipse.org/modeling/emf/emf/javadoc/2.11/index.html?org/eclipse/emf/ant/taskdefs/codegen/ecore/GeneratorTask.html">GeneratorTask</a></li>
 *   <li><a href="http://download.eclipse.org/modeling/emf/emf/javadoc/2.11/index.html?org/eclipse/emf/importer/ecore/taskdefs/EcoreGeneratorTask.html">EcoreGeneratorTask</a></li>
 * </ul>
 *
 * @author Adrian Rumpold (a.rumpold@ds-lab.org)
 */
class GenerateBuildDescriptorTask extends DefaultTask {
    @OutputFile
    RegularFileProperty descriptor = newOutputFile()

    @InputDirectory
    DirectoryProperty modelPath = newInputDirectory()

    @Internal
    DirectoryProperty outputDir = newOutputDirectory()

    @Input
    Property<String> reconcileGenmodel

    @Input
    Property<Boolean> removeOSGiDescriptors

    @Internal
    FileCollection models

    @Internal
    FileCollection genmodels

    private final LogLevel level = LogLevel.DEBUG

    GenerateBuildDescriptorTask() {
        reconcileGenmodel = project.objects.property(String)
    }

    @TaskAction
    def generateDescriptor() {
        discoverModels()

        logger.log(level, "----- generateDescriptor() -----")
        logger.log(level, "Output file: {}", descriptor)
        logger.log(level, "Ecore models: {}", models)
        logger.log(level, "Genmodel: {}", genmodels)
        logger.log(level, "reconcileGenmodel: {}", reconcileGenmodel)
        logger.log(level, "removeOSGiDescriptors: {}", removeOSGiDescriptors)
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

        def projectBase = project.projectDir.toPath()

        // Data binding for template rendering
        def binding = [
                project: project,
                package: [
                        nsUri   : nsUri,
                        nsPrefix: nsPrefix,
                        name    : pkgName,
                ],
                model  : [
                        reconcileGenmodel: reconcileGenmodel.get(),
                        ecore            : projectBase.relativize(model.toPath()),
                        genmodel         : projectBase.relativize(genmodel.toPath()),
                ],
                output : [
                        folder: projectBase.relativize(outputDir.asFile.get().toPath()),
                        resourceDir: projectBase.relativize(project.sourceSets.generated.resources.srcDirs[0].toPath())
                ],
                removeOSGiDescriptors: removeOSGiDescriptors.get()
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
