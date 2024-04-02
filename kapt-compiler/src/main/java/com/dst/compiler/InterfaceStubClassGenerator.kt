package com.dst.compiler

import com.dst.rpc.StubFunction
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * @author liuzhongao
 * @since 2024/2/16 17:17
 */
internal object InterfaceStubClassGenerator {

    @JvmStatic
    fun buildInterfaceProxyImplementationClass(
        environment: ProcessingEnvironment,
        rootElement: Element,
    ) {
        buildInterfaceCalleeStubImplementationClass(
            environment = environment,
            rootElement = rootElement,
        )
    }

    @JvmStatic
    private fun buildInterfaceCalleeStubImplementationClass(
        environment: ProcessingEnvironment,
        rootElement: Element,
    ) {
        if (rootElement !is TypeElement) {
            return
        }

        val packageName = (rootElement.enclosingElement as? PackageElement)?.qualifiedName?.toString() ?: return
        val simpleName = rootElement.simpleName.toString()
        val newClassName = "${simpleName}_Generated_Stub"
        val writer = environment.filer.createSourceFile("${packageName}.${newClassName}").openWriter().buffered()
        val declaringClassName = rootElement.qualifiedName.toString()

        writer.appendLine("package ${packageName};")
            .appendLine()
            .appendLine("public final class $newClassName implements ${StubFunction::class.java.name} {")
        writer.appendLine()
            .appendLine("\t@${NotNull::class.java.name}")
            .appendLine("\tprivate final $declaringClassName implementationInstance;")
            .appendLine()
            .appendLine("\tpublic ${newClassName}(@${NotNull::class.java.name} $declaringClassName implementationInstance) {")
            .appendLine("\t\tthis.implementationInstance = implementationInstance;")
            .appendLine("\t}")

        writer.appendLine()
            .appendLine("\t@${Nullable::class.java.name}")
            .appendLine("\t@${Override::class.java.name}")
            .appendLine("\tpublic ${Object::class.java.name} invokeNonSuspendFunction(@${NotNull::class.java.name} ${Class::class.java.name}<?> functionOwner, @${NotNull::class.java.name} ${String::class.java.name} functionName, @${NotNull::class.java.name} ${String::class.java.name} functionUniqueKey, @${NotNull::class.java.name} java.util.List<? extends Class<?>> functionParameterTypes, @${NotNull::class.java.name} java.util.List<?> functionParameterValue) {")
            .apply {
                val functionList = rootElement.enclosedElements.filterIsInstance<ExecutableElement>().filter { !it.isSuspendExecutable }
                if (functionList.isNotEmpty()) {
                    appendLine("\t\t${Object::class.java.simpleName} result = null;")
                    appendLine("\t\tswitch (functionUniqueKey) {")
                    functionList.forEach { element ->
                        appendLine("\t\t\tcase \"${buildFunctionUniqueKey(element)}\":")
                        if (element.returnType.toString() == "void" || element.returnType.toString() == "java.lang.Void") {
                            append("\t\t\t\tthis.implementationInstance.${element.simpleName}(")
                        } else append("\t\t\t\tresult = this.implementationInstance.${element.simpleName}(")
                        element.parameters.forEachIndexed { index, variableElement ->
                            if (index != 0) {
                                append(", ")
                            }
                            append("(${buildType(variableElement)}) functionParameterValue.get(${index})")
                        }
                        appendLine(");")
                        appendLine("\t\t\t\tbreak;")
                    }
                    appendLine("\t\t\tdefault:")
                    appendLine("\t\t\t\tbreak;")
                    appendLine("\t\t}")
                    appendLine("\t\treturn result;")
                } else appendLine("\t\treturn null;")
            }
            .appendLine("\t}")

        writer.appendLine()
            .appendLine("\t@${Nullable::class.java.name}")
            .appendLine("\t@${Override::class.java.name}")
            .appendLine("\tpublic ${Object::class.java.name} invokeSuspendFunction(@${NotNull::class.java.name} ${Class::class.java.name}<?> functionOwner, @${NotNull::class.java.name} ${String::class.java.name} functionName, @${NotNull::class.java.name} ${String::class.java.name} functionUniqueKey, @${NotNull::class.java.name} ${List::class.java.name}<? extends ${Class::class.java.name}<?>> functionParameterTypes, @${NotNull::class.java.name} java.util.List<?> functionParameterValue, @${NotNull::class.java.name} kotlin.coroutines.Continuation<? super Object> \$completion) {")
            .apply {
                val functionList = rootElement.enclosedElements.filterIsInstance<ExecutableElement>().filter { it.isSuspendExecutable }
                if (functionList.isNotEmpty()) {
                    appendLine("\t\tObject result = null;")
                    appendLine("\t\tswitch (functionUniqueKey) {")
                    functionList.forEach { element ->
                        appendLine("\t\t\tcase \"${buildFunctionUniqueKey(element)}\":")
                        if (element.returnType.toString() == requireNotNull(Void::class.javaPrimitiveType).name || element.returnType.toString() == Void::class.java.name) {
                            append("\t\t\t\tthis.implementationInstance.${element.simpleName}(")
                        } else append("\t\t\t\tresult = this.implementationInstance.${element.simpleName}(")
                        val functionParameterList = element.parameters
                        for (index in 0 until functionParameterList.size - 1) {
                            if (index != 0) {
                                append(", ")
                            }
                            append("(${buildType(functionParameterList[index])}) functionParameterValue.get(${index})")
                        }
                        if (functionParameterList.size > 1) {
                            append(", ")
                        }
                        append("\$completion")
                        appendLine(");")
                        appendLine("\t\t\t\tbreak;")
                    }
                    appendLine("\t\t\tdefault:")
                    appendLine("\t\t\t\tbreak;")
                    appendLine("\t\t}")
                    appendLine("\t\treturn result;")
                } else appendLine("\t\treturn null;")
            }
            .appendLine("\t}")
        writer.appendLine("}")
        writer.flush()
        writer.close()
    }

}