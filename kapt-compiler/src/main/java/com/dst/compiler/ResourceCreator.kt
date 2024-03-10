package com.dst.compiler

import com.dst.rpc.ExceptionHandler
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
            .appendLine("import ${Override::class.java.name};")
            .appendLine("import ${RPCollector::class.java.name};")
            .appendLine("import ${NotNull::class.java.name};")
            .appendLine("import ${RPCInterfaceRegistry::class.java.name};")
            .appendLine("import ${ExceptionHandler::class.java.name};")
            .appendLine("import ${StubFunction::class.java.name};")
            .appendLine()
            .appendLine("public class $generatedClassName implements RPCollector {")
            .appendLine()
            .appendLine("\t@Override")
            .appendLine("\tpublic void collect(@NotNull RPCInterfaceRegistry registry) {")
            .apply {
                annotatedClassDeclaredElementList.forEachIndexed { index, typeElement ->
                    if (index != 0) {
                        appendLine()
                    }
                    appendLine("\t\tregistry.putServiceProxyLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function3<RPCAddress, RPCAddress, ExceptionHandler, ${typeElement.qualifiedName}>() {")
                        .appendLine("\t\t\t@Override")
                        .appendLine("\t\t\tpublic ${typeElement.qualifiedName} invoke(@NotNull RPCAddress sourceAddress, @NotNull RPCAddress remoteAddress, @NotNull ExceptionHandler exceptionHandler) {")
                        .appendLine("\t\t\t\treturn new ${typeElement.qualifiedName}_Generated_Proxy(sourceAddress, remoteAddress, exceptionHandler);")
                        .appendLine("\t\t\t}")
                        .appendLine("\t\t});")
                    appendLine("\t\tregistry.putServiceStubLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function1<${typeElement.qualifiedName}, StubFunction>() {")
                        .appendLine("\t\t\t@${Override::class.java.name}")
                        .appendLine("\t\t\tpublic StubFunction invoke(@NotNull ${typeElement.qualifiedName} impl) {")
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