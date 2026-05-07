package de.lootz.borderline

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.borderline.app.ProvisioningActivity

/**
 * Launcher entry point. Immediately redirects to ProvisioningActivity.
 * The app icon always opens provisioning/setup, not a dashboard.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ProvisioningActivity::class.java))
        finish()
    }
}
