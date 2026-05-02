package io.github.gaming32.bingo.buildscript

import net.fabricmc.classtweaker.api.ClassTweakerReader
import net.fabricmc.classtweaker.api.visitor.AccessWidenerVisitor
import net.fabricmc.classtweaker.api.visitor.ClassTweakerVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class ConvertClassTweakerTask : DefaultTask() {
    @get:InputFile
    abstract val classTweakerFile: RegularFileProperty

    @get:OutputFile
    abstract val accessTransformerFile: RegularFileProperty

    @TaskAction
    fun run() {
        val accessTransformer = StringBuilder()

        val visitor = object : ClassTweakerVisitor {
            override fun visitAccessWidener(owner: String) = object : AccessWidenerVisitor {
                override fun visitClass(
                    access: AccessWidenerVisitor.AccessType,
                    transitive: Boolean
                ) {
                    val atAccess = when (access) {
                        AccessWidenerVisitor.AccessType.ACCESSIBLE -> "public"
                        AccessWidenerVisitor.AccessType.EXTENDABLE -> "public-f"
                        AccessWidenerVisitor.AccessType.MUTABLE -> "public-f"
                    }
                    accessTransformer.append(atAccess).append(' ')
                        .append(owner.replace('/', '.')).append('\n')
                }

                override fun visitMethod(
                    name: String,
                    descriptor: String,
                    access: AccessWidenerVisitor.AccessType,
                    transitive: Boolean
                ) {
                    val atAccess = when (access) {
                        AccessWidenerVisitor.AccessType.ACCESSIBLE -> "public"
                        AccessWidenerVisitor.AccessType.EXTENDABLE -> "protected-f"
                        AccessWidenerVisitor.AccessType.MUTABLE -> "private-f"
                    }
                    accessTransformer.append(atAccess).append(' ')
                        .append(owner.replace('/', '.')).append(' ')
                        .append(name).append(descriptor).append('\n')
                }

                override fun visitField(
                    name: String,
                    descriptor: String,
                    access: AccessWidenerVisitor.AccessType,
                    transitive: Boolean
                ) {
                    val atAccess = when (access) {
                        AccessWidenerVisitor.AccessType.ACCESSIBLE -> "public"
                        AccessWidenerVisitor.AccessType.EXTENDABLE -> "protected"
                        AccessWidenerVisitor.AccessType.MUTABLE -> "private-f"
                    }
                    accessTransformer.append(atAccess).append(' ')
                        .append(owner.replace('/', '.')).append(' ')
                        .append(name).append('\n')
                }
            }
        }

        classTweakerFile.get().asFile.bufferedReader().use { reader ->
            ClassTweakerReader.create(visitor).read(reader)
        }

        accessTransformerFile.get().asFile.parentFile.mkdirs()
        accessTransformerFile.get().asFile.writeText(accessTransformer.toString())
    }
}
