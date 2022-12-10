package io.github.lightman314.lightmanscurrency.network.server;

import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class ClientToServerPacket {

	private final Identifier type;
	protected ClientToServerPacket(Identifier type) { this.type = type; }
	
	protected final PacketByteBuf encode() {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(this.type.toString());
		this.encode(buffer);
		return buffer;
	}
	protected abstract void encode(PacketByteBuf buffer);
	
	public final void sendToServer() { ClientPlayNetworking.send(PacketChannels.CLIENT_TO_SERVER, this.encode()); }
	
}
