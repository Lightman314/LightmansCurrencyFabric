package io.github.lightman314.lightmanscurrency.network.client;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyData;
import io.github.lightman314.lightmanscurrency.network.client.messages.admin.SMessageSyncAdminList;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.SMessageATMPlayerAccountResponse;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.SMessageBankTransferResponse;
import io.github.lightman314.lightmanscurrency.network.client.messages.blockentity.SMessageSendArmorStandID;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.ejectiondata.SMessageChangeSelectedData;
import io.github.lightman314.lightmanscurrency.network.client.messages.enchantments.SMessageMoneyMendingClink;
import io.github.lightman314.lightmanscurrency.network.client.messages.notifications.SMessageClientNotification;
import io.github.lightman314.lightmanscurrency.network.client.messages.team.SMessageCreateTeamResponse;
import io.github.lightman314.lightmanscurrency.network.client.messages.trader.SMessageStorageInteraction;
import io.github.lightman314.lightmanscurrency.network.client.messages.trader.SMessageSyncUserCount;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayChannelHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class LCClientPacketHandler implements PlayChannelHandler {

	private final Map<Identifier,IClientPacketHandler> PACKET_HANDLERS = new HashMap<>();

	public LCClientPacketHandler() {
		//Register built-in packets

		//Admin
		this.registerPacketType(SMessageSyncAdminList.PACKET_ID, SMessageSyncAdminList::handle);

		//Bank
		this.registerPacketType(SMessageATMPlayerAccountResponse.PACKET_ID, SMessageATMPlayerAccountResponse::handle);
		this.registerPacketType(SMessageBankTransferResponse.PACKET_ID, SMessageBankTransferResponse::handle);

		//Block Entity
		this.registerPacketType(SMessageSendArmorStandID.PACKET_ID, SMessageSendArmorStandID::handle);

		//Enchantments
		this.registerPacketType(SMessageMoneyMendingClink.PACKET_ID, SMessageMoneyMendingClink::handle);

		//Trader
		this.registerPacketType(SMessageSyncUserCount.PACKET_ID, SMessageSyncUserCount::handle);
		this.registerPacketType(SMessageStorageInteraction.PACKET_ID, SMessageStorageInteraction::handle);

		//Data synchronization
		this.registerPacketType(SMessageUpdateClientNotifications.PACKET_ID, SMessageUpdateClientNotifications::handle);
		this.registerPacketType(SMessageClearClientTraders.PACKET_ID, SMessageClearClientTraders::handle);
		this.registerPacketType(SMessageRemoveClientTrader.PACKET_ID, SMessageRemoveClientTrader::handle);
		this.registerPacketType(SMessageUpdateClientTrader.PACKET_ID, SMessageUpdateClientTrader::handle);
		this.registerPacketType(SMessageUpdateEjectionData.PACKET_ID, SMessageUpdateEjectionData::handle);
		this.registerPacketType(SMessageUpdateClientBank.PACKET_ID, SMessageUpdateClientBank::handle);
		this.registerPacketType(SMessageSetupClientBank.PACKET_ID, SMessageSetupClientBank::handle);
		this.registerPacketType(SMessageSyncSelectedBankAccount.PACKET_ID, SMessageSyncSelectedBankAccount::handle);
		this.registerPacketType(SMessageUpdateClientTeam.PACKET_ID, SMessageUpdateClientTeam::handle);
		this.registerPacketType(SMessageRemoveClientTeam.PACKET_ID, SMessageRemoveClientTeam::handle);
		this.registerPacketType(SMessageSetupClientTeam.PACKET_ID, SMessageSetupClientTeam::handle);
		this.registerPacketType(SMessageUpdateClientWallet.PACKET_ID, SMessageUpdateClientWallet::handle);
		this.registerPacketType(SMessageSetupClientWallet.PACKET_ID, SMessageSetupClientWallet::handle);

		//Money Data
		this.registerPacketType(MoneyData.PACKET_ID, MoneyData::handle);
		//ATM Data
		this.registerPacketType(ATMData.PACKET_ID, ATMData::handle);

		//Notifications
		this.registerPacketType(SMessageClientNotification.PACKET_ID, SMessageClientNotification::handle);

		//Team
		this.registerPacketType(SMessageCreateTeamResponse.PACKET_ID, SMessageCreateTeamResponse::handle);

		//Ejection Data
		this.registerPacketType(SMessageChangeSelectedData.PACKET_ID, SMessageChangeSelectedData::handle);

	}

	@Override
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		try {
			Identifier type = new Identifier(buffer.readString());
			LightmansCurrency.LogDebug("Received client packet of type '" + type + "'!");
			if(PACKET_HANDLERS.containsKey(type))
				PACKET_HANDLERS.get(type).handle(client, handler, buffer, responseSender);
			else
				throw new RuntimeException("No packet handler was registered for Server -> Client packet type '" + type + "'!");
		} catch(Throwable t) { LightmansCurrency.LogError("Error handling client packet!", t); }
	}

	public void registerPacketType(Identifier type, IClientPacketHandler handler) {
		if(this.PACKET_HANDLERS.containsKey(type))
		{
			LightmansCurrency.LogWarning("LC Client Packet Handler already contains a registration for packet handler of type '" + type.toString() + "'.\nStacktrace: " + new Throwable());
			return;
		}
		PACKET_HANDLERS.put(type, handler);
	}
	
	public interface IClientPacketHandler {
		void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender);
	}
	
}
