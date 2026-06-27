package com.kasircafe.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kasircafe.pos.presentation.navigation.PosNavGraph
import com.kasircafe.pos.ui.theme.KasirCafeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KasirCafeTheme {
                PosNavGraph()
            }
        }
    }
}
