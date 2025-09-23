package com.thisfeng.composestudykit.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thisfeng.composestudykit.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * NetworkExamplesScreen的UI测试
 * 演示如何测试包含多个功能项的页面
 */
@RunWith(AndroidJUnit4::class)
class NetworkExamplesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 测试网络示例页面显示基本功能项
     */
    @Test
    fun networkExamplesScreen_showsAllFeatureItems() {
        // 验证主要功能项存在
        composeTestRule.onNodeWithText("网络请求示例").assertExists()
        composeTestRule.onNodeWithText("DataStore 示例").assertExists()
        composeTestRule.onNodeWithText("文件操作示例").assertExists()
        composeTestRule.onNodeWithText("🔄 版本更新").assertExists()
    }

    /**
     * 测试点击版本更新功能项
     */
    @Test
    fun networkExamplesScreen_clickUpdateItem_navigatesToUpdateScreen() {
        // 点击版本更新项
        composeTestRule.onNodeWithText("🔄 版本更新").performClick()
        
        // 验证导航到更新页面（根据实际界面文本调整）
        composeTestRule.onNodeWithText("版本更新").assertExists()
    }
}