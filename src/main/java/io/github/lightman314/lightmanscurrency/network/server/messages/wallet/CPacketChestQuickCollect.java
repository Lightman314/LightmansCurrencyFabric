package io.github.lightman314.lightmanscurrency.network.server.messages.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CPacketChestQuickCollect extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "wallet_chest_collection");

    private final boolean allowHidden;
    private CPacketChestQuickCollect(boolean allowHidden) { super(PACKET_ID); this.allowHidden = allowHidden; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setBoolean("allowHidden", this.allowHidden); }

    public static void send() { new CPacketChestQuickCollect(LCConfig.CLIENT.chestButtonAllowSideChains.get()).sendToServer(); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof GenericContainerScreenHandler menu)
        {
            WalletItem.QuickCollect(player, menu.getInventory(), data.getBoolean("allowHidden"));
        }
    }

}
