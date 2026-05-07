package ch.voronoi.GridWaveExamples;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class ExamplesPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ExamplesPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new PingCommand(this.getName(), this.getManifest().getVersion().toString()));
    }
}
