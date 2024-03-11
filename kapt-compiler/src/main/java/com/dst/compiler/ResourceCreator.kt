package com.dst.compiler

import com.dst.rpc.Address
import com.dst.rpc.ExceptionHandler
import com.dst.rpc.StubFunction
import com.dst.rpc.RPCInterfaceRegistry
import com.dst.rpc.Collector
import com.dst.rpc.annotations.RPCImplementation
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
        interfaceAnnotatedElements: List<TypeElement>,
        interfaceImplementationElements: List<TypeElement>
    ) {
        if (interfaceAnnotatedElements.isEmpty() && interfaceImplementationElements.isEmpty()) {
            return
        }
        val firstClassDeclaration = interfaceAnnotatedElements.firstOrNull()
            ?: interfaceImplementationElements.firstOrNull()
            ?: return
        val packageName = (firstClassDeclaration.enclosingElement as PackageElement).qualifiedName.toString()
        val generatedClassName = "${Collector::class.java.simpleName}_${Integer.toBinaryString(firstClassDeclaration.simpleName.hashCode())}"

        // kotlin code generation.
//        val collectorWriter = environment.filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "${generatedClassName}.kt").openWriter()
        val collectorWriter = environment.filer.createSourceFile("${packageName}.${generatedClassName}").openWriter()
        collectorWriter.appendLine("package ${packageName};")
            .appendLine()
            .appendLine("import ${Override::class.java.name};")
            .appendLine("import ${Collector::class.java.name};")
            .appendLine("import ${NotNull::class.java.name};")
            .appendLine("import ${RPCInterfaceRegistry::class.java.name};")
            .appendLine("import ${ExceptionHandler::class.java.name};")
            .appendLine("import ${StubFunction::class.java.name};")
            .appendLine("import ${Address::class.java.name};")
            .appendLine()
            .appendLine("public class $generatedClassName implements ${Collector::class.java.name} {")
            .appendLine()
            .appendLine("\t@Override")
            .appendLine("\tpublic void collect(@NotNull RPCInterfaceRegistry registry) {")
            .apply {
                interfaceAnnotatedElements.forEachIndexed { index, typeElement ->
                    if (index != 0) {
                        appendLine()
                    }
                    appendLine("\t\tregistry.putServiceProxyLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function3<Address, Address, ExceptionHandler, ${typeElement.qualifiedName}>() {")
                        .appendLine("\t\t\t@Override")
                        .appendLine("\t\t\tpublic ${typeElement.qualifiedName} invoke(@NotNull Address sourceAddress, @NotNull Address remoteAddress, @NotNull ExceptionHandler exceptionHandler) {")
                        .appendLine("\t\t\t\treturn new ${typeElement.qualifiedName}_Generated_Proxy(sourceAddress, remoteAddress, exceptionHandler);")
                        .appendLine("\t\t\t}")
                        .appendLine("\t\t});")
                    appendLine("\t\tregistry.putServiceStubLazy(${typeElement.qualifiedName}.class, new kotlin.jvm.functions.Function1<${typeElement.qualifiedName}, StubFunction>() {")
                        .appendLine("\t\t\t@Override")
                        .appendLine("\t\t\tpublic StubFunction invoke(@NotNull ${typeElement.qualifiedName} impl) {")
                        .appendLine("\t\t\t\treturn new ${typeElement.qualifiedName}_Generated_Stub(impl);")
                        .appendLine("\t\t\t}")
                        .appendLine("\t\t});")
                }
                interfaceImplementationElements.forEachIndexed { index, typeElement ->
                    if (index == 0) {
                        appendLine()
                    }
                    val annotation = typeElement.annotationMirrors.find { it.annotationType.toString() == RPCImplementation::class.java.name }
                    if (annotation != null) {
                        val elementValues = annotation.elementValues.mapKeys { (executableElement, _) -> executableElement.simpleName.toString() }
                        val clazz = elementValues["clazz"]
                        if (clazz != null) {
                            appendLine("\t\tregistry.putServiceImpl(${clazz.value}.class, new ${typeElement.qualifiedName}());")
                        }
                    }
                }
            }
            .appendLine("\t}")
            .appendLine("}")
        collectorWriter.flush()
        collectorWriter.close()

        val resourceWriter = environment.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/${Collector::class.java.name}").openWriter()
        resourceWriter.appendLine("${packageName}.${generatedClassName}")
        resourceWriter.flush()
        resourceWriter.close()
    }
}