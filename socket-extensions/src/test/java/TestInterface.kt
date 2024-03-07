import com.dst.rpc.INoProguard


/**
 * @author liuzhongao
 * @since 2024/3/7 17:25
 */
interface TestInterface : INoProguard {

    val name: String

    suspend fun getUserName(): String
}