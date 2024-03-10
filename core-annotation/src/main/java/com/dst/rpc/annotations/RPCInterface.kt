package com.dst.rpc.annotations

/**
 * this annotation is only available on interfaces!
 * please do not used on origin classes or other symbols.
 *
 * this is used to create default implementation the annotated interfaces,
 * for better performance.
 *
 * @author liuzhongao
 * @since 2024/3/8 00:02
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class RPCInterface