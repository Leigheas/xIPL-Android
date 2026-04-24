class GameLauncher(private val context: Context) {
    
    fun launchGame(gameClientPath: String, args: String = "") {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("classicuo://launch?path=$gameClientPath&args=$args")
            
            // If ClassicUO is installed as separate app
            setPackage("com.classicuo")
        }
        
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback: open game client directly or show install dialog
            showInstallGameDialog()
        }
    }
    
    private fun showInstallGameDialog() {
        // Guide user to install ClassicUO or native UO client
    }
}
