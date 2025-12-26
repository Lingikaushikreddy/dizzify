package app.dizzify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.dizzify.ui.LauncherShell
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val vm: LauncherViewModel = koinViewModel()
            LauncherShell(viewModel = vm)
        }
    }


    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        // Prevent back from exiting launcher (it's a home screen)
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true
//        }
//        return super.onKeyDown(keyCode, event)
//    }
//
//    override fun onBackPressed() {
//        // Do nothing - launcher shouldn't exit on back
//    }
}