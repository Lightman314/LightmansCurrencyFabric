package io.github.lightman314.lightmanscurrency.network.server;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.server.messages.auction.CMessageSubmitBid;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.*;
import io.github.lightman314.lightmanscurrency.network.server.messages.blockentity.CMessageRequestArmorStandID;
import io.github.lightman314.lightmanscurrency.network.server.messages.blockentity.CPacketRequestNBT;
import io.github.lightman314.lightmanscurrency.network.server.messages.coinmint.CMessageMintCoin;
import io.github.lightman314.lightmanscurrency.network.server.messages.emergencyejection.CMessageChangeSelectedData;
import io.github.lightman314.lightmanscurrency.network.server.messages.notifications.CMessageFlagNotificationsSeen;
import io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata.CMessageAddPersistentAuction;
import io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata.CMessageAddPersistentTrader;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.*;
import io.github.lightman314.lightmanscurrency.network.server.messages.ticket_machine.CMessageCraftTicket;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.*;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.*;
import io.github.lightman314.lightmanscurrency.network.server.messages.traderinterface.CMessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.network.server.messages.traderinterface.CMessageInterfaceInteraction;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageSetWalletVisible;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageWalletInteraction;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class LCServerPacketHandler implements PlayChannelHandler {

	public static final Identifier CHANNEL = new Identifier(LightmansCurrency.MODID, "client_to_server");
	
	private final Map<Identifier,IServerPacketHandler> PACKET_HANDLERS = new HashMap<>();
	
	public LCServerPacketHandler() {
		//Register built-in packets

		//Auction House
		this.registerPacketType(CMessageSubmitBid.PACKET_ID, CMessageSubmitBid::handle);

		//Block Entity
		this.registerPacketType(CPacketRequestNBT.PACKET_ID, CPacketRequestNBT::handle);
		this.registerPacketType(CMessageRequestArmorStandID.PACKET_ID, CMessageRequestArmorStandID::handle);

		//Bank
		this.registerPacketType(CMessageSelectBankAccount.PACKET_ID, CMessageSelectBankAccount::handle);
		this.registerPacketType(CMessageBankInteraction.PACKET_ID, CMessageBankInteraction::handle);
		this.registerPacketType(CMessageATMExchange.PACKET_ID, CMessageATMExchange::handle);
		this.registerPacketType(CMessageSetBankNotificationLevel.PACKET_ID, CMessageSetBankNotificationLevel::handle);
		this.registerPacketType(CMessageATMSetPlayerAccount.PACKET_ID, CMessageATMSetPlayerAccount::handle);
		this.registerPacketType(CMessageBankTransferPlayer.PACKET_ID, CMessageBankTransferPlayer::handle);

		//CoinMint
		this.registerPacketType(CMessageMintCoin.PACKET_ID, CMessageMintCoin::handle);

		//Emergency Ejection
		this.registerPacketType(CMessageChangeSelectedData.PACKET_ID, CMessageChangeSelectedData::handle);

		//Persistent Data
		this.registerPacketType(CMessageAddPersistentTrader.PACKET_ID, CMessageAddPersistentTrader::handle);
		this.registerPacketType(CMessageAddPersistentAuction.PACKET_ID, CMessageAddPersistentAuction::handle);

		//Trader
		this.registerPacketType(CMessageTraderMessage.PACKET_ID, CMessageTraderMessage::handle);
		this.registerPacketType(CMessageStorageInteraction.PACKET_ID, CMessageStorageInteraction::handle);
		this.registerPacketType(CMessageOpenTrades.PACKET_ID, CMessageOpenTrades::handle);
		this.registerPacketType(CMessageOpenStorage.PACKET_ID, CMessageOpenStorage::handle);
		this.registerPacketType(CMessageAddOrRemoveTrade.PACKET_ID, CMessageAddOrRemoveTrade::handle);
		this.registerPacketType(CMessageExecuteTrade.PACKET_ID, CMessageExecuteTrade::handle);
		this.registerPacketType(CMessageCollectCoins.PACKET_ID, CMessageCollectCoins::handle);
		this.registerPacketType(CMessageStoreCoins.PACKET_ID, CMessageStoreCoins::handle);

		//Trader Interface
		this.registerPacketType(CMessageHandlerMessage.PACKET_ID, CMessageHandlerMessage::handle);
		this.registerPacketType(CMessageInterfaceInteraction.PACKET_ID, CMessageInterfaceInteraction::handle);

		//Team
		this.registerPacketType(CMessageCreateTeamBankAccount.PACKET_ID, CMessageCreateTeamBankAccount::handle);
		this.registerPacketType(CMessageSetTeamBankLimit.PACKET_ID, CMessageSetTeamBankLimit::handle);
		this.registerPacketType(CMessageEditTeam.PACKET_ID, CMessageEditTeam::handle);
		this.registerPacketType(CMessageRenameTeam.PACKET_ID, CMessageRenameTeam::handle);
		this.registerPacketType(CMessageDisbandTeam.PACKET_ID, CMessageDisbandTeam::handle);
		this.registerPacketType(CMessageCreateTeam.PACKET_ID, CMessageCreateTeam::handle);

		//Ticket Machine
		this.registerPacketType(CMessageCraftTicket.PACKET_ID, CMessageCraftTicket::handle);

		//Notifications
		this.registerPacketType(CMessageFlagNotificationsSeen.PACKET_ID, CMessageFlagNotificationsSeen::handle);

		//Wallet
		this.registerPacketType(CMessageOpenWalletMenu.PACKET_ID, CMessageOpenWalletMenu::handle);
		this.registerPacketType(CMessageOpenWalletBankMenu.PACKET_ID, CMessageOpenWalletBankMenu::handle);
		this.registerPacketType(CMessageWalletExchangeCoins.PACKET_ID, CMessageWalletExchangeCoins::handle);
		this.registerPacketType(CMessageWalletToggleAutoExchange.PACKET_ID, CMessageWalletToggleAutoExchange::handle);
		this.registerPacketType(CMessageWalletQuickCollect.PACKET_ID, CMessageWalletQuickCollect::handle);

		//Wallet Slot
		this.registerPacketType(CMessageSetWalletVisible.PACKET_ID, CMessageSetWalletVisible::handle);
		this.registerPacketType(CMessageWalletInteraction.PACKET_ID, CMessageWalletInteraction::handle);

	}
	
	@Override
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		try {
			Identifier type = new Identifier(buffer.readString());
			LightmansCurrency.LogInfo("Received server packet of type '" + type + "'!");
			if(PACKET_HANDLERS.containsKey(type))
				PACKET_HANDLERS.get(type).handle(server, player, handler, buffer, responseSender);
			else
				throw new RuntimeException("No packet handler was registered for Client -> Server packet type '" + type + "'!\nStacktrace: " + new Throwable());
		} catch(Throwable t) { LightmansCurrency.LogError("Error handling server packet!", t); }
	}
	
	private void registerPacketType(Identifier type, IServerPacketHandler handler) {
		if(PACKET_HANDLERS.containsKey(type))
		{
			LightmansCurrency.LogWarning("LC Server Packet Handler already contains a registration for packet handler of type '" + type.toString() + "'.");
			return;
		}
		PACKET_HANDLERS.put(type, handler);
	}
	
	public interface IServerPacketHandler {
		void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender);
	}
	

}
