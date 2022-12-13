package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.common.core.groups.*;
import io.github.lightman314.lightmanscurrency.common.items.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.Reference.Color;
import io.github.lightman314.lightmanscurrency.common.Reference.WoodType;
import io.github.lightman314.lightmanscurrency.common.blocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderinterface.*;
import org.jetbrains.annotations.Nullable;

public class ModBlocks {

	//Item building templates
	public static final BiFunction<Block,ItemGroup,Item> BASIC_ITEM = (block,group) -> {
		FabricItemSettings settings = new FabricItemSettings();
		if(group != null)
			settings.group(group);
		return new BlockItem(block, settings);
	};
	public static final BiFunction<Block,ItemGroup,Item> COINBLOCK_ITEM = (block,group) -> {
		FabricItemSettings settings = new FabricItemSettings();
		if(group != null)
			settings.group(group);
		return new CoinBlockItem(block, settings);
	};
	public static final BiFunction<Block,ItemGroup,Item> COINBLOCK_ITEM_FIREPROOF = (block,group) -> {
		FabricItemSettings settings = new FabricItemSettings().fireproof();
		if(group != null)
			settings.group(group);
		return new CoinBlockItem(block, settings);
	};
	public static final BiFunction<Block,ItemGroup,Item> CASH_REGISTER_ITEM = (block,group) -> {
		FabricItemSettings settings = new FabricItemSettings();
		if(group != null)
			settings.group(group);
		return new CashRegisterItem(block,settings);
	};
	public static final BiFunction<Block,ItemGroup,Item> COIN_JAR_ITEM = (block,group) -> {
		FabricItemSettings settings = new FabricItemSettings();
		if(group != null)
			settings.group(group);
		return new CoinJarItem(block,settings);
	};

	public static <T> Function<T,Identifier> BasicIDGeneration(String namespace, String name) {
		return (val) -> new Identifier(namespace, name + "_" + val.toString().toLowerCase());
	}

	public static <T> Function<T,Identifier> BasicIDGeneration(String namespace, String name, @Nullable T ignoreEntry) {
		return (val) -> val == ignoreEntry ? new Identifier(namespace, name) : new Identifier(namespace, name + "_" + val.toString().toLowerCase());
	}
	
	//Hold the blocks for public access
	//Coin piles
	public static final BlockItemPair COINPILE_COPPER = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_COPPER), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINPILE_IRON = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_IRON), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINPILE_GOLD = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_GOLD), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINPILE_DIAMOND = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_EMERALD), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINPILE_EMERALD = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_DIAMOND), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINPILE_NETHERITE = build(new CoinpileBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL).nonOpaque(), ModItems.COIN_NETHERITE), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM_FIREPROOF);
	
	//Coin blocks
	public static final BlockItemPair COINBLOCK_COPPER = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_COPPER), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINBLOCK_IRON = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_IRON), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINBLOCK_GOLD = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_GOLD), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINBLOCK_EMERALD = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_EMERALD), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINBLOCK_DIAMOND = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_DIAMOND), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM);
	public static final BlockItemPair COINBLOCK_NETHERITE = build(new CoinBlock(FabricBlockSettings.of(Material.METAL).strength(3f, 6f).sounds(BlockSoundGroup.METAL), ModItems.COIN_NETHERITE), LightmansCurrency.COIN_GROUP, COINBLOCK_ITEM_FIREPROOF);
	
	//Machines
	//Misc Machines
	public static final BlockItemPair MACHINE_ATM = build(new ATMBlock(FabricBlockSettings.of(Material.METAL).strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.MACHINE_GROUP);
	public static final BlockItemPair MACHINE_MINT = build(new CoinMintBlock(FabricBlockSettings.of(Material.METAL).strength(2f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.MACHINE_GROUP);
	//Cash Register
	public static final BlockItemPair CASH_REGISTER = build(new CashRegisterBlock(FabricBlockSettings.of(Material.METAL).strength(3f,6f).sounds(BlockSoundGroup.METAL).nonOpaque(), Block.createCuboidShape(1d,0d,1d,15d,10d,15d)), LightmansCurrency.MACHINE_GROUP, CASH_REGISTER_ITEM);

	//Item Traders
	//Display Case
	public static final BlockItemPair DISPLAY_CASE = build(new DisplayCaseBlock(FabricBlockSettings.of(Material.METAL).strength(2f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.GLASS).nonOpaque()), LightmansCurrency.TRADING_GROUP);

	//Vending Machines
	public static final BlockBundle<Color> VENDING_MACHINE = build(() -> new VendingMachineBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.TRADING_GROUP, Color.values());

	//Large Vending Machines
	public static final BlockBundle<Color> VENDING_MACHINE_LARGE = build(() -> new VendingMachineLargeBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.TRADING_GROUP, Color.values());

	//Wooden Shelves
	public static final BlockBundle<WoodType> SHELF = build(() -> new ShelfBlock(FabricBlockSettings.of(Material.WOOD).strength(2f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.WOOD).nonOpaque()), LightmansCurrency.TRADING_GROUP, WoodType.values());

	//Card Shelves
	public static final BlockBundle<WoodType> CARD_DISPLAY = build(() -> new CardDisplayBlock(FabricBlockSettings.of(Material.WOOD).strength(2f,Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.WOOD).nonOpaque()), LightmansCurrency.TRADING_GROUP, WoodType.values());

	//Armor Display
	public static final BlockItemPair ARMOR_DISPLAY = build(new ArmorDisplayBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.TRADING_GROUP);

	//Freezer
	public static final BlockItemPair FREEZER = build(new FreezerBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.TRADING_GROUP);

	//Ticket Kiosk
	public static final BlockItemPair TICKET_KIOSK = build(new TicketKioskBlock(FabricBlockSettings.of(Material.METAL).strength(3f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque()), LightmansCurrency.TRADING_GROUP);

	//Network Traders
	public static final BlockItemPair ITEM_NETWORK_TRADER_1 = build(new NetworkItemTraderBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque(), NetworkItemTraderBlock.TRADE_COUNT_T1), LightmansCurrency.TRADING_GROUP);
	public static final BlockItemPair ITEM_NETWORK_TRADER_2 = build(new NetworkItemTraderBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque(), NetworkItemTraderBlock.TRADE_COUNT_T2), LightmansCurrency.TRADING_GROUP);
	public static final BlockItemPair ITEM_NETWORK_TRADER_3 = build(new NetworkItemTraderBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque(), NetworkItemTraderBlock.TRADE_COUNT_T3), LightmansCurrency.TRADING_GROUP);
	public static final BlockItemPair ITEM_NETWORK_TRADER_4 = build(new NetworkItemTraderBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL).nonOpaque(), NetworkItemTraderBlock.TRADE_COUNT_T4), LightmansCurrency.TRADING_GROUP);

	//Trader Interface
	public static final BlockItemPair ITEM_TRADER_INTERFACE = build(new ItemTraderInterfaceBlock(FabricBlockSettings.of(Material.METAL).strength(5f, Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL)), LightmansCurrency.MACHINE_GROUP);

	//Terminal
	public static final BlockItemPair TERMINAL = build(new TerminalBlock(FabricBlockSettings.of(Material.METAL).strength(3f,6f).sounds(BlockSoundGroup.METAL).nonOpaque(),Block.createCuboidShape(1d,0d,1d,15d,15d,15d)), LightmansCurrency.MACHINE_GROUP);
	public static final BlockItemPair GEM_TERMINAL = build(new TerminalBlock(FabricBlockSettings.of(Material.AMETHYST).strength(3f,6f).sounds(BlockSoundGroup.AMETHYST_CLUSTER).nonOpaque(), Block.createCuboidShape(2d,0d,2d,14d,12d,14d)),LightmansCurrency.MACHINE_GROUP);

	//Paygate
	public static final BlockItemPair PAYGATE = build(new PaygateBlock(FabricBlockSettings.of(Material.METAL).strength(3f,Float.POSITIVE_INFINITY).sounds(BlockSoundGroup.METAL)),LightmansCurrency.MACHINE_GROUP);

	//Ticket Machine
	public static final BlockItemPair TICKET_MACHINE = build(new TicketMachineBlock(FabricBlockSettings.of(Material.METAL).strength(3f,6f).sounds(BlockSoundGroup.METAL).nonOpaque()),LightmansCurrency.MACHINE_GROUP);

	//Coin Jars
	public static final BlockItemPair PIGGY_BANK = build(new CoinJarBlock(FabricBlockSettings.of(Material.STONE).strength(0.1f,2f).sounds(BlockSoundGroup.STONE).nonOpaque(), Block.createCuboidShape(4d,0d,4d,12d,8d,12d)), ItemGroup.DECORATIONS, COIN_JAR_ITEM);
	public static final BlockItemPair COINJAR_BLUE = build(new CoinJarBlock(FabricBlockSettings.of(Material.STONE).strength(0.1f,2f).sounds(BlockSoundGroup.STONE).nonOpaque(), Block.createCuboidShape(4d,0d,4d,12d,8d,12d)), ItemGroup.DECORATIONS, COIN_JAR_ITEM);

	public static void registerBlocks() {
		//Coin Piles
		register(new Identifier(LightmansCurrency.MODID, "coinpile_copper"), COINPILE_COPPER);
		register(new Identifier(LightmansCurrency.MODID, "coinpile_iron"), COINPILE_IRON);
		register(new Identifier(LightmansCurrency.MODID, "coinpile_gold"), COINPILE_GOLD);
		register(new Identifier(LightmansCurrency.MODID, "coinpile_emerald"), COINPILE_EMERALD);
		register(new Identifier(LightmansCurrency.MODID, "coinpile_diamond"), COINPILE_DIAMOND);
		register(new Identifier(LightmansCurrency.MODID, "coinpile_netherite"), COINPILE_NETHERITE);
		
		//Coin Blocks
		register(new Identifier(LightmansCurrency.MODID, "coinblock_copper"), COINBLOCK_COPPER);
		register(new Identifier(LightmansCurrency.MODID, "coinblock_iron"), COINBLOCK_IRON);
		register(new Identifier(LightmansCurrency.MODID, "coinblock_gold"), COINBLOCK_GOLD);
		register(new Identifier(LightmansCurrency.MODID, "coinblock_emerald"), COINBLOCK_EMERALD);
		register(new Identifier(LightmansCurrency.MODID, "coinblock_diamond"), COINBLOCK_DIAMOND);
		register(new Identifier(LightmansCurrency.MODID, "coinblock_netherite"), COINBLOCK_NETHERITE);
		
		//Machines
		register(new Identifier(LightmansCurrency.MODID, "atm"), MACHINE_ATM);
		register(new Identifier(LightmansCurrency.MODID, "coinmint"), MACHINE_MINT);
		register(new Identifier(LightmansCurrency.MODID, "cash_register"), CASH_REGISTER);

		//Item Traders
		//Display Case
		register(new Identifier(LightmansCurrency.MODID, "display_case"), DISPLAY_CASE);
		
		//Vending Machine
		register(VENDING_MACHINE, BasicIDGeneration(LightmansCurrency.MODID, "vending_machine", Color.WHITE));

		//Large Vending Machine
		register(VENDING_MACHINE_LARGE, BasicIDGeneration(LightmansCurrency.MODID, "vending_machine_large", Color.WHITE));

		//Shelves
		register(SHELF, BasicIDGeneration(LightmansCurrency.MODID, "shelf"));
		
		//Card Display
		register(CARD_DISPLAY, BasicIDGeneration(LightmansCurrency.MODID, "card_display"));

		//Armor Display
		register(new Identifier(LightmansCurrency.MODID, "armor_display"), ARMOR_DISPLAY);

		//Freezer
		register(new Identifier(LightmansCurrency.MODID, "freezer"), FREEZER);
		
		//Ticket Kiosk
		register(new Identifier(LightmansCurrency.MODID, "ticket_kiosk"), TICKET_KIOSK);

		//Network Traders
		register(new Identifier(LightmansCurrency.MODID, "item_trader_server_sml"), ITEM_NETWORK_TRADER_1);
		register(new Identifier(LightmansCurrency.MODID, "item_trader_server_med"), ITEM_NETWORK_TRADER_2);
		register(new Identifier(LightmansCurrency.MODID, "item_trader_server_lrg"), ITEM_NETWORK_TRADER_3);
		register(new Identifier(LightmansCurrency.MODID, "item_trader_server_xlrg"), ITEM_NETWORK_TRADER_4);

		//Trader Interface
		register(new Identifier(LightmansCurrency.MODID, "item_trader_interface"), ITEM_TRADER_INTERFACE);
		
		//Terminal
		register(new Identifier(LightmansCurrency.MODID,"terminal"), TERMINAL);
		//Gem Terminal
		register(new Identifier(LightmansCurrency.MODID, "gem_terminal"), GEM_TERMINAL);
		
		//Paygate
		register(new Identifier(LightmansCurrency.MODID, "paygate"), PAYGATE);
		
		//Ticket Machine
		register(new Identifier(LightmansCurrency.MODID, "ticket_machine"), TICKET_MACHINE);
		
		//Coin Jars
		register(new Identifier(LightmansCurrency.MODID, "piggy_bank"), PIGGY_BANK);
		register(new Identifier(LightmansCurrency.MODID, "coinjar_blue"), COINJAR_BLUE);
		
	}
	
	/**
	* Block Item Pair Building Code
	*/
	private static BlockItemPair build(Block block, ItemGroup group) { return build(block, group, BASIC_ITEM); }
	
	private static BlockItemPair build(Block block, ItemGroup group, BiFunction<Block,ItemGroup,Item> itemGenerator) {
		Item item = itemGenerator.apply(block, group);
		return new BlockItemPair(block,item);
	}

	private static <L> BlockBundle<L> build(Supplier<Block> blockSupplier, ItemGroup group, L[] types) { return build(blockSupplier, group, types, BASIC_ITEM); }

	private static <L> BlockBundle<L> build(Supplier<Block> blockSupplier, ItemGroup group, L[] types, BiFunction<Block,ItemGroup,Item> itemGenerator) {
		BlockBundle<L> result = new BlockBundle<>();
		for(L type : types)
		{
			Block block = blockSupplier.get();
			Item item = itemGenerator.apply(block,group);
			result.put(type,new BlockItemPair(block,item));
		}
		return result;
	}
	
	/**
	 * Block Item Pair Registration Code
	 */
	private static void register(Identifier type, BlockItemPair pair) {
		if(pair.block != null)
			Registry.register(Registry.BLOCK, type, pair.block);
		if(pair.item != null)
			Registry.register(Registry.ITEM, type, pair.item);
	}
	
	private static <L> void register(ObjectBundle<BlockItemPair,L> group, Function<L,Identifier> idGenerator) {
		group.foreach((key, pair) -> register(idGenerator.apply(key), pair));
	}
	
}