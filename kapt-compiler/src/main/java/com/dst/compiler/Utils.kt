package com.dst.compiler

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * @author liuzhongao
 * @since 2024/2/16 15:25
 */


fun buildType(element: Element): String {
    return buildType(element.asType())
}

fun buildType(mirror: TypeMirror): String {
    return mirror.toString()
}

fun buildTypeWithoutParameterizedTypes(element: Element): String {
    return buildTypeWithoutParameterizedTypes(element.asType())
}

fun buildTypeWithoutParameterizedTypes(mirror: TypeMirror): String {
    return StringBuilder()
        .apply {
            if (mirror is DeclaredType) {
                append((mirror.asElement() as TypeElement).qualifiedName.toString())
            } else append(mirror.toString())
        }
        .toString()
}

fun buildFunctionUniqueKey(element: ExecutableElement): String {
    return StringBuilder().also { stringBuilder ->
        if (element.isSuspendExecutable) {
            stringBuilder.append("suspend ")
        }
        stringBuilder.append("fun ${element.simpleName}(")
        val parameterList = element.parameters
        if (parameterList.isNotEmpty()) {
            parameterList.forEachIndexed { index, variableElement ->
                if (index != 0) {
                    stringBuilder.append(", ")
                }
                stringBuilder.append(variableElement.simpleName.toString())
                stringBuilder.append(": ")
                stringBuilder.append(buildType(variableElement))
            }
        }
        stringBuilder.append(")")
        stringBuilder.append(": ${buildType(element.returnType)}")
    }.toString()
}

val ExecutableElement.isSuspendExecutable: Boolean
    get() {
        val parameterList = this.parameters as? List<VariableElement>
        if (parameterList.isNullOrEmpty()) {
            return false
        }
        return parameterList.any { variableElement -> variableElement.asType().toString().contains("kotlin.coroutines.Continuation") }
    }

val ExecutableElement.continuationVariable: VariableElement?
    get() = this.parameters.find { it.asType().toString().contains("kotlin.coroutines.Continuation") }

val ExecutableElement.parametersWithoutContinuation: List<VariableElement>
    get() = this.parameters.filter { !it.asType().toString().contains("kotlin.coroutines.Continuation") }