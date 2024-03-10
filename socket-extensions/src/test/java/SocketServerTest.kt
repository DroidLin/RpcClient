import com.dst.rpc.ClientManager
import com.dst.rpc.InitConfig
import com.dst.rpc.Address
import com.dst.rpc.socket.sourceAddress
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * @author liuzhongao
 * @since 2024/3/7 17:26
 */
internal val serverSocketAddress = Address("socket", "localhost", 2213)

fun main() = runBlocking {
    val initConfig = InitConfig.Builder()
        .sourceAddress(serverSocketAddress)
        .build()
    ClientManager.putService(TestInterface::class.java, TestInterfaceImpl())
    ClientManager.init(initConfig)
    coroutineScope {
        delay(600_000)
    }
}