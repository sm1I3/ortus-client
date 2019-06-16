package com.sm1l3.ortus_client

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.sm1l3.ortus_client.database.DBHelper
import com.sm1l3.ortus_client.database.Database
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_setup_user.*

class MainActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var browser: Browser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        database = Database(DBHelper(this))

        when (database.getCurrentUser()) {
            null -> launchUserSetup()
            else -> launchBrowser()
        }
    }

    fun onLoginButtonClick(view: View) {
        val login = loginTextField.text
        val password = passwordTextField.text

        if (login.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Empty login or password", Toast.LENGTH_SHORT).show()
            return
        }

        database.updateCurrentUser(login.toString(), password.toString())
        launchBrowser()
    }

    private fun requestPermissions() {
        when (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)) {
            PERMISSION_GRANTED -> return
            else -> requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun launchUserSetup() = setContentView(R.layout.fragment_setup_user)

    private fun launchBrowser() {
        setContentView(R.layout.fragment_browser)

        if (!::browser.isInitialized) {
            browser = Browser(webView, progressBar, database)
        }

        browser.start()
    }
}
