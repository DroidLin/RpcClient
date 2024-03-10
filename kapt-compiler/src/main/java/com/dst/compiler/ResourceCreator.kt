package com.dst.compiler

import com.dst.rpc.ExceptionHandler
import com.dst.rpc.RPCAddress
import com.dst.rpc.StubFunction
import com.dst.rpc.RPCInterfaceRegistry
import com.dst.rpc.RPCollector
import org.jetbrains.annotations.NotNull
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

/**
 * @author liuzhongao
 * @since 2024/3/9 16:53
 */
internal object ResourceCreator {

    fun resourceCreate(
        environment: ProcessingEnvironment,
        annotatedClassDeclaredElementList: List<TypeElement>
    ) {
        if (annotatedClassDeclaredElementList.isEmpty()) {
            return
        }

        val firstClassDeclaration = annotatedClassDeclaredElementList.first()
        val packageName = (firstClassDeclaration.enclosingElement as PackageElement).qualifiedName.toString()
        val generatedClassName = "${RPCollector::class.java.simpleName}_${Integer.toHexString(firstClassDeclaration.simpleName.hashCode())}"

        val collectorWriter = environment.filer.createSourceFile("${packageName}.${generatedClassName}").openWriter()
        collectorWriter.appendLine("package ${packageName};")
            .appendLine()
            .appendLine("public class $generatedClassName implements ${RPCollector::class.java.name} {")
            .appendLine()
            .appendLine("\t@${Override::class.java.name}")
            .appendLine("\tpublic void collect(@${NotNull::class.java.name} ${RPCInterfaceRegistry::class.java.name} registry) {")
            .apply {
                annotatedClassDeclaredElementList.forEachIndexed { index, typeElement ->
                    if (index != 0) {
                        appendLine()
                    }
                    appendLine("\t\tregistry.putServiceProxyLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function3<${RPCAddress::class.java.name}, ${RPCAddress::class.java.name}, ${ExceptionHandler::class.java.name}, ${typeElement.qualifiedName}>() {")
                        .appendLine("\t\t\t@${Override::class.java.name}")
                        .appendLine("\t\t\tpublic ${typeElement.qualifiedName} invoke(@${NotNull::class.java.name} ${RPCAddress::class.java.name} sourceAddress, @${NotNull::class.java.name} ${RPCAddress::class.java.name} remoteAddress, @${NotNull::class.java.name} ${ExceptionHandler::class.java.name} exceptionHandler) {")
                        .appendLine("\t\t\t\treturn new ${typeElement.qualifiedName}_Generated_Proxy(sourceAddress, remoteAddress, exceptionHandler);")
                        .appendLine("\t\t\t}")
                        .appendLine("\t\t});")
                    appendLine("\t\tregistry.putServiceStubLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function1<${typeElement.qualifiedName}, ${StubFunction::class.java.name}>() {")
                        .appendLine("\t\t\t@${Override::class.java.name}")
                        .appendLine("\t\t\tpublic ${StubFunction::class.java.name} invoke(@${NotNull::class.java.name} ${typeElement.qualifiedName} impl) {")
                        .appendLine("\t\t\t\treturn new ${typeElement.qualifiedName}_Generated_Stub(impl);")
                        .appendLine("\t\t\t}")
                        .appendLine("\t\t});")
                }
            }
            .appendLine("\t}")
            .appendLine("}")
        collectorWriter.flush()
        collectorWriter.close()

        val resourceWriter = environment.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/${RPCollector::class.java.name}").openWriter()
        resourceWriter.appendLine("${packageName}.${generatedClassName}")
        resourceWriter.flush()
        resourceWriter.close()
    }
}