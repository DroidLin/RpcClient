import com.android.dependencies.serializer.SerializeReader
import com.android.dependencies.serializer.SerializeWriter
import org.junit.Assert
import org.junit.Test

/**
 * @author liuzhongao
 * @since 2024/3/6 17:41
 */
class SerializeTest {

    @Test
    fun intSerializeTest() {
        val serializeWriter = SerializeWriter()
        serializeWriter.writeInt(10)

        val serializeReader = SerializeReader(serializeWriter.toByteArray())
        val number = serializeReader.readInt()
        Assert.assertEquals(number, 10)
    }

    @Test
    fun intArraySerializeTest() {
        val array = intArrayOf(-121, 2, 3, 4, 5)
        val serializeWriter = SerializeWriter()
        serializeWriter.writeIntArray(array)

        val serializeReader = SerializeReader(serializeWriter.toByteArray())
        val tempArray = serializeReader.readIntArray()
        Assert.assertArrayEquals(array, tempArray)
    }

    @Test
    fun listSerializeTest() {
        val list: List<Any?> = listOf(1, 2, 2.2, 4, null, 5)
        val serializeWriter = SerializeWriter()
        serializeWriter.writeList(list)

        val serializeReader = SerializeReader(serializeWriter.toByteArray())
        val tempList = serializeReader.readList<Any?>()
        Assert.assertEquals(list.size, tempList?.size)
        for (index in list.indices) {
            Assert.assertEquals(list[index], tempList?.get(index))
        }
    }

    @Test
    fun charArraySerializeTest() {
        val array = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g')
        val serializeWriter = SerializeWriter()
        serializeWriter.writeCharArray(array)

        val serializeReader = SerializeReader(serializeWriter.toByteArray())
        val tempArray = serializeReader.readCharArray()
        Assert.assertArrayEquals(array, tempArray)
    }

    @Test
    fun stringSerializeTest() {
        val source = "liuzhongao"
        val serializeWriter = SerializeWriter()
        serializeWriter.writeString(source)

        val serializeReader = SerializeReader(serializeWriter.toByteArray())
        val tempArray = serializeReader.readString()
        Assert.assertEquals(source, tempArray)
    }

}