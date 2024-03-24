package io.github.lightman314.lightmanscurrency.api.config;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.network.client.messages.config.SPacketSyncConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class SyncedConfigFile extends ConfigFile {

    private static final Map<Identifier,SyncedConfigFile> fileMap = new HashMap<>();

    @Override
    protected boolean isServerOnly() { return true; }

    public static void playerJoined(@NotNull ServerPlayerEntity player) {
        fileMap.values().forEach(c -> c.createSyncPacket().sendTo(player));
    }

    public static void handleSyncData(@NotNull Identifier configID, @NotNull Map<String,String> data)
    {
        if(fileMap.containsKey(configID))
            fileMap.get(configID).loadSyncData(data);
        else
            LightmansCurrency.LogError("Received config data for '" + configID + "' from the server, however this config is not present on the client!");
    }

    public static void onClientLeavesServer() { fileMap.values().forEach(SyncedConfigFile::clearSyncedData); }

    protected final Identifier id;

    protected SyncedConfigFile(@NotNull String fileName, @NotNull Identifier id) {
        super(fileName, LoadPhase.GAME_START); //Lock load phase as game start to ensure the packet can be sent correctly.
        this.id = id;
        if(fileMap.containsKey(this.id))
            throw new IllegalArgumentException("Synced Config " + this.id + " already exists!");
        fileMap.put(this.id, this);
    }

    private boolean loadedSyncData = false;
    @Override
    public boolean isLoaded() { return super.isLoaded() || this.loadedSyncData; }
    @Override
    protected void afterReload() { this.createSyncPacket().sendToAll(); }

    public final void clearSyncedData() { this.forEach(ConfigOption::clearSyncedData); this.loadedSyncData = false; }

    @NotNull
    private Map<String,String> getSyncData()
    {
        Map<String,String> map = new HashMap<>();
        this.getAllOptions().forEach((id, option) -> map.put(id, option.write()));
        return ImmutableMap.copyOf(map);
    }

    private ServerToClientPacket createSyncPacket() { return new SPacketSyncConfig(this.id, this.getSyncData()); }

    private void loadSyncData(@NotNull Map<String,String> syncData)
    {
        LightmansCurrency.LogInfo("Received config data for '" + this.id + "' from the server!");
        this.getAllOptions().forEach((id, option) -> {
            if(syncData.containsKey(id))
                option.load(id, syncData.get(id), true);
            else
                LightmansCurrency.LogWarning("Received data for config option '" + id + "' but it is not present on the client!");
        });
    }

}
