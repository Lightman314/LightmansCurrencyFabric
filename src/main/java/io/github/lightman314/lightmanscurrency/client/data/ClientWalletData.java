package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientWalletData {

    private static final Map<UUID, WalletHandler> playerWalletData = new HashMap<>();

    @NotNull
    public static WalletHandler GetPlayerWallet(PlayerEntity player) { return playerWalletData.getOrDefault(player.getUuid(), new WalletHandler()).updatePlayer(player); }

    public static void UpdateWallet(UUID playerID, WalletHandler newData) { playerWalletData.put(playerID, newData); }

    public static void InitializeWallets(Map<UUID,WalletHandler> walletData) {
        playerWalletData.clear();
        playerWalletData.putAll(walletData);
    }

    public static void onClientLogout(ClientPlayNetworkHandler handler, MinecraftClient client) {
        playerWalletData.clear();
    }


}
