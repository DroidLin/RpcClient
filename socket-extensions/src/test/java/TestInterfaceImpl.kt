import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author liuzhongao
 * @since 2024/3/7 17:25
 */
class TestInterfaceImpl : TestInterface {
    override val name: String get() = "liuzhongao"

    override suspend fun getUserName(): String {
        return withContext(Dispatchers.IO) {
            "liuzhongaoV1"
        }
    }
}