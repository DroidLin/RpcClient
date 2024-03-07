package com.dst.compiler

import java.io.Writer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * @author liuzhongao
 * @since 2024/2/16 14:43
 */
internal object InterfaceProxyClassGenerator {

    @JvmStatic
    fun buildInterfaceProxyImplementationClass(
        environment: ProcessingEnvironment,
        rootElement: Element,
    ) {
        if (rootElement !is TypeElement) {
            return
        }
        val packageName = (rootElement.enclosingElement as? PackageElement)?.qualifiedName?.toString() ?: return
        val simpleName = rootElement.simpleName.toString()
        val newClassName = "${simpleName}_Generated_Proxy"
        val writer = environment.filer.createSourceFile("${packageName}.${newClassName}").openWriter().buffered()

        writer.appendLine("package ${packageName};")
            .appendLine()
            .appendLine("public final class $newClassName implements ${rootElement.qualifiedName} {")
            .appendLine()
            .appendLine("\t@androidx.annotation.NonNull")
            .appendLine("\tprivate final com.dst.rpc.Connection connection;")
            .appendLine()
            .append("\tpublic ${newClassName}(")
            .append("@androidx.annotation.NonNull com.dst.rpc.RPCAddress sourceAddress, ")
            .append("@androidx.annotation.NonNull com.dst.rpc.RPCAddress remoteAddress")
            .appendLine(") {")
            .appendLine("\t\tthis.connection = com.dst.rpc.ClientManager.openConnection(sourceAddress, remoteAddress);")
            .appendLine("\t}")
            .appendLine()
            .append("\tpublic ${newClassName}(")
            .append("@androidx.annotation.NonNull com.dst.rpc.RPCAddress sourceAddress, ")
            .append("@androidx.annotation.NonNull com.dst.rpc.RPCAddress remoteAddress, ")
            .append("@androidx.annotation.NonNull com.dst.rpc.ExceptionHandler exceptionHandler")
            .appendLine(") {")
            .appendLine("\t\tthis.connection = com.dst.rpc.ClientManager.openConnection(sourceAddress, remoteAddress, exceptionHandler);")
            .appendLine("\t}")
            .appendLine()
            .apply {
                val memberFunctionList = rootElement.enclosedElements
                if (memberFunctionList.isNotEmpty()) {
                    for (function in memberFunctionList) {
                        if (function !is ExecutableElement) {
                            continue
                        }
                        appendLine()
                            .appendLine("\t@java.lang.Override")
                            .append("\tpublic final ${function.returnType} ${function.simpleName}(")
                        val methodParameter = function.parameters
                        if (methodParameter.isNotEmpty()) {
                            methodParameter.forEachIndexed { index, variableElement ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append(buildType(variableElement))
                                    .append(" ")
                                    .append(variableElement.simpleName.toString())
                            }
                        }
                        appendLine(") {")
                        val returnTypeString = function.returnType.toString()
                        val returnValueExists = returnTypeString != "void" && returnTypeString != "java.lang.Void"
                        if (returnValueExists) {
                            appendLine("\t\tjava.lang.Object data = null;")
                        }
                        appendLine("\t\ttry {")
                        if (!function.isSuspendExecutable) {
                            if (returnValueExists) {
                                appendLine("\t\t\tdata = kotlinx.coroutines.BuildersKt.runBlocking(kotlin.coroutines.EmptyCoroutineContext.INSTANCE, new kotlin.jvm.functions.Function2<kotlinx.coroutines.CoroutineScope, kotlin.coroutines.Continuation<? super Object>, Object>() {")
                            } else appendLine("\t\t\tkotlinx.coroutines.BuildersKt.runBlocking(kotlin.coroutines.EmptyCoroutineContext.INSTANCE, new kotlin.jvm.functions.Function2<kotlinx.coroutines.CoroutineScope, kotlin.coroutines.Continuation<? super Object>, Object>() {")
                            appendLine("\t\t\t\t@Override")
                                .appendLine("\t\t\t\tpublic Object invoke(kotlinx.coroutines.CoroutineScope coroutineScope, kotlin.coroutines.Continuation<? super Object> continuation) {")
                                .appendLine("\t\t\t\t\treturn ${newClassName}.this.connection.call(")
                                .appendLine("\t\t\t\t\t\t${rootElement.qualifiedName}.class,")
                                .appendLine("\t\t\t\t\t\t\"${function.simpleName}\",")
                                .apply {
                                    append("\t\t\t\t\t\tkotlin.collections.CollectionsKt.listOf(")
                                    val functionParameter = function.parameters
                                    if (functionParameter.isNotEmpty()) {
                                        functionParameter.forEachIndexed { index, variableElement ->
                                            if (index != 0) {
                                                append(", ")
                                            }
                                            append("${buildType(variableElement)}.class")
                                        }
                                    }
                                    appendLine("),")
                                }
                                .apply {
                                    append("\t\t\t\t\t\tkotlin.collections.CollectionsKt.listOf(")
                                    val functionParameter = function.parameters
                                    if (functionParameter.isNotEmpty()) {
                                        functionParameter.forEachIndexed { index, variableElement ->
                                            if (index != 0) {
                                                append(", ")
                                            }
                                            append(variableElement.simpleName)
                                        }
                                    }
                                    appendLine("),")
                                }
                                .appendLine("\t\t\t\t\t\tfalse,")
                                .appendLine("\t\t\t\t\t\tcontinuation")
                                .appendLine("\t\t\t\t\t);")
                                .appendLine("\t\t\t\t}")
                                .appendLine("\t\t\t});")
                        } else {
                            val continuationParameter = requireNotNull(function.continuationVariable)
                            if (returnValueExists) {
                                appendLine("\t\t\tdata = ${newClassName}.this.connection.call(")
                            } else appendLine("\t\t\t${newClassName}.this.connection.call(")
                            appendLine("\t\t\t\t${rootElement.qualifiedName}.class,")
                                .appendLine("\t\t\t\t\"${function.simpleName}\",")
                                .apply {
                                    append("\t\t\t\tkotlin.collections.CollectionsKt.listOf(")
                                    val functionParameter = function.parametersWithoutContinuation
                                    if (functionParameter.isNotEmpty()) {
                                        functionParameter.forEachIndexed { index, variableElement ->
                                            if (index != 0) {
                                                append(", ")
                                            }
                                            append("${buildType(variableElement)}.class")
                                        }
                                    }
                                    appendLine("),")
                                }
                                .apply {
                                    append("\t\t\t\tkotlin.collections.CollectionsKt.listOf(")
                                    val functionParameter = function.parameters
                                    if (functionParameter.isNotEmpty()) {
                                        functionParameter.forEachIndexed { index, variableElement ->
                                            if (index != 0) {
                                                append(", ")
                                            }
                                            append(variableElement.simpleName)
                                        }
                                    }
                                    appendLine("),")
                                }
                                .appendLine("\t\t\t\tfalse,")
                                .appendLine("\t\t\t\t(kotlin.coroutines.Continuation<? super Object>) ${continuationParameter.simpleName}")
                                .appendLine("\t\t\t);")
                        }
                        appendLine("\t\t} catch (Throwable e) {")
                            .appendLine("\t\t\tthrow new RuntimeException(e);")
                            .appendLine("\t\t}")
                        if (returnValueExists) {
//                            appendLine("\t\tif (data == null) {")
//                                .appendLine("\t\t\tthrow new NullPointerException(\"\");")
//                                .appendLine("\t\t}")
                            appendLine("\t\treturn (${buildType(function.returnType)}) data;")
                        }
                        appendLine("\t}")
                    }
                }
            }
            .appendLine("}")
        writer.flush()
        writer.close()
    }

    @JvmStatic
    private fun buildImplementationMethod(writer: Writer, element: ExecutableElement, body: () -> Unit = {}) {
        writer.appendLine()
            .appendLine("\t@java.lang.Override")
            .append("\tpublic final ${element.returnType} ${element.simpleName}(")
        val methodParameter = element.parameters
        if (methodParameter.isNotEmpty()) {
            methodParameter.forEachIndexed { index, variableElement ->
                if (index != 0) {
                    writer.append(", ")
                }
                writer.append(buildType(variableElement))
                    .append(" ")
                    .append(variableElement.simpleName.toString())
            }
        }
        writer.appendLine(") {")
        body()
        writer.appendLine("\t}")
    }
}