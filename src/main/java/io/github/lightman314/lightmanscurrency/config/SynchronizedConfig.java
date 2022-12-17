package io.github.lightman314.lightmanscurrency.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedConfig extends Config{

    static final Map<Identifier,SynchronizedConfig> configMap = new HashMap<>();

    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static void register(Identifier id, SynchronizedConfig synchronizedConfig) { configMap.put(id, synchronizedConfig); }

    public static void loadFromPacket(Identifier id, JsonObject json) {
        if(configMap.containsKey(id))
        {
            LightmansCurrency.LogInfo("Reloading '" + id + "' config as a sync packet was sent by the server.");
            SynchronizedConfig config = configMap.get(id);
            config.reloadFromJson(json);
        }
        else
            LightmansCurrency.LogWarning("Received a config sync packet from the server, but no config of type '" + id + "' is present on the client.");
    }

    public final Identifier id;

    public SynchronizedConfig(String fileName, Identifier id, ConfigBuilder builder) { this(fileName, false, id, builder); }

    public SynchronizedConfig(String fileName, boolean overrideFolder, Identifier id, ConfigBuilder builder) {
        super(fileName, overrideFolder, builder);
        this.id = id;
        register(this.id, this);
    }

    @Override
    protected void afterReload() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            PacketByteBuf buffer = this.writeSyncPacket();
            server.getPlayerManager().getPlayerList().forEach(p -> ServerPlayNetworking.send(p, PacketChannels.CONFIG_SYNC, buffer));
        }
    }

    protected final PacketByteBuf writeSyncPacket() {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeIdentifier(this.id);
        String json = GSON.toJson(this.getAsJson());
        buffer.writeInt(json.length());
        buffer.writeString(json, json.length());
        return buffer;
    }

    public static void sendConfigSyncPackets(PacketSender sender) {
        for(SynchronizedConfig config : configMap.values())
            sender.sendPacket(PacketChannels.CONFIG_SYNC, config.writeSyncPacket());
    }

}
