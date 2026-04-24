data class ShardInfo(
    val name: String,
    val remoteRoot: String,
    val remotePort: Int,
    val launcherExecutable: String,
    val clientExecutable: String,
    val clientFlags: String
)

class ShardConfigManager(private val context: Context) {
    private val configUrl = "https://raw.githubusercontent.com/Ohkthx/xIPL/main/shards"
    
    suspend fun fetchShardConfig(shardName: String): ShardInfo {
        // Parse shard configuration from remote
        val configText = httpClient.get("$configUrl/$shardName").text
        return parseConfig(configText)
    }
    
    fun parseConfig(configText: String): ShardInfo {
        // Parse bash config file into Kotlin data class
        // Extract: UO_SHARD_ROOT, UO_SHARD_PORT, etc.
    }
}
