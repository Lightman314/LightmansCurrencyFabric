package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import io.github.lightman314.lightmanscurrency.common.callbacks.EntityDeathCallback;
import io.github.lightman314.lightmanscurrency.common.callbacks.MobInitializationCallback;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.core.*;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.enchantments.EnchantmentEvents;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.common.money.DefaultMoneyDataCollection;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.item.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.paygate.*;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.settings.*;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataArmor;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataTicket;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.rules.types.*;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.*;
import io.github.lightman314.lightmanscurrency.integration.trinketsapi.LCTrinketsAPI;
import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import io.github.lightman314.lightmanscurrency.network.client.messages.time.SMessageSyncTime;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lightman314.lightmanscurrency.network.server.LCServerPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class LightmansCurrency implements ModInitializer {
	
	public static final String MODID = "lightmanscurrency";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {

        LightmansCurrency.LogInfo("Registering Lightman's Currency Items/Blocks");

        //WoodType Registration
        //Not yet implemented in this version of LC

		//Trigger Registration
		ModItems.registerItems();
		ModBlocks.registerBlocks();
		ModBlockEntities.registerBlockEntities();
        ModMenus.registerMenus();
        ModEnchantments.registerEnchantments();
        ModPointsOfInterest.registerPointsOfInterest();
        ModProfessions.registerProfessions();
		ModCommandArguments.registerCommandArguments();
		ModRecipes.registerRecipes();
		ModSounds.registerSounds();
        ModCreativeGroups.registerItemGroups();
        ModGameRules.registerGameRules();

        LightmansCurrency.LogInfo("Done registering Lightman's Currency Items/Blocks");

        //Register Configs
        LCConfig.init();
        //Manually reload common config here for villager trade config options
        LCConfig.COMMON.reload();

        //Register villager trades
        VillagerTradeManager.registerVillagerTrades();

        //Setup Server Packet Listener
        ServerPlayNetworking.registerGlobalReceiver(PacketChannels.CLIENT_TO_SERVER, new LCServerPacketHandler());

        //Register Event Listeners
        this.registerEventListeners();
        EventListener.registerEventListeners();

        //TraderData deserializers
        TraderData.register(ItemTraderData.TYPE, ItemTraderData::new);
        TraderData.register(ItemTraderDataArmor.TYPE, ItemTraderDataArmor::new);
        TraderData.register(ItemTraderDataTicket.TYPE, ItemTraderDataTicket::new);
        TraderData.register(PaygateTraderData.TYPE, PaygateTraderData::new);
        TraderData.register(AuctionHouseTrader.TYPE, AuctionHouseTrader::new);
        TraderData.register(SlotMachineTraderData.TYPE, SlotMachineTraderData::new);

        //Initialize the Trade Rule deserializers
        TradeRule.RegisterDeserializer(PlayerWhitelist.TYPE, PlayerWhitelist::new);
        TradeRule.RegisterDeserializer(PlayerBlacklist.TYPE, PlayerBlacklist::new);
        TradeRule.RegisterDeserializer(PlayerTradeLimit.TYPE, PlayerTradeLimit::new);
        TradeRule.RegisterDeserializer(PlayerDiscounts.TYPE, PlayerDiscounts::new);
        TradeRule.RegisterDeserializer(TimedSale.TYPE, TimedSale::new);
        TradeRule.RegisterDeserializer(TradeLimit.TYPE, TradeLimit::new);
        TradeRule.RegisterDeserializer(FreeSample.TYPE, FreeSample::new);
        TradeRule.RegisterDeserializer(PriceFluctuation.TYPE, PriceFluctuation::new);

        //Initialize the Notification deserializers
        Notification.register(ItemTradeNotification.TYPE, ItemTradeNotification::new);
        Notification.register(PaygateNotification.TYPE, PaygateNotification::new);
        Notification.register(OutOfStockNotification.TYPE, OutOfStockNotification::new);
        Notification.register(LowBalanceNotification.TYPE, LowBalanceNotification::new);
        Notification.register(AuctionHouseSellerNotification.TYPE, AuctionHouseSellerNotification::new);
        Notification.register(AuctionHouseBuyerNotification.TYPE, AuctionHouseBuyerNotification::new);
        Notification.register(AuctionHouseSellerNobidNotification.TYPE, AuctionHouseSellerNobidNotification::new);
        Notification.register(AuctionHouseBidNotification.TYPE, AuctionHouseBidNotification::new);
        Notification.register(AuctionHouseCancelNotification.TYPE, AuctionHouseCancelNotification::new);
        Notification.register(TextNotification.TYPE, TextNotification::new);
        Notification.register(AddRemoveAllyNotification.TYPE, AddRemoveAllyNotification::new);
        Notification.register(AddRemoveTradeNotification.TYPE, AddRemoveTradeNotification::new);
        Notification.register(ChangeAllyPermissionNotification.TYPE, ChangeAllyPermissionNotification::new);
        Notification.register(ChangeCreativeNotification.TYPE, ChangeCreativeNotification::new);
        Notification.register(ChangeNameNotification.TYPE, ChangeNameNotification::new);
        Notification.register(ChangeOwnerNotification.TYPE, ChangeOwnerNotification::new);
        Notification.register(ChangeSettingNotification.SIMPLE_TYPE, ChangeSettingNotification.Simple::new);
        Notification.register(ChangeSettingNotification.ADVANCED_TYPE, ChangeSettingNotification.Advanced::new);
        Notification.register(DepositWithdrawNotification.PLAYER_TYPE, DepositWithdrawNotification.Player::new);
        Notification.register(DepositWithdrawNotification.TRADER_TYPE, DepositWithdrawNotification.Trader::new);
        Notification.register(DepositWithdrawNotification.SERVER_TYPE, DepositWithdrawNotification.Server::new);
        Notification.register(BankTransferNotification.TYPE, BankTransferNotification::new);
        Notification.register(SlotMachineTradeNotification.TYPE, SlotMachineTradeNotification::new);

        //Initialize the Notification Category deserializers
        NotificationCategory.register(NotificationCategory.GENERAL_TYPE, c -> NotificationCategory.GENERAL);
        NotificationCategory.register(NullCategory.TYPE, c -> NullCategory.INSTANCE);
        NotificationCategory.register(TraderCategory.TYPE, TraderCategory::new);
        NotificationCategory.register(BankCategory.TYPE, BankCategory::new);
        NotificationCategory.register(AuctionHouseCategory.TYPE, c -> AuctionHouseCategory.INSTANCE);

        //Register Trader Search Filters
        TraderSearchFilter.addFilter(new BasicSearchFilter());
        TraderSearchFilter.addFilter(new ItemTraderSearchFilter());

        ATMIconData.init();

	}

    private void registerEventListeners()
    {
        //Command Registration Event
        CommandRegistrationCallback.EVENT.register(CommandLoader::register);

        //Server Started Event
        ServerLifecycleEvents.SERVER_STARTED.register(ServerHook::collectServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerHook::onServerClose);
        //Add non-ServerHook listeners to the Server Hook event so that any subclasses can get the server from the ServerHook storage.
        ServerHook.addServerStartListener(server -> MoneyUtil.reloadMoneyData());
        ServerHook.addServerStartListener(server -> ATMData.reloadATMData());
        ServerHook.addServerStartListener(server -> ConfigFile.loadServerFiles(ConfigFile.LoadPhase.GAME_START));

        //Server Tick Event
        ServerTickEvents.START_SERVER_TICK.register(TraderSaveData::onServerTick);
        ServerTickEvents.START_SERVER_TICK.register(WalletSaveData::onServerTick);

        //Player Login Event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> MoneyUtil.getMoneyData().sendTo(sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ATMData.get().sendTo(sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> BankSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> EjectionSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TraderSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> TeamSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> WalletSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> WalletSaveData.OnPlayerDisconnect(handler.player));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> MoneyUtil.onPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> NotificationSaveData.OnPlayerLogin(handler.player, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommandLCAdmin.SendAdminList(sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> SMessageSyncTime.CreatePacket().sendTo(sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> SyncedConfigFile.playerJoined(handler.getPlayer()));

        //Killed by other entity event
        LootTableEvents.MODIFY.register(LootManager::onLootTableLoaded);
        EntityDeathCallback.EVENT.register(LootManager::entityDeath);

        //Entity spawn event
        MobInitializationCallback.EVENT.register((entity, world, difficulty, spawnReason, entityData, nbt) -> LootManager.onEntitySpawned(entity, spawnReason));

        //Money Data init event
        DefaultMoneyDataCollection.EVENT.register(MoneyUtil::initializeDefaultCoins);

        EnchantmentEvents.registerEventListeners();

        LootManager.setup();

        //Setup Trinkets Events (if loaded)
        LCTrinketsAPI.setupEventListeners();

    }

	public static void LogDebug(String message)
    {
    	LOGGER.debug(message);
    }
    
    public static void LogInfo(String message)
    {
        LOGGER.info(message);
    }
    
    public static void LogWarning(String message) { LOGGER.warn(message); }
    public static void LogWarning(String message, Object... objects) { LOGGER.warn(message, objects); }

    public static void LogError(String message, Object... objects)
    {
        LOGGER.error(message, objects);
    }
    
    public static void LogError(String message)
    {
        LOGGER.error(message);
    }

}