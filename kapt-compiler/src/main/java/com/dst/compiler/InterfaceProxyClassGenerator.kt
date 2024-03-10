package com.dst.compiler

import com.dst.rpc.ClientManager
import com.dst.rpc.Connection
import com.dst.rpc.ExceptionHandler
import com.dst.rpc.Address
import org.jetbrains.annotations.NotNull
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * @author liuzhongao
 * @since 2024/2/16 14:43
 */
internal object InterfaceProxyClassGenerator {

    @JvmStatic
    fun buildInterfaceProxyImplementationClass(
        environment: ProcessingEnvironment,
        rootElement: TypeElement,
    ) {
        val packageName = (rootElement.enclosingElement as? PackageElement)?.qualifiedName?.toString() ?: return
        val simpleName = rootElement.simpleName.toString()
        val newClassName = "${simpleName}_Generated_Proxy"
        val writer = environment.filer.createSourceFile("${packageName}.${newClassName}").openWriter().buffered()

        writer.appendLine("package ${packageName};")
            .appendLine()
            .appendLine("public final class $newClassName implements ${rootElement.qualifiedName} {")
            .appendLine()
            .appendLine("\t@${NotNull::class.java.name}")
            .appendLine("\tprivate final ${Connection::class.java.name} connection;")
            .appendLine()
            .append("\tpublic ${newClassName}(")
            .append("@${NotNull::class.java.name} ${Address::class.java.name} sourceAddress, ")
            .append("@${NotNull::class.java.name} ${Address::class.java.name} remoteAddress")
            .appendLine(") {")
            .appendLine("\t\tthis.connection = ${ClientManager::class.java.name}.openConnection(sourceAddress, remoteAddress);")
            .appendLine("\t}")
            .appendLine()
            .append("\tpublic ${newClassName}(")
            .append("@${NotNull::class.java.name} ${Address::class.java.name} sourceAddress, ")
            .append("@${NotNull::class.java.name} ${Address::class.java.name} remoteAddress, ")
            .append("@${NotNull::class.java.name} ${ExceptionHandler::class.java.name} exceptionHandler")
            .appendLine(") {")
            .appendLine("\t\tthis.connection = ${ClientManager::class.java.name}.openConnection(sourceAddress, remoteAddress, exceptionHandler);")
            .appendLine("\t}")
            .apply {
                val memberFunctionList = rootElement.enclosedElements
                if (memberFunctionList.isNotEmpty()) {
                    for (function in memberFunctionList) {
                        if (function !is ExecutableElement) {
                            continue
                        }
                        appendLine()
                            .appendLine("\t@Override")
                            .append("\tpublic final ${function.returnType} ${function.simpleName}(")
                        val methodParameter = function.parameters
                        if (methodParameter.isNotEmpty()) {
                            methodParameter.forEachIndexed { index, variableElement ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append(buildType(variableElement)).append(" ").append(variableElement.simpleName.toString())
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
                                .appendLine("\t\t\t\t\t\t\"${buildFunctionUniqueKey(function)}\",")
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
                                .appendLine("\t\t\t\t\t\t\"${buildFunctionUniqueKey(function)}\",")
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
                                    val functionParameter = function.parametersWithoutContinuation
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
                                .appendLine("\t\t\t\ttrue,")
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
}