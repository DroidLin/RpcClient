package com.dst.compiler

import com.dst.rpc.annotations.RPCImplementation
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
        val interfaceAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(RPCInterface::class.java)
            .filterIsInstance<TypeElement>()
        interfaceAnnotatedElements.forEach { element ->
            require(element.kind == ElementKind.INTERFACE) {
                "annotation requires ${element.simpleName} declared as interface"
            }
            InterfaceProxyClassGenerator.buildInterfaceProxyImplementationClass(this.processingEnv, element)
            InterfaceStubClassGenerator.buildInterfaceProxyImplementationClass(this.processingEnv, element)
        }
        val interfaceImplementationElements = roundEnvironment.getElementsAnnotatedWith(RPCImplementation::class.java)
            .filterIsInstance<TypeElement>()
        ResourceCreator.resourceCreate(this.processingEnv, interfaceAnnotatedElements, interfaceImplementationElements)
        return false
    }
}