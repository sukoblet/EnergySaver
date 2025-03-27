// src/androidTest/java/com/eti/energysaver/ui/auth/LoginScreenTest.kt
package com.eti.energysaver.ui.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onForgotPassword = {}
            )
        }
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }
}
