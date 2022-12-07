package io.github.lightman314.lightmanscurrency.common.enchantments;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class EnchantmentEvents {

    public static void registerEventListeners() {
        ServerTickEvents.START_SERVER_TICK.register(EnchantmentEvents::serverTick);
    }

    private static void serverTick(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            MoneyMendingEnchantment.runPlayerTick(player);
            CoinMagnetEnchantment.runPlayerTick(player);
        });
    }

}
