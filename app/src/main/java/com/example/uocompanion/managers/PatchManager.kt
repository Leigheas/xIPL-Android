class PatchManager(private val context: Context) {
    private val patcherDb: PatcherDatabase = Room.databaseBuilder(
        context,
        PatcherDatabase::class.java,
        "patcher_db"
    ).build()
    
    suspend fun downloadGameFiles(
        shardInfo: ShardInfo,
        onProgress: (current: Long, total: Long) -> Unit
    ) {
        val fileList = fetchFileManifest(shardInfo.remoteRoot)
        
        for (file in fileList) {
            downloadFile(
                remoteUrl = "${shardInfo.remoteRoot}/${file.path}",
                localPath = "${context.filesDir}/game/${file.path}",
                onProgress = onProgress
            )
        }
    }
    
    private suspend fun fetchFileManifest(remoteRoot: String): List<FileInfo> {
        // Similar to UOPatcher's file listing
        // Could use JSON manifest or scan remote directory
        return httpClient.get("$remoteRoot/manifest.json")
            .body<List<FileInfo>>()
    }
    
    private suspend fun downloadFile(
        remoteUrl: String,
        localPath: String,
        onProgress: (Long, Long) -> Unit
    ) {
        val file = File(localPath)
        file.parentFile?.mkdirs()
        
        httpClient.get(remoteUrl).body<ResponseBody>().use { body ->
            val totalSize = body.contentLength()
            file.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        onProgress(totalRead, totalSize)
                    }
                }
            }
        }
    }
}

class AppUpdateManager(private val context: Context) {
    private val updateCheckUrl = "https://raw.githubusercontent.com/Ohkthx/xIPL/main/version.json"
    
    data class VersionInfo(val version: String, val downloadUrl: String)
    
    suspend fun checkForUpdates(): VersionInfo? {
        val currentVersion = BuildConfig.VERSION_NAME
        val remoteVersion = httpClient.get(updateCheckUrl).body<VersionInfo>()
        
        return if (remoteVersion.version > currentVersion) {
            remoteVersion
        } else null
    }
    
    suspend fun downloadUpdate(versionInfo: VersionInfo) {
        val workRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setInputData(workDataOf("url" to versionInfo.downloadUrl))
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "app_update",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}
