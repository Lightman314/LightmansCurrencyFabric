package io.github.lightman314.lightmanscurrency.server;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerHook {

    private static MinecraftServer server = null;
    public static MinecraftServer getServer() { return server; }

    private static final List<Consumer<MinecraftServer>> listeners = new ArrayList<>();
    public static void addServerStartListener(Consumer<MinecraftServer> listener) { if(!listeners.contains(listener)) listeners.add(listener); }

    public static void collectServer(MinecraftServer server) {
        ServerHook.server = server;
        for(Consumer<MinecraftServer> listener : listeners)
        {
            if(listener != null)
                listener.accept(server);
        }
    }

    public static void onServerClose(MinecraftServer ignored) { ServerHook.server = null; }




}
