package io.github.lightman314.lightmanscurrency.network.client.messages.enchantments;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;

public class SMessageMoneyMendingClink extends ServerToClientPacket.Simple {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "enchantment_money_clink");

    private static long lastClink = 0;
    private static final long CLINK_DELAY = 1000;

    public SMessageMoneyMendingClink() { super(PACKET_ID); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {

        if(System.currentTimeMillis() - lastClink < CLINK_DELAY || !LCConfig.CLIENT.moneyMendingClink.get())
            return;
        lastClink = System.currentTimeMillis();
        //Play a coin clinking sound
        client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.COINS_CLINKING, 1f, 0.4f));

    }

}
