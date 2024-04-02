package com.dst.compiler

import com.dst.rpc.InterfaceRegistry
import com.dst.rpc.Collector
import com.dst.rpc.annotations.RPCImplementation
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * @author liuzhongao
 * @since 2024/3/9 16:53
 */
internal object ResourceCreator {

    @OptIn(KspExperimental::class)
    fun resourceCreate(
        logger: KSPLogger,
        codeGenerator: CodeGenerator,
        annotatedClassDeclaredElementList: List<KSClassDeclaration>,
        implementationClassDeclarations: List<KSClassDeclaration>
    ) {
        if (annotatedClassDeclaredElementList.isEmpty() && implementationClassDeclarations.isEmpty()) {
            return
        }
        val firstClassDeclaration = annotatedClassDeclaredElementList.firstOrNull()
            ?: implementationClassDeclarations.firstOrNull()
            ?: return
        val packageName = firstClassDeclaration.packageName.asString()
        val generatedClassName = "${Collector::class.java.simpleName}_${Integer.toBinaryString(firstClassDeclaration.simpleName.hashCode())}"
        val collectorWriter = codeGenerator.createNewFile(
            dependencies = Dependencies(false, requireNotNull(firstClassDeclaration.containingFile)),
            packageName = packageName,
            fileName = generatedClassName
        ).bufferedWriter()

        collectorWriter.appendLine("package $packageName")
            .appendLine()
            .appendLine("internal class $generatedClassName : ${Collector::class.java.name} {")
            .appendLine()
            .appendLine("\toverride fun collect(registry: ${InterfaceRegistry::class.java.name}) {")
            .apply {
                annotatedClassDeclaredElementList.forEachIndexed { index, ksClassDeclaration ->
                    val annotatedClassPackageName = ksClassDeclaration.packageName.asString()
                    appendLine("\t\tregistry.putServiceProxy(${annotatedClassPackageName}.${ksClassDeclaration.simpleName.asString()}::class.java) { source, remote, handler -> ${annotatedClassPackageName}.${ksClassDeclaration}_Generated_Proxy(source, remote, handler) }")
                }
                annotatedClassDeclaredElementList.forEachIndexed { index, ksClassDeclaration ->
                    val annotatedClassPackageName = ksClassDeclaration.packageName.asString()
                    appendLine("\t\tregistry.putServiceStub(${annotatedClassPackageName}.${ksClassDeclaration.simpleName.asString()}::class.java) { impl -> ${annotatedClassPackageName}.${ksClassDeclaration}_Generated_Stub(impl) }")
                }
                implementationClassDeclarations.forEachIndexed { index, ksClassDeclaration ->
                    val implementationAnnotation = ksClassDeclaration.annotations.find { ksAnnotation -> ksAnnotation.shortName.asString() == RPCImplementation::class.java.simpleName }
                    val ksValueAnnotationArgumentValue = implementationAnnotation?.arguments?.find { it.name?.asString() == "clazz" }?.value
                    if (ksValueAnnotationArgumentValue != null && ksValueAnnotationArgumentValue is KSType) {
                        appendLine("\t\tregistry.putServiceImpl(${buildType(ksValueAnnotationArgumentValue)}::class.java, ${ksClassDeclaration.packageName.asString()}.${ksClassDeclaration.simpleName.asString()}())")
                    }
                }
            }
            .appendLine("\t}")
            .appendLine("}")
        collectorWriter.flush()
        collectorWriter.close()

        val resourceWriter = codeGenerator.createNewFileByPath(
            dependencies = Dependencies(false),
            path = "META-INF/services/${Collector::class.java.name}",
            extensionName = ""
        ).bufferedWriter()
        resourceWriter.appendLine("${packageName}.${generatedClassName}")
        resourceWriter.flush()
        resourceWriter.close()
    }
}