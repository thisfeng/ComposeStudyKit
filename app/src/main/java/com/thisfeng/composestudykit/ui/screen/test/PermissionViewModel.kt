package com.thisfeng.composestudykit.ui.screen.test

/**
 * @author：thisfeng
 * @time 2025/9/15 16:49
 *
 */
import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PermissionViewModel : ViewModel() {

    // 使用 StateFlow 來暴露權限狀態
    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission = _hasCameraPermission.asStateFlow()

    private val _showPermissionDeniedDialog = MutableStateFlow(false)
    val showPermissionDeniedDialog = _showPermissionDeniedDialog.asStateFlow()

    fun checkAndRequestCameraPermission(context: Context) {
        viewModelScope.launch {
            if (context is Activity) {
                XXPermissions.with(context)
                    .permission(PermissionLists.getCameraPermission())
                    .request(object : OnPermissionCallback {
                        override fun onResult(
                            grantedList: MutableList<IPermission>,
                            deniedList: MutableList<IPermission>
                        ) {
                            val allGranted = deniedList.isEmpty()
                            _hasCameraPermission.value = allGranted

                            // 如果权限被永久拒绝，则显示对话框引导用户到设置页面
                            if (deniedList.isNotEmpty()) {
                                val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(
                                    context,
                                    deniedList
                                )
                                if (doNotAskAgain) {
                                    _showPermissionDeniedDialog.value = true
                                }
                            }
                        }
                    })
            }
        }
    }

    fun dismissPermissionDeniedDialog() {
        _showPermissionDeniedDialog.value = false
    }

    fun startPermissionSettingsActivity(context: Context) {
        if (context is Activity) {
            XXPermissions.startPermissionActivity(context, PermissionLists.getCameraPermission())
        }
    }
}