package com.kameji.starpowerradiowv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SITE_URL = "https://star-power-radio.de/"
        private const val SENDEPLAN_URL = "https://star-power-radio.de/sendeplan.php"
        private const val LOGIN_URL = "https://star-power-radio.de/admin.php?level=6"
        private const val REGISTER_URL = "https://star-power-radio.de/member.php?action=registrieren"
        private const val KONTAKT_URL = "https://star-power-radio.de/kontaktformular.php?send=1"
        private const val BEWERBUNG_URL = "https://star-power-radio.de/kontaktformular.php?send=2"

        private const val DISCORD_URL = "https://discord.gg/qmfYKEWxmY"
        private const val FACEBOOK_URL = "https://www.facebook.com/profile.php?id=100055208095454"
        private const val TIKTOK_URL = "https://www.tiktok.com/@ronnykirsc"
        private const val TWITCH_URL = "https://www.twitch.tv/star_power_radio"

        private const val REQ_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermissionIfNeeded()

        findViewById<Button>(R.id.btnRadio).setOnClickListener {
            Toast.makeText(this, "Radio Button erkannt", Toast.LENGTH_SHORT).show()

            try {
                val intent = Intent(this, RadioService::class.java).apply {
                    action = RadioService.ACTION_START
                }
                ContextCompat.startForegroundService(this, intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Service Fehler: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnMute).setOnClickListener {
            Toast.makeText(this, "Mute Button erkannt", Toast.LENGTH_SHORT).show()

            try {
                val intent = Intent(this, RadioService::class.java).apply {
                    action = RadioService.ACTION_TOGGLE_MUTE
                }
                ContextCompat.startForegroundService(this, intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Mute Fehler: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            openInternalPage(LOGIN_URL)
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            openInternalPage(REGISTER_URL)
        }

        findViewById<Button>(R.id.btnSendeplan).setOnClickListener {
            openInternalPage(SENDEPLAN_URL)
        }

        findViewById<Button>(R.id.btnKontakt).setOnClickListener {
            openInternalPage(KONTAKT_URL)
        }

        findViewById<Button>(R.id.btnBewerbung).setOnClickListener {
            openInternalPage(BEWERBUNG_URL)
        }

        findViewById<Button>(R.id.btnWebsite).setOnClickListener {
            openInternalPage(SITE_URL)
        }

        findViewById<Button>(R.id.btnDiscord).setOnClickListener {
            openExternal(DISCORD_URL)
        }

        findViewById<Button>(R.id.btnFacebook).setOnClickListener {
            openExternal(FACEBOOK_URL)
        }

        findViewById<Button>(R.id.btnTikTok).setOnClickListener {
            openExternal(TIKTOK_URL)
        }

        findViewById<Button>(R.id.btnTwitch).setOnClickListener {
            openExternal(TWITCH_URL)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_NOTIFICATIONS
                )
            }
        }
    }

    private fun openInternalPage(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun openExternal(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}