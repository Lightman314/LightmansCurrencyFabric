package io.github.lightman314.lightmanscurrency.common.money.wallet;

import io.github.lightman314.lightmanscurrency.client.data.ClientWalletData;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageSetupClientWallet;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientWallet;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WalletSaveData extends PersistentState {

    private final Map<UUID,WalletHandler> playerWalletData = new HashMap<>();
    private final List<UUID> relevantPlayers = new ArrayList<>();

    private WalletSaveData() {}

    private WalletSaveData(NbtCompound compound)
    {
        NbtList walletData = compound.getList("PlayerWalletData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < walletData.size(); ++i)
        {
            NbtCompound tag = walletData.getCompound(i);
            UUID player = tag.getUuid("Player");
            WalletHandler handler = new WalletHandler();
            handler.load(tag.getCompound("WalletData"));
            playerWalletData.put(player, handler);
        }
    }

    public NbtCompound writeNbt(NbtCompound compound)
    {
        NbtList walletData = new NbtList();
        this.playerWalletData.forEach((player, handler) -> {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("Player", player);
            tag.put("WalletData", handler.save());
            walletData.add(tag);
        });
        compound.put("PlayerWalletData", walletData);

        return compound;
    }

    private static WalletSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(WalletSaveData::new, WalletSaveData::new, "lightmanscurrency_wallet_data");
        }
        return null;
    }

    @NotNull
    public static WalletHandler GetPlayerWallet(@NotNull PlayerEntity player)
    {
        if(player.getWorld().isClient)
        {
            return ClientWalletData.GetPlayerWallet(player);
        }
        else
        {
            WalletSaveData wsd = get();
            if(wsd != null)
            {
                if(wsd.playerWalletData.containsKey(Objects.requireNonNull(player.getUuid())))
                    return wsd.playerWalletData.get(player.getUuid()).updatePlayer(player);
                //Create new wallet handler for the new player
                WalletHandler newHandler = new WalletHandler();
                wsd.playerWalletData.put(player.getUuid(), newHandler);
                wsd.markWalletHandlerDirty(player.getUuid(), newHandler);
                return newHandler.updatePlayer(player);
            }
            return new WalletHandler();
        }
    }

    private void markWalletHandlerDirty(UUID playerID, WalletHandler handler) { this.markDirty(); new SMessageUpdateClientWallet(playerID, handler).sendToAll(); }

    public static void onServerTick(MinecraftServer server) {
        WalletSaveData wsd = get();
        if(wsd != null)
        {
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            {
                WalletHandler handler = GetPlayerWallet(player);
                handler.tick();
                if(handler.isDirty())
                {
                    handler.clean();
                    wsd.markWalletHandlerDirty(player.getUuid(), handler);
                }
            }
        }
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {
        WalletSaveData wsd = get();

        if(wsd == null)
            return;

        //Confirm the presence of the players wallet data
        GetPlayerWallet(player);

        NbtCompound compound = new NbtCompound();
        NbtList walletData = new NbtList();
        wsd.playerWalletData.forEach((playerID, handler) -> {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("Player", playerID);
            tag.put("WalletData", handler.save());
            walletData.add(tag);
        });
        compound.put("PlayerWalletData", walletData);

        //Add player to the relevant player list
        if(!wsd.relevantPlayers.contains(player.getUuid()))
            wsd.relevantPlayers.add(player.getUuid());

        new SMessageSetupClientWallet(compound).sendTo(sender);
    }

    public static void OnPlayerDisconnect(ServerPlayerEntity player)
    {
        WalletSaveData wsd = get();
        if(wsd == null)
            return;
        //One last check for changed wallet data before they leave
        WalletHandler handler = GetPlayerWallet(player);
        if(handler.isDirty())
        {
            handler.clean();
            wsd.markWalletHandlerDirty(player.getUuid(), handler);
        }

        //Clear the player id from the relevant player list
        wsd.relevantPlayers.remove(player.getUuid());

    }

}
