package com.thisfeng.composestudykit.ui.screen.test

/**
 * @author：thisfeng
 * @time 2025/9/15 16:48
 *
 */
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPermissionScreen(
    viewModel: PermissionViewModel = viewModel(),
    onBackClick: () -> Unit

) {
    val context = LocalContext.current

    // 收集 ViewModel 中的 StateFlow 狀態
    val hasPermission by viewModel.hasCameraPermission.collectAsState()
    val showPermissionDeniedDialog by viewModel.showPermissionDeniedDialog.collectAsState()

    // 螢幕載入時檢查一次權限
    LaunchedEffect(Unit) {
        viewModel.checkAndRequestCameraPermission(context)


      /*  XXPermissions.with(context)
            .permission(PermissionLists.getCameraPermission())
            .request(object : OnPermissionCallback {
                override fun onResult(
                    grantedList: MutableList<IPermission>,
                    deniedList: MutableList<IPermission>
                ) {
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        return
                    }
                    // 移除未定义的函数调用
                }
            })*/
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "test permission",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )

        if (hasPermission) {
            Text("已獲得相機權限！")
        } else {
            Text("需要相機權限才能使用此功能。")
            Button(onClick = {
                // 點擊時，在 ViewModel 中發起權限請求
                viewModel.checkAndRequestCameraPermission(context)
            }) {
                Text("請求相機權限")
            }
        }
    }

    // 显示权限被永久拒绝对话框
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionDeniedDialog() },
            title = { Text("权限被拒绝") },
            text = { Text("相机权限已被永久拒绝，请前往设置页面手动开启权限。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissPermissionDeniedDialog()
                        if (context is Activity) {
                            viewModel.startPermissionSettingsActivity(context)
                        }
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissPermissionDeniedDialog() }
                ) {
                    Text("取消")
                }
            }
        )
    }
}