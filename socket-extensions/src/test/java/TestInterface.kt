import com.dst.rpc.INoProguard
import com.dst.rpc.annotations.RPCInterface


/**
 * @author liuzhongao
 * @since 2024/3/7 17:25
 */
@RPCInterface
interface TestInterface : INoProguard {

    val name: String

    suspend fun getUserName(): String
}