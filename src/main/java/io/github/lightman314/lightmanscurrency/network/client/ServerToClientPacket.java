package io.github.lightman314.lightmanscurrency.network.client;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class ServerToClientPacket {

	private final Identifier type;
	protected ServerToClientPacket(Identifier type) { this.type = type; }
	
	protected final PacketByteBuf encode() {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(this.type.toString());
		LazyPacketData.Builder builder = LazyPacketData.builder();
		try{ this.encode(builder);
		} catch (Throwable t) { LightmansCurrency.LogError("Error encoding ServerToClient packet '" + this.type + "'", t); }
		builder.build().encode(buffer);
		return buffer;
	}
	protected abstract void encode(LazyPacketData.Builder dataBuilder);

	public final void sendToAll() {
		MinecraftServer server = ServerHook.getServer();
		if(server != null)
			this.sendTo(server.getPlayerManager().getPlayerList());
	}

	public final void sendTo(Iterable<ServerPlayerEntity> players) {
		for(ServerPlayerEntity player : players)
			this.sendTo(player);
	}

	public final void sendTo(PlayerEntity player) { if(player instanceof ServerPlayerEntity) this.sendTo((ServerPlayerEntity) player);}
	public final void sendTo(ServerPlayerEntity player) { ServerPlayNetworking.send(player, PacketChannels.SERVER_TO_CLIENT, this.encode()); }

	public final void sendTo(PacketSender packetSender) { packetSender.sendPacket(PacketChannels.SERVER_TO_CLIENT, this.encode()); }

	public static class Simple extends ServerToClientPacket
	{
		protected Simple(Identifier type) { super(type); }
		@Override
		protected final void encode(LazyPacketData.Builder dataBuilder) { }
	}

}
