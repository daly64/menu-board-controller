package com.example.menu_board_controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.menu_board_controller.ui.screens.ControlPanelScreen
import com.example.menu_board_controller.ui.theme.MenuboardcontrollerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuboardcontrollerTheme {
                ControlPanelScreen()
            }
        }
    }
}
