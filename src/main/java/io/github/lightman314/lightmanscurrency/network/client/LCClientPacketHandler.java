package io.github.lightman314.lightmanscurrency.network.client;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.money.MoneyData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.messages.admin.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.auction.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.config.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.ejectiondata.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.enchantments.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.notifications.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.slot_machine.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.team.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.time.*;
import io.github.lightman314.lightmanscurrency.network.client.messages.trader.*;
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
		this.registerPacketType(SMessageSyncTime.PACKET_ID, SMessageSyncTime::handle);

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

		//Slot Machine
		this.registerPacketType(SMessageSlotMachine.PACKET_ID, SMessageSlotMachine::handle);

		//Auction House
		this.registerPacketType(SMessageAttemptBid.PACKET_ID, SMessageAttemptBid::handle);

		//Config
		this.registerPacketType(SPacketReloadConfig.PACKET_ID, SPacketReloadConfig::handle);
		this.registerPacketType(SPacketSyncConfig.PACKET_ID, SPacketSyncConfig::handle);

	}

	@Override
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		Identifier type = new Identifier(buffer.readString());
		LazyPacketData data = LazyPacketData.decode(buffer);
		client.execute(() -> {
			try {
				LightmansCurrency.LogDebug("Handling client packet of type '" + type + "'!");
				if(PACKET_HANDLERS.containsKey(type))
					PACKET_HANDLERS.get(type).handle(client, handler, data, responseSender);
				else
					throw new RuntimeException("No packet handler was registered for Server -> Client packet type '" + type + "'!");
			} catch(Throwable t) { LightmansCurrency.LogError("Error handling client packet!", t); }
		});
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
		void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender);
	}
	
}
