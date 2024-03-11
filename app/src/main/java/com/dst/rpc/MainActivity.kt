package com.dst.rpc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.system.measureNanoTime

class MainActivity : AppCompatActivity() {

    private val testInterface: TestInterface = ClientManager.serviceCreate(TestInterface::class.java, mainProcessAddress, libraryProcessAddress)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.go_to_library_activity).setOnClickListener { v ->
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.get_name).setOnClickListener { v ->
            lifecycleScope.launch {
                val countTime = measureNanoTime {
                    val name = this@MainActivity.testInterface.name
                    println(name)
                }
                println("getName cost: ${(countTime) / 1_000_000.0}ms")
            }
        }
        findViewById<View>(R.id.open_user_name).setOnClickListener { v ->
            lifecycleScope.launch {
                val countTime = measureNanoTime {
                    val name = this@MainActivity.testInterface.openUserName(12331)
                    println(name)
                }
                println("getName cost: ${(countTime) / 1_000_000.0}ms")
            }
        }
        findViewById<View>(R.id.suspend_open_user_name).setOnClickListener { v ->
            lifecycleScope.launch {
                val countTime = measureNanoTime {
                    val name = this@MainActivity.testInterface.suspendGetUsername(222222)
                    println(name)
                }
                println("getName cost: ${(countTime) / 1_000_000.0}ms")
            }
        }
        findViewById<View>(R.id.suspend_get_user_name).setOnClickListener { v ->
            lifecycleScope.launch {
                val countTime = measureNanoTime {
                    val name = this@MainActivity.testInterface.suspendOpenUsername(1231, "2122")
                    println(name)
                }
                println("getName cost: ${(countTime) / 1_000_000.0}ms")
            }
        }
    }
}