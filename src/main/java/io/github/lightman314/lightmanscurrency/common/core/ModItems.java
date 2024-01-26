package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class ModItems {
	
	//Hold the items for public access
	public static final CoinItem COIN_COPPER = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final CoinItem COIN_IRON = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final CoinItem COIN_GOLD = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final CoinItem COIN_EMERALD = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final CoinItem COIN_DIAMOND = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final CoinItem COIN_NETHERITE = new CoinItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP).fireproof());
	
	public static final Item TRADING_CORE = new Item(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final Item TICKET = new TicketItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final Item TICKET_MASTER = new TicketItem(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP).rarity(Rarity.RARE).maxCount(1));
	public static final Item TICKET_STUB = new Item(new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	
	public static final WalletItem WALLET_COPPER = new WalletItem(0, 6, "wallet_copper", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final WalletItem WALLET_IRON = new WalletItem(1, 12, "wallet_iron", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final WalletItem WALLET_GOLD = new WalletItem(2, 18, "wallet_gold", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final WalletItem WALLET_EMERALD = new WalletItem(3, 24, "wallet_emerald", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final WalletItem WALLET_DIAMOND = new WalletItem(4, 30, "wallet_diamond", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP));
	public static final WalletItem WALLET_NETHERITE = new WalletItem(5, 36, "wallet_netherite", new FabricItemSettings().group(LightmansCurrency.COIN_GROUP).fireproof());
	
	public static final PortableTerminalItem PORTABLE_TERMINAL = new PortableTerminalItem(new FabricItemSettings().group(LightmansCurrency.MACHINE_GROUP));
	public static final PortableTerminalItem PORTABLE_GEM_TERMINAL = new PortableTerminalItem(new FabricItemSettings().group(LightmansCurrency.MACHINE_GROUP));
	public static final PortableATMItem PORTABLE_ATM = new PortableATMItem(new FabricItemSettings().group(LightmansCurrency.MACHINE_GROUP));

	public static final CapacityUpgradeItem ITEM_CAPACITY_UPGRADE_1 = new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade1, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final CapacityUpgradeItem ITEM_CAPACITY_UPGRADE_2 = new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade2, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final CapacityUpgradeItem ITEM_CAPACITY_UPGRADE_3 = new CapacityUpgradeItem(UpgradeType.ITEM_CAPACITY, LCConfig.SERVER.itemCapacityUpgrade3, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	
	public static final SpeedUpgradeItem SPEED_UPGRADE_1 = new SpeedUpgradeItem(4, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final SpeedUpgradeItem SPEED_UPGRADE_2 = new SpeedUpgradeItem(8, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final SpeedUpgradeItem SPEED_UPGRADE_3 = new SpeedUpgradeItem(12, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final SpeedUpgradeItem SPEED_UPGRADE_4 = new SpeedUpgradeItem(16, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	public static final SpeedUpgradeItem SPEED_UPGRADE_5 = new SpeedUpgradeItem(20, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	
	public static final UpgradeItem NETWORK_UPGRADE = new UpgradeItem.Simple(UpgradeType.NETWORK, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	
	public static final UpgradeItem HOPPER_UPGRADE = new UpgradeItem.Simple(UpgradeType.HOPPER, new FabricItemSettings().group(LightmansCurrency.UPGRADE_GROUP));
	
	//Hidden item(s)
	public static final Item FREEZER_DOOR = new Item(new FabricItemSettings());
		
	public static void registerItems() {
		
		//Coins
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_copper"), COIN_COPPER);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_iron"), COIN_IRON);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_gold"), COIN_GOLD);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_emerald"), COIN_EMERALD);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_diamond"), COIN_DIAMOND);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "coin_netherite"), COIN_NETHERITE);
		
		//Misc
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "trading_core"), TRADING_CORE);
		
		//Ticket
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "ticket"), TICKET);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "master_ticket"), TICKET_MASTER);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "ticket_stub"), TICKET_STUB);
		
		//Wallets
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_copper"), WALLET_COPPER);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_iron"), WALLET_IRON);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_gold"), WALLET_GOLD);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_emerald"), WALLET_EMERALD);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_diamond"), WALLET_DIAMOND);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "wallet_netherite"), WALLET_NETHERITE);
		
		//Portable Blocks
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "portable_terminal"), PORTABLE_TERMINAL);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "portable_gem_terminal"), PORTABLE_GEM_TERMINAL);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "portable_atm"), PORTABLE_ATM);
		
		//Item Capacity Upgrades
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "item_capacity_upgrade_1"), ITEM_CAPACITY_UPGRADE_1);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "item_capacity_upgrade_2"), ITEM_CAPACITY_UPGRADE_2);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "item_capacity_upgrade_3"), ITEM_CAPACITY_UPGRADE_3);
		
		//Speed Upgrades
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "speed_upgrade_1"), SPEED_UPGRADE_1);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "speed_upgrade_2"), SPEED_UPGRADE_2);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "speed_upgrade_3"), SPEED_UPGRADE_3);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "speed_upgrade_4"), SPEED_UPGRADE_4);
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "speed_upgrade_5"), SPEED_UPGRADE_5);
		
		//Network Upgrade
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "network_upgrade"), NETWORK_UPGRADE);
		
		//Hopper Upgrade
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "hopper_upgrade"), HOPPER_UPGRADE);
		
		//Freezer Door(s)
		Registry.register(Registry.ITEM, new Identifier(LightmansCurrency.MODID, "freezer_door"), FREEZER_DOOR);
		
	}
	
}