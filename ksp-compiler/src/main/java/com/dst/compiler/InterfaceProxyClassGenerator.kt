package com.dst.compiler

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.lza.android.inter.process.compiler.*
import java.io.Writer

/**
 * @author liuzhongao
 * @since 2024/2/6 10:56
 */
internal object InterfaceProxyClassGenerator {

    @JvmStatic
    fun buildInterfaceProxyImplementationClass(
        resolver: Resolver,
        codeGenerator: CodeGenerator,
        interfaceClassDeclaration: KSClassDeclaration
    ) {
        buildInterfaceCallerProxyImplementationClass(
            resolver = resolver,
            codeGenerator = codeGenerator,
            interfaceClassDeclaration = interfaceClassDeclaration
        )
    }

    /**
     * build caller interface proxy implementation class.
     */
    @JvmStatic
    private fun buildInterfaceCallerProxyImplementationClass(
        resolver: Resolver,
        codeGenerator: CodeGenerator,
        interfaceClassDeclaration: KSClassDeclaration
    ) {
        require(interfaceClassDeclaration.classKind == ClassKind.INTERFACE) {
            "annotation requires ${interfaceClassDeclaration.qualifiedName?.asString() ?: ""} declared as interface"
        }

        val implClassName = "${interfaceClassDeclaration.simpleName.asString()}_Generated_Proxy"
        val writer = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                sources = arrayOf(requireNotNull(interfaceClassDeclaration.containingFile))
            ),
            packageName = interfaceClassDeclaration.packageName.asString(),
            fileName = implClassName
        ).bufferedWriter()
        buildKtClassPackage(writer, interfaceClassDeclaration.packageName.asString())
        buildKtClassBlock(writer, interfaceClassDeclaration, implClassName) {
            interfaceClassDeclaration.getDeclaredProperties().filter { it.isOpen() }
                .forEach { propertyDeclaration ->
                    buildProperty(
                        writer = writer,
                        propertyDeclaration = propertyDeclaration
                    ) { buildPropertyBody(implClassName, writer, interfaceClassDeclaration, propertyDeclaration) }
                }
            interfaceClassDeclaration.getDeclaredFunctions().filter { it.isOpen() }
                .forEach { functionDeclaration ->
                    buildFunction(
                        writer = writer,
                        functionDeclaration = functionDeclaration
                    ) { buildFunctionBody(implClassName, writer, interfaceClassDeclaration, functionDeclaration) }
                }
        }
        writer.flush()
        writer.close()
    }

    private fun buildKtClassPackage(
        writer: Writer,
        packageName: String
    ) {
        writer.appendLine("package $packageName")
            .appendLine()
    }

    private inline fun buildKtClassBlock(
        writer: Writer,
        interfaceDeclaration: KSClassDeclaration,
        className: String,
        body: () -> Unit
    ) {
        writer.appendLine("class $className @kotlin.jvm.JvmOverloads constructor(")
            .appendLine("\tsourceAddress: com.dst.rpc.RPCAddress,")
            .appendLine("\tremoteAddress: com.dst.rpc.RPCAddress,")
            .appendLine("\texceptionHandler: com.dst.rpc.ExceptionHandler = com.dst.rpc.ExceptionHandler")
            .appendLine(") : ${requireNotNull(interfaceDeclaration.qualifiedName).asString()} {")
            .appendLine()
            .appendLine("\tprivate val connection: com.dst.rpc.Connection = com.dst.rpc.ClientManager.openConnection(sourceAddress, remoteAddress, exceptionHandler)")
            .apply { body() }
            .appendLine("}")
    }

    private fun buildProperty(
        writer: Writer,
        propertyDeclaration: KSPropertyDeclaration,
        propertyBody: () -> Unit
    ) {
        writer.appendLine()
            .append("\toverride ")
            .apply {
                if (propertyDeclaration.isMutable) {
                    append("var")
                } else append("val")
            }
            .append(" ")
            .apply {
                val receiverType = propertyDeclaration.extensionReceiver?.resolve()
                if (receiverType != null) {
                    append(buildType(receiverType)).append(".")
                }
            }
            .append(propertyDeclaration.simpleName.asString())
            .append(": ").append(buildType(propertyDeclaration.type.resolve()))
            .appendLine()
            .append("\t\tget() ").append("{").appendLine()
            .apply { propertyBody() }
            .append("\t\t}")
            .appendLine()
    }

    private fun buildPropertyBody(
        className: String,
        writer: Writer,
        interfaceClassDeclaration: KSClassDeclaration,
        propertyDeclaration: KSPropertyDeclaration
    ) {
        val propertyType = propertyDeclaration.type.resolve()
        writer
            .appendLine("\t\t\treturn kotlinx.coroutines.runBlocking {")
            .appendLine("\t\t\t\tthis@${className}.connection.call(")
            .appendLine("\t\t\t\t\tfunctionOwner = ${interfaceClassDeclaration.qualifiedName?.asString()}::class.java,")
            .appendLine("\t\t\t\t\tfunctionName = \"${propertyDeclaration.simpleName.asString()}\",")
            .appendLine("\t\t\t\t\tfunctionUniqueKey = \"${buildPropertyUniqueKey(propertyDeclaration)}\",")
            .appendLine("\t\t\t\t\tfunctionParameterTypes = kotlin.collections.listOf(),")
            .appendLine("\t\t\t\t\tfunctionParameterValues = kotlin.collections.listOf(),")
            .appendLine("\t\t\t\t\tisSuspended = false")
            .appendLine("\t\t\t\t) as? ${buildType(propertyType)}")
            .append("\t\t\t}")
            .apply {
                if (!propertyType.isMarkedNullable) {
                    appendLine(" ?: throw kotlin.IllegalArgumentException(\"function return type requires non-null type, but returns null type after IPC call and the fallback operation!! please check.\")")
                }
            }
    }

    private fun buildFunction(
        writer: Writer,
        functionDeclaration: KSFunctionDeclaration,
        functionBody: () -> Unit
    ) {
        writer.appendLine()
            .append("\toverride").append(" ")
            .apply {
                if (functionDeclaration.isProtected()) {
                    append("protected").append(" ")
                } else if (functionDeclaration.isInternal()) {
                    append("internal").append(" ")
                } else append("public").append(" ")
                if (functionDeclaration.modifiers.contains(Modifier.SUSPEND)) {
                    append("suspend").append(" ")
                }
            }
            .append("fun ")
            .apply {
                val receiverType = functionDeclaration.extensionReceiver?.resolve()
                if (receiverType != null) {
                    append(buildType(receiverType)).append(".")
                }
            }
            .append("${functionDeclaration.simpleName.asString()}(")
            .append(buildFunctionParameters(functionDeclaration))
            .append(")")
            .apply {
                val returnKSType = functionDeclaration.returnType?.resolve()
                if (returnKSType != null) {
                    append(": ${buildType(returnKSType)}")
                }
            }
            .appendLine(" {")
            .apply { functionBody() }
            .appendLine("\t}")
    }

    private fun buildFunctionBody(
        className: String,
        writer: Writer,
        interfaceClassDeclaration: KSClassDeclaration,
        functionDeclaration: KSFunctionDeclaration
    ) {
        val functionReturnType = requireNotNull(functionDeclaration.returnType?.resolve())
        val hasReturnValue = functionReturnType.declaration.simpleName.asString() != "Unit"
        val isSuspendFunction = functionDeclaration.modifiers.contains(Modifier.SUSPEND)
        writer
            .apply {
                if (isSuspendFunction) {
                    if (hasReturnValue) {
                        appendLine("\t\treturn this@${className}.connection.call(")
                    } else appendLine("\t\tthis@${className}.connection.call(")
                    appendLine("\t\t\tfunctionOwner = ${interfaceClassDeclaration.qualifiedName?.asString()}::class.java,")
                        .appendLine("\t\t\tfunctionName = \"${functionDeclaration.simpleName.asString()}\",")
                        .appendLine("\t\t\tfunctionUniqueKey = \"${buildFunctionUniqueKey(functionDeclaration)}\",")
                        .apply {
                            append("\t\t\tfunctionParameterTypes = kotlin.collections.listOf(")
                            functionDeclaration.parameters.forEachIndexed { index, ksValueParameter ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append("${buildType(ksValueParameter.type.resolve())}::class.java")
                            }
                            appendLine("),")
                        }
                        .apply {
                            append("\t\t\tfunctionParameterValues = kotlin.collections.listOf(")
                            functionDeclaration.parameters.forEachIndexed { index, ksValueParameter ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append(ksValueParameter.name?.asString())
                            }
                            appendLine("),")
                        }
                        .appendLine("\t\t\tisSuspended = $isSuspendFunction")
                    if (hasReturnValue && !functionReturnType.isMarkedNullable) {
                        appendLine("\t\t) as? ${buildType(functionReturnType)} ?: throw kotlin.IllegalArgumentException(\"function return type requires non-null type, but returns null type after IPC call and the fallback operation!! please check.\")")
                    } else {
                        appendLine("\t\t)")
                    }
                } else {
                    if (hasReturnValue) {
                        appendLine("\t\treturn kotlinx.coroutines.runBlocking {")
                    } else appendLine("\t\tkotlinx.coroutines.runBlocking {")
                    appendLine("\t\t\tthis@${className}.connection.call(")
                        .appendLine("\t\t\t\tfunctionOwner = ${interfaceClassDeclaration.qualifiedName?.asString()}::class.java,")
                        .appendLine("\t\t\t\tfunctionName = \"${functionDeclaration.simpleName.asString()}\",")
                        .appendLine("\t\t\t\tfunctionUniqueKey = \"${buildFunctionUniqueKey(functionDeclaration)}\",")
                        .apply {
                            append("\t\t\t\tfunctionParameterTypes = kotlin.collections.listOf(")
                            functionDeclaration.parameters.forEachIndexed { index, ksValueParameter ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append("${buildType(ksValueParameter.type.resolve())}::class.java")
                            }
                            appendLine("),")
                        }
                        .apply {
                            append("\t\t\t\tfunctionParameterValues = kotlin.collections.listOf(")
                            functionDeclaration.parameters.forEachIndexed { index, ksValueParameter ->
                                if (index != 0) {
                                    append(", ")
                                }
                                append(ksValueParameter.name?.asString())
                            }
                            appendLine("),")
                        }
                        .appendLine("\t\t\t\tisSuspended = $isSuspendFunction")
                    if (hasReturnValue && !functionReturnType.isMarkedNullable) {
                        appendLine("\t\t\t) as? ${buildType(functionReturnType)} ?: throw kotlin.IllegalArgumentException(\"function return type requires non-null type, but returns null type after IPC call and the fallback operation!! please check.\")")
                    } else {
                        appendLine("\t\t\t)")
                    }
                    appendLine("\t\t}")
                }
            }
    }
}