package com.thisfeng.composestudykit.update

import android.content.Context
import android.util.Log
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.network.BaseRepository
import com.thisfeng.composestudykit.network.RetrofitClient
import com.thisfeng.composestudykit.utils.AppGlobals
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import java.io.IOException
import java.net.UnknownHostException
import org.json.JSONObject

/**
 * 版本更新数据仓库
 * 负责版本检查、APK下载等业务逻辑
 */
class UpdateRepository : BaseRepository() {

    override val dataStoreCacheManager = null // 版本更新不使用缓存

    private val updateApiService: UpdateApiService by lazy {
        // 使用标准的RetrofitClient创建API服务
        RetrofitClient.createThirdPartyRetrofit("https://cloud.ablegenius.com/")
            .create(UpdateApiService::class.java)
    }

    companion object {
        private const val TAG = "UpdateRepository"
    }

    /**
     * 检查版本更新
     * @param channel 渠道类型
     * @param company 公司标识
     * @param serial 设备序列号
     * @param outlet 门店标识
     * @return ApiResult<VersionInfo>
     */
    suspend fun checkVersion(
        channel: String = "ANDROID",
        company: String = "WingFat",
        serial: String = "W3oqf7L71JG821Q",
        outlet: String = "6001001"
    ): ApiResult<VersionInfo> {
        return safeRawApiCall {
            try {
                Log.d(TAG, "开始检查版本更新...")
                Log.d(TAG, "请求参数: channel=$channel, company=$company, serial=$serial, outlet=$outlet")
                
                // 使用完整的URL路径
                val fullUrl = "https://cloud.ablegenius.com/a/api/app/version"
                val responseBody: ResponseBody = updateApiService.checkVersion(
                    url = fullUrl,
                    channel = channel,
                    company = company,
                    serial = serial,
                    outlet = outlet
                )
                
                Log.d(TAG, "收到响应")
                
                try {
                    val jsonString = responseBody.string()
                    Log.d(TAG, "响应内容: $jsonString")
                    
                    // 直接解析JSON字符串
                    val jsonObject = JSONObject(jsonString)
                    val code = jsonObject.optInt("code", -1)
                    
                    if (code == 1) {
                        val dataObj = jsonObject.optJSONObject("data")
                        if (dataObj != null) {
                            val versionInfo = VersionInfo(
                                versions = dataObj.optString("versions", ""),
                                inner = dataObj.optInt("inner", 0),
                                type = dataObj.optString("type", ""),
                                time = dataObj.optString("time", ""),
                                explain = dataObj.optString("explain", ""),
                                isMust = dataObj.optBoolean("isMust", false),
                                downloadUrl = dataObj.optString("downloadUrl", ""),
                                downloadCount = dataObj.optInt("downloadCount", 0),
                                serial = dataObj.optString("serial", "")
                            )
                            Log.d(TAG, "版本检查成功，返回版本信息")
                            versionInfo
                        } else {
                            Log.e(TAG, "响应中缺少data字段")
                            throw Exception("响应中缺少data字段")
                        }
                    } else {
                        val msg = jsonObject.optString("msg", "版本检查失败")
                        Log.e(TAG, "版本检查失败: $msg")
                        throw Exception(msg)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "读取响应失败", e)
                    throw Exception("读取响应失败: ${e.message}")
                } catch (e: Exception) {
                    Log.e(TAG, "解析响应失败", e)
                    throw Exception("解析响应失败: ${e.message}")
                }
            } catch (e: UnknownHostException) {
                // 域名解析失败，提供更友好的错误信息
                Log.e(TAG, "域名解析失败", e)
                throw Exception("无法连接到服务器，请检查网络连接或稍后重试")
            } catch (e: Exception) {
                Log.e(TAG, "检查版本更新时发生错误", e)
                throw e
            }
        }
    }

    /**
     * 创建UpdateManager实例
     * @param context 上下文
     * @return UpdateManager实例
     */
    fun createUpdateManager(context: Context): UpdateManager {
        return UpdateManager(context)
    }

    /**
     * 下载APK文件
     * @param context 上下文
     * @param downloadUrl 下载链接
     * @return 下载进度Flow
     */
    fun downloadApk(context: Context, downloadUrl: String): Flow<UpdateManager.DownloadProgress> {
        val updateManager = createUpdateManager(context)
        return updateManager.downloadApk(downloadUrl)
    }

    /**
     * 安装APK
     * @param context 上下文
     * @return 是否成功启动安装
     */
    fun installApk(context: Context): Boolean {
        val updateManager = createUpdateManager(context)
        return updateManager.installApk()
    }

    /**
     * 检查是否需要更新
     * @param context 上下文
     * @param serverVersionCode 服务器版本号
     * @return 是否需要更新
     */
    fun needUpdate(context: Context, serverVersionCode: Int): Boolean {
        val updateManager = createUpdateManager(context)
        return updateManager.needUpdate(serverVersionCode)
    }

    /**
     * 获取当前版本信息
     * @param context 上下文
     * @return Pair<版本号, 版本名称>
     */
    fun getCurrentVersion(context: Context): Pair<Long, String> {
        val updateManager = createUpdateManager(context)
        return Pair(updateManager.getCurrentVersionCode(), updateManager.getCurrentVersionName())
    }

    /**
     * 清理下载的APK文件
     * @param context 上下文
     */
    fun clearDownloadedApk(context: Context) {
        val updateManager = createUpdateManager(context)
        updateManager.clearDownloadedApk()
    }

    /**
     * 检查APK文件是否已存在
     * @return 是否存在APK文件
     */
    fun isApkFileExists(): Boolean {
        val updateManager = createUpdateManager(AppGlobals.applicationContext)
        return updateManager.isApkFileExists()
    }

    /**
     * 检查APK文件是否已完整下载
     * @param expectedSize 期望的文件大小（如果已知）
     * @return 是否完整下载
     */
    fun isApkFileComplete(expectedSize: Long = -1L): Boolean {
        val updateManager = createUpdateManager(AppGlobals.applicationContext)
        return updateManager.isApkFileComplete(expectedSize)
    }
}