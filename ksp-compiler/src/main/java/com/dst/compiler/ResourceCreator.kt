package com.dst.compiler

import com.dst.rpc.RPCInterfaceRegistry
import com.dst.rpc.RPCollector
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * @author liuzhongao
 * @since 2024/3/9 16:53
 */
internal object ResourceCreator {

    fun resourceCreate(
        codeGenerator: CodeGenerator,
        annotatedClassDeclaredElementList: List<KSClassDeclaration>
    ) {
        if (annotatedClassDeclaredElementList.isEmpty()) {
            return
        }
        val firstClassDeclaration = annotatedClassDeclaredElementList.first()
        val packageName = firstClassDeclaration.packageName.asString()
        val generatedClassName = "${RPCollector::class.java.simpleName}_${Integer.toHexString(firstClassDeclaration.simpleName.hashCode())}"
        val collectorWriter = codeGenerator.createNewFile(
            dependencies = Dependencies(false, requireNotNull(firstClassDeclaration.containingFile)),
            packageName = packageName,
            fileName = generatedClassName
        ).bufferedWriter()

        collectorWriter.appendLine("package $packageName")
            .appendLine()
            .appendLine("internal class $generatedClassName : ${RPCollector::class.java.name} {")
            .appendLine()
            .appendLine("\toverride fun collect(registry: ${RPCInterfaceRegistry::class.java.name}) {")
            .apply {
                annotatedClassDeclaredElementList.forEachIndexed { index, ksClassDeclaration ->
                    if (index != 0) {
                        appendLine()
                    }
                    val annotatedClassPackageName = ksClassDeclaration.packageName.asString()
                    appendLine("\t\tregistry.putServiceProxyLazy(${annotatedClassPackageName}.${ksClassDeclaration.simpleName.asString()}::class.java) { source, remote, handler ->")
                        .appendLine("\t\t\t${annotatedClassPackageName}.${ksClassDeclaration}_Generated_Proxy(sourceAddress = source, remoteAddress = remote, exceptionHandler = handler)")
                        .appendLine("\t\t}")
                    appendLine("\t\tregistry.putServiceStubLazy(${annotatedClassPackageName}.${ksClassDeclaration.simpleName.asString()}::class.java) { impl ->")
                        .appendLine("\t\t\t${annotatedClassPackageName}.${ksClassDeclaration}_Generated_Stub(implementationInstance = impl)")
                        .appendLine("\t\t}")
                }
            }
            .appendLine("\t}")
            .appendLine("}")
        collectorWriter.flush()
        collectorWriter.close()

        val resourceWriter = codeGenerator.createNewFileByPath(
            dependencies = Dependencies(false),
            path = "META-INF/services/${RPCollector::class.java.name}",
            extensionName = ""
        ).bufferedWriter()
        resourceWriter.appendLine("${packageName}.${generatedClassName}")
        resourceWriter.flush()
        resourceWriter.close()
    }
}