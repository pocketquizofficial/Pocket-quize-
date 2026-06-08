package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QuizAppMainScreen
import com.example.ui.QuizViewModel
import com.example.ui.theme.PocketQuizTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PocketQuizTheme {
        val viewModel: QuizViewModel = viewModel()
        QuizAppMainScreen(viewModel = viewModel)
      }
    }
  }
}
