package io.github.lightman314.lightmanscurrency.network;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.util.Identifier;

public class PacketChannels {

    public static final Identifier SERVER_TO_CLIENT = new Identifier(LightmansCurrency.MODID, "server_to_client");
    public static final Identifier CLIENT_TO_SERVER = new Identifier(LightmansCurrency.MODID, "client_to_server");

    public static final Identifier CONFIG_SYNC = new Identifier(LightmansCurrency.MODID, "config_synchronization");

}
