package com.dst.compiler

import com.dst.rpc.annotations.RPCInterface
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

/**
 * @author liuzhongao
 * @since 2024/2/16 12:01
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RemoteProcessAnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(RPCInterface::class.java.name)

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {
        if (roundEnvironment == null) return false
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(RPCInterface::class.java)
            .filterIsInstance<TypeElement>()
        annotatedElements.forEach { element ->
            require(element.kind == ElementKind.INTERFACE) {
                "annotation requires ${element.simpleName} declared as interface"
            }
            InterfaceProxyClassGenerator.buildInterfaceProxyImplementationClass(this.processingEnv, element)
            InterfaceStubClassGenerator.buildInterfaceProxyImplementationClass(this.processingEnv, element)
        }
        ResourceCreator.resourceCreate(this.processingEnv, annotatedElements)
        return false
    }
}