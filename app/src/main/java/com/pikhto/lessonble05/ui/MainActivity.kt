package com.pikhto.lessonble05.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.pikhto.blin.BleManager
import com.pikhto.blin.BleManagerInterface
import com.pikhto.blin.permissions.RequestPermissions
import com.pikhto.lessonble05.LessonBle05App
import com.pikhto.lessonble05.R
import com.pikhto.lessonble05.databinding.ActivityMainBinding
import com.pikhto.lessonble05.fake.FakeBleManager
import com.pikhto.lessonble05.helper.linkMenu
import com.pikhto.lessonble05.models.MainActivityViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    companion object {
        const val FAKE = "fake_debug"
    }
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var bleManager:BleManagerInterface

    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_settings -> true
                else -> { false }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBleManager()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        requestPermissions()
        linkMenu(true, menuProvider)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        linkMenu(false, menuProvider)
        super.onDestroy()
    }

    private fun requestPermissions() {
        val requestPermissions = RequestPermissions(this)
        requestPermissions.requestPermissions(listOf(
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
        ))

        lifecycleScope.launch {
            requestPermissions.stateFlowRequestPermission.filterNotNull().collect { permission ->
                if (permission.granted) {
                    Toast.makeText(baseContext, getString(R.string.permission_granted, permission.permission), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, getString(R.string.permission_not_granted, permission.permission), Toast.LENGTH_SHORT).show()
                    finishAndRemoveTask()
                    exitProcess(0)
                }
            }
        }
    }

    private fun initBleManager() {
        var fake = false
        intent.extras?.let { extras ->
            fake = extras.getBoolean(FAKE, false)
        }

        bleManager = if (fake) {
            FakeBleManager()
        } else {
            BleManager(applicationContext)
        }
        (applicationContext as LessonBle05App).bleManager = bleManager
        lifecycle.addObserver(bleManager)
    }
}