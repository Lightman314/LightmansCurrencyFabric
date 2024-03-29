package io.github.lightman314.lightmanscurrency;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.config.ClientConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.*;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ScreenPositionOption;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import io.github.lightman314.lightmanscurrency.common.items.CoinItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public final class LCConfig {

    private LCConfig() {}

    public static void init() {}

    public static final Client CLIENT = new Client();
    public static final Common COMMON = new Common();
    public static final Server SERVER = new Server();

    public static class Client extends ClientConfigFile
    {

        private Client() { super("lightmanscurrency-client"); }

        public final IntOption itemRenderLimit = IntOption.create(Integer.MAX_VALUE, 0);

        public final StringOption timeFormat = StringOption.create("MM/dd/yy hh:mmaa");

        public final ScreenPositionOption walletSlot = ScreenPositionOption.create(76, 43);
        public final ScreenPositionOption walletSlotCreative = ScreenPositionOption.create(126,19);
        public final ScreenPositionOption walletButtonOffset = ScreenPositionOption.create(8,-10);

        public final BooleanOption walletOverlayEnabled = BooleanOption.createTrue();
        public final EnumOption<ScreenCorner> walletOverlayCorner = EnumOption.create(ScreenCorner.BOTTOM_LEFT);
        public final ScreenPositionOption walletOverlayPosition = ScreenPositionOption.create(5,-5);
        public final EnumOption<WalletDisplayOverlay.DisplayType> walletOverlayType = EnumOption.create(WalletDisplayOverlay.DisplayType.ITEMS_WIDE);

        public final ScreenPositionOption notificationAndTeamButtonPosition = ScreenPositionOption.create(152,3);
        public final ScreenPositionOption notificationAndTeamButtonCreativePosition = ScreenPositionOption.create(171,18);

        public final BooleanOption chestButtonVisible = BooleanOption.createTrue();
        public final BooleanOption chestButtonAllowSideChains = BooleanOption.createFalse();

        public final BooleanOption pushNotificationsToChat = BooleanOption.createTrue();

        public final IntOption slotMachineAnimationTime = IntOption.create(100, 20, 1200);
        public final IntOption slotMachineAnimationRestTime = IntOption.create(20, 0, 1200);

        public final BooleanOption moneyMendingClink = BooleanOption.createTrue();


        @Override
        protected void setup(@NotNull ConfigBuilder builder)
        {


            builder.comment("Quality Settings").push("quality");

            builder.comment("Maximum number of items each Item Trader can renderBG (per-trade) as stock. Lower to decrease client-lag in trader-rich areas.",
                            "Setting to 0 will disable item rendering entirely, so use with caution.")
                    .add("itemTraderRenderLimit", this.itemRenderLimit);

            builder.pop();

            builder.comment("Time Formatting Settings").push("time");

            builder.comment("How Notification Timestamps are displayed.","Follows SimpleDateFormat formatting: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html")
                    .add("timeFormatting", this.timeFormat);

            builder.pop();

            builder.comment("Wallet Slot Settings").push("wallet_slot");

            builder.comment("The position that the wallet slot will be placed at in the players inventory.")
                    .add("slot", this.walletSlot);

            builder.comment("The position that the wallet slot will be placed at in the players creative inventory.")
                    .add("creativeSlot", this.walletSlotCreative);

            builder.comment("The offset that the wallet button should be placed at relative to the wallet slot position.")
                    .add("button", this.walletButtonOffset);

            builder.pop();

            builder.comment("Wallet Overlay Settings").push("wallet_hud");

            builder.comment("Whether an overlay should be drawn on your HUD displaying your wallets current money amount.")
                    .add("enabled", this.walletOverlayEnabled);

            builder.comment("The corner of the screen that the overlay should be drawn on.")
                    .add("displayCorner", this.walletOverlayCorner);

            builder.comment("The position offset from the defined corner.")
                    .add("displayOffset", this.walletOverlayPosition);

            builder.comment("Whether the wallets contents should be displayed as a coin item, or as value text.")
                    .add("displayType", this.walletOverlayType);

            builder.pop();

            builder.comment("Inventory Button Settings").push("inventory_buttons");

            builder.comment("The position that the notification & team manager buttons will be placed at in the players inventory.")
                    .add("button", this.notificationAndTeamButtonPosition);

            builder.comment("The position that the notification & team manager buttons will be placed at in the players creative inventory.")
                    .add("buttonCreative", this.notificationAndTeamButtonCreativePosition);

            builder.pop();

            builder.comment("Chest Button Settings").push("chest_buttons");

            builder.comment("Whether the 'Move Coins into Wallet' button will appear in the top-right corner of the Chest Screen if there are coins in the chest that can be collected.")
                    .add("enabled", this.chestButtonVisible);

            builder.comment("Whether the 'Move Coins into Wallet' button should collect coins from a side-chain.",
                            "By default these would be the coin pile and coin block variants of the coins.")
                    .add("allowSideChainCollection", this.chestButtonAllowSideChains);

            builder.pop();

            builder.comment("Notification Settings").push("notification");

            builder.comment("Whether notifications should be posted in your chat when you receive them.")
                    .add("notificationsInChat", this.pushNotificationsToChat);

            builder.pop();

            builder.comment("Slot Machine Animation Settings").push("slot_machine");

            builder.comment("The number of Minecraft ticks the slot machine animation will last.",
                            "Note: 20 ticks = 1 second",
                            "Must be at least 20 ticks (1s) for coding reasons.")
                    .add("animationDuration", this.slotMachineAnimationTime);

            builder.comment("The number of Minecraft ticks the slot machine will pause before repeating the animation.")
                    .add("animationRestDuration", this.slotMachineAnimationRestTime);

            builder.pop();

            builder.comment("Sound Settings").push("sounds");

            builder.comment("Whether Money Mending should make a noise when triggered.")
                    .add("moneyMendingClink", this.moneyMendingClink);

            builder.pop();

        }

    }

    public static class Common extends ConfigFile
    {

        private Common() { super("lightmanscurrency-common", LoadPhase.NULL); }


        //Crafting Options
        public final BooleanOption canCraftNetworkTraders = BooleanOption.createTrue();
        public final BooleanOption canCraftTraderInterfaces = BooleanOption.createTrue();
        public final BooleanOption canCraftAuctionStands = BooleanOption.createTrue();

        //Custom Trades
        public final BooleanOption addCustomWanderingTrades = BooleanOption.createTrue();
        public final BooleanOption addBankerVillager = BooleanOption.createTrue();
        public final BooleanOption addCashierVillager = BooleanOption.createTrue();
        public final BooleanOption changeVanillaTrades = BooleanOption.createFalse();
        public final BooleanOption changeModdedTrades = BooleanOption.createFalse();
        public final BooleanOption changeWanderingTrades = BooleanOption.createFalse();
        public final ItemOption defaultVillagerReplacementCoin = ItemOption.create(ModItems.COIN_EMERALD, false);
        public final StringListOption villagerReplacementCoinOverrides = StringListOption.create(ImmutableList.of(
                "minecraft:butcher-lightmanscurrency:coin_iron",
                "minecraft:cartographer-lightmanscurrency:coin_iron",
                "minecraft:farmer-lightmanscurrency:coin_iron",
                "minecraft:fisherman-lightmanscurrency:coin_iron",
                "minecraft:fletcher-lightmanscurrency:coin_copper",
                "minecraft:leatherworker-lightmanscurrency:coin_iron",
                "minecraft:mason-lightmanscurrency:coin_iron",
                "minecraft:shepherd-lightmanscurrency:coin_iron"));

        //Loot Items
        public final ItemOption lootItem1 = ItemOption.create(ModItems.COIN_COPPER);
        public final ItemOption lootItem2 = ItemOption.create(ModItems.COIN_IRON);
        public final ItemOption lootItem3 = ItemOption.create(ModItems.COIN_GOLD);
        public final ItemOption lootItem4 = ItemOption.create(ModItems.COIN_EMERALD);
        public final ItemOption lootItem5 = ItemOption.create(ModItems.COIN_DIAMOND);
        public final ItemOption lootItem6 = ItemOption.create(ModItems.COIN_NETHERITE);

        //Entity Loot
        public final BooleanOption enableEntityDrops = BooleanOption.createTrue();
        public final BooleanOption allowSpawnerEntityDrops = BooleanOption.createFalse();
        public final BooleanOption allowFakePlayerCoinDrops = BooleanOption.createTrue();

        public final StringListOption entityDropsT1 = StringListOption.create(LootManager.ENTITY_COPPER_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption entityDropsT2 = StringListOption.create(LootManager.ENTITY_IRON_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption entityDropsT3 = StringListOption.create(LootManager.ENTITY_GOLD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption entityDropsT4 = StringListOption.create(LootManager.ENTITY_EMERALD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption entityDropsT5 = StringListOption.create(LootManager.ENTITY_DIAMOND_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption entityDropsT6 = StringListOption.create(LootManager.ENTITY_NETHERITE_DROPLIST.stream().map(Identifier::toString).toList());

        public final StringListOption bossEntityDropsT1 = StringListOption.create(LootManager.ENTITY_BOSS_COPPER_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption bossEntityDropsT2 = StringListOption.create(LootManager.ENTITY_BOSS_IRON_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption bossEntityDropsT3 = StringListOption.create(LootManager.ENTITY_BOSS_GOLD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption bossEntityDropsT4 = StringListOption.create(LootManager.ENTITY_BOSS_EMERALD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption bossEntityDropsT5 = StringListOption.create(LootManager.ENTITY_BOSS_DIAMOND_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption bossEntityDropsT6 = StringListOption.create(LootManager.ENTITY_BOSS_NETHERITE_DROPLIST.stream().map(Identifier::toString).toList());

        //Chest Loot
        public final BooleanOption enableChestLoot = BooleanOption.createTrue();

        public final StringListOption chestDropsT1 = StringListOption.create(LootManager.CHEST_COPPER_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption chestDropsT2 = StringListOption.create(LootManager.CHEST_IRON_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption chestDropsT3 = StringListOption.create(LootManager.CHEST_GOLD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption chestDropsT4 = StringListOption.create(LootManager.CHEST_EMERALD_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption chestDropsT5 = StringListOption.create(LootManager.CHEST_DIAMOND_DROPLIST.stream().map(Identifier::toString).toList());
        public final StringListOption chestDropsT6 = StringListOption.create(LootManager.CHEST_NETHERITE_DROPLIST.stream().map(Identifier::toString).toList());

        @Override
        protected void setup(@NotNull ConfigBuilder builder)
        {


            builder.comment("Crafting Settings","/reload required for any changes made to take effect.","FEATURE NOT IMPLEMENTED AT THIS TIME!").push("crafting");

            builder.comment("Whether Network Traders can be crafted.",
                            "Disabling will not remove any existing Network Traders from the world, nor prevent their use.",
                            "Disabling does NOT disable the recipes of Network Upgrades or the Trading Terminals.")
                    .add("canCraftNetworkTrader", this.canCraftNetworkTraders);

            builder.comment("Whether Trader Interface blocks can be crafted.",
                            "Disabling will not remove any existing Trader Interfaces from the world, nor prevent their use.")
                    .add("canCraftTraderInterface", this.canCraftTraderInterfaces);

            builder.comment("Whether Auction Stand blocks can be crafted.",
                            "Disabling will not remove any existing Auction Stands from the world, nor prevent their use.")
                    .add("canCraftAuctionStand", this.canCraftAuctionStands);

            builder.pop();

            builder.comment("Villager Related Settings","Note: Any changes to villagers requires a full reboot to be applied due to how Minecraft/Forge registers trades.").push("villagers");

            builder.comment("Whether the wandering trader will have additional trades that allow you to buy misc items with money.")
                    .add("addCustomWanderingTrades", this.addCustomWanderingTrades);

            builder.comment("Whether the banker villager profession will have any registered trades. The banker sells Lightman's Currency items for coins.")
                    .add("addBanker", this.addBankerVillager);

            builder.comment("Whether the cashier villager profession will have any registered trades.. The cashier sells an amalgamation of vanilla traders products for coins.")
                    .add("addCashier", this.addCashierVillager);

            builder.comment("Villager Trade Modification","Note: Changes made only apply to newly generated trades. Villagers with trades already defined will not be changed.").push("modification");

            builder.comment("Whether vanilla villagers should have the Emeralds from their trades replaced with coins.")
                    .add("changeVanillaTrades", this.changeVanillaTrades);

            builder.comment("Whether villagers added by other mods should have the Emeralds from their trades replaced with coins.")
                    .add("changeModdedTrades", this.changeModdedTrades);

            builder.comment("Whether the wandering trader should have the emeralds from their trades replaced with the default replacement coin.")
                    .add("changeWanderingTrades", this.changeWanderingTrades);

            builder.comment("The default coin to replace a trades emeralds with.")
                    .add("defaultReplacementCoin", this.defaultVillagerReplacementCoin);

            builder.comment("List of replacement coin overrides.",
                            "Each entry must be formatted as follows: \"mod:some_trader_type-lightmanscurrency:some_coin\"",
                            "Every trader not on this list will use the default trader coin defined above.")
                    .add("replacementCoinOverrides", this.villagerReplacementCoinOverrides);

            builder.pop().pop();

            builder.comment("Loot Options").push("loot");

            builder.comment("T1 loot item.","Leave blank (\"\") to not spawn T1 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":1, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT1", this.lootItem1);
            builder.comment("T2 loot item.","Leave blank (\"\") to not spawn T2 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":2, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT2", this.lootItem2);
            builder.comment("T3 loot item.","Leave blank (\"\") to not spawn T3 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":3, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT3", this.lootItem3);
            builder.comment("T4 loot item.","Leave blank (\"\") to not spawn T4 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":4, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT4", this.lootItem4);
            builder.comment("T5 loot item.","Leave blank (\"\") to not spawn T5 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":5, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT5", this.lootItem5);
            builder.comment("T6 loot item.","Leave blank (\"\") to not spawn T6 loot.","Applies to loot table loot type \"lightmanscurrency:configured_item\" with \"tier\":6, which is used in all \"lightmanscurrency:loot_addons\" loot tables configured below.")
                    .add("lootItemT6", this.lootItem6);

            builder.comment("Entity Loot Settings.").push("entities");

            builder.comment("Whether coins can be dropped by entities.")
                    .add("enabled", this.enableEntityDrops);

            builder.comment("Whether coins can be dropped by entities that were spawned by the vanilla spawner.")
                    .add("allowSpawnedDrops", this.allowSpawnerEntityDrops);

            builder.comment("Whether modded machines that emulate player behaviour can trigger coin drops from entities.",
                            "Set to false to help prevent autmated coin farming.")
                    .add("allowFakePlayerDrops", this.allowFakePlayerCoinDrops);

            builder.comment("Entity Drop Lists. Accepts the following inputs:",
                            "Entity IDs. e.g. \"minecraft:cow\"",
                            "Entity Tags. e.g. \"#minecraft:skeletons\"",
                            "Every entity provided by a mod. e.g. \"minecraft:*\"",
                            "Note: If an entity meets multiple criteria, it will drop the lowest tier loot that matches (starting with normal T1 -> T6 then boss T1 -> T6)")
                    .push("lists");

            builder.comment("List of Entities that will drop T1 loot.","Requires a player kill to trigger coin drops.")
                    .add("T1", this.entityDropsT1);
            builder.comment("List of Entities that will drop T1 -> T2 loot.","Requires a player kill to trigger coin drops.")
                    .add("T2", this.entityDropsT2);
            builder.comment("List of Entities that will drop T1 -> T3 loot.","Requires a player kill to trigger coin drops.")
                    .add("T3", this.entityDropsT3);
            builder.comment("List of Entities that will drop T1 -> T4 loot.","Requires a player kill to trigger coin drops.")
                    .add("T4", this.entityDropsT4);
            builder.comment("List of Entities that will drop T1 -> T5 loot.","Requires a player kill to trigger coin drops.")
                    .add("T5", this.entityDropsT5);
            builder.comment("List of Entities that will drop T1 -> T6 loot.","Requires a player kill to trigger coin drops.")
                    .add("T6", this.entityDropsT6);

            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier1\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT1", this.bossEntityDropsT1);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier2\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT2", this.bossEntityDropsT2);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier3\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT3", this.bossEntityDropsT3);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier4\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT4", this.bossEntityDropsT4);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier5\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT5", this.bossEntityDropsT5);
            builder.comment("List of Entities that will drop loot from the \"lightmanscurrency:loot_addons/boss/tier6\" loot table.","Does NOT require a player kill to trigger coin drops.")
                    .add("BossT6", this.bossEntityDropsT6);

            //Pop lists -> entities
            builder.pop().pop();

            builder.comment("Chest Loot Settings").push("chests");

            builder.comment("Whether coins can spawn in chests.")
                    .add("enabled", this.enableChestLoot);

            builder.comment("Chest Spawn Lists").push("lists");

            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier1\" loot table.")
                    .add("T1", this.chestDropsT1);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier2\" loot table.")
                    .add("T2", this.chestDropsT2);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier3\" loot table.")
                    .add("T3", this.chestDropsT3);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier4\" loot table.")
                    .add("T4", this.chestDropsT4);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier5\" loot table.")
                    .add("T5", this.chestDropsT5);
            builder.comment("List of Loot Tables that will also spawn loot from the \"lightmanscurrency:loot_addons/chest/tier6\" loot table.")
                    .add("T6", this.chestDropsT6);

            //Pop lists -> chests -> loot
            builder.pop().pop().pop();

        }

        private final Map<String,Item> villagerCoinOverrideResults = new HashMap<>();

        @Override
        protected void afterReload() {
            this.villagerCoinOverrideResults.clear();
            List<String> overrides = this.villagerReplacementCoinOverrides.get();
            for(int i = 0; i < overrides.size(); ++i)
            {
                try {
                    String override = overrides.get(i);
                    if(!override.contains("-"))
                        throw new RuntimeException("Input doesn't have a '-' splitter.");
                    String[] split = override.split("-");
                    if(split.length != 2)
                        throw new RuntimeException("Input has more than 1 '-' splitter.");

                    Identifier villagerType;
                    try { villagerType = new Identifier(split[0]);
                    } catch(InvalidIdentifierException t) { throw new RuntimeException("Villager type is not a valid resource location.", t); }
                    Identifier itemType;
                    try { itemType = new Identifier(split[1]);
                    } catch(InvalidIdentifierException t) { throw new RuntimeException("Item is not a valid resource location.", t); }

                    Item item = Registry.ITEM.get(itemType);
                    if(item == Items.AIR)
                        throw new RuntimeException("Item '" + itemType + "' is air or is not a registered item.");

                    if(this.villagerCoinOverrideResults.containsKey(villagerType.toString()))
                        throw new RuntimeException("Villager Type '" + villagerType + "' already has an override. Cannot override it twice!");

                    this.villagerCoinOverrideResults.put(villagerType.toString(), item);
                    LightmansCurrency.LogInfo("Villager Replacement Coin Override loaded: " + villagerType + " -> " + itemType);

                } catch(RuntimeException t) { LightmansCurrency.LogError("Error parsing villager emerald override input #" + (i + 1) + ".", t); }
            }
        }

        @NotNull
        public Item getEmeraldReplacementItem(@NotNull String trader)
        {
            if(this.villagerCoinOverrideResults.containsKey(trader))
                return this.villagerCoinOverrideResults.get(trader);
            return this.defaultVillagerReplacementCoin.get();
        }

    }

    public static class Server extends SyncedConfigFile
    {

        private Server() { super("lightmanscurrency-server", new Identifier(LightmansCurrency.MODID,"server")); }

        //Notification Limit
        public final IntOption notificationLimit = IntOption.create(500, 0);

        public final BooleanOption safelyEjectMachineContents = BooleanOption.createTrue();

        //Coin Minting/Melting
        public final BooleanOption coinMintCanMint = BooleanOption.createTrue();
        public final BooleanOption coinMintCanMelt = BooleanOption.createFalse();
        public final IntOption coinMintDefaultDuration = IntOption.create(100,1,72000);

        //Mint Specific Options
        public final BooleanOption coinMintMintableCopper = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableIron = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableGold = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableEmerald = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableDiamond = BooleanOption.createTrue();
        public final BooleanOption coinMintMintableNetherite = BooleanOption.createTrue();

        //Melt Specific Options
        public final BooleanOption coinMintMeltableCopper = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableIron = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableGold = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableEmerald = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableDiamond = BooleanOption.createTrue();
        public final BooleanOption coinMintMeltableNetherite = BooleanOption.createTrue();

        //Wallet Settings
        public final IntOption walletExchangeLevel = IntOption.create(1,0,6);
        public final IntOption walletPickupLevel = IntOption.create(2,0,6);
        public final IntOption walletBankLevel = IntOption.create(5,0,6);

        //Value Display Options
        public final EnumOption<CoinItem.CoinItemTooltipType> coinTooltipType = EnumOption.create(CoinItem.CoinItemTooltipType.DEFAULT);
        public final EnumOption<CoinValue.ValueType> coinValueType = EnumOption.create(CoinValue.ValueType.DEFAULT);
        public final EnumOption<CoinValue.ValueType> coinValueInputType = EnumOption.create(CoinValue.ValueType.DEFAULT);
        public final ItemOption valueBaseCoin = ItemOption.create(ModItems.COIN_COPPER);
        public final StringOption valueFormat = StringOption.create("${value}");

        //Item Capacity Upgrade Settings
        public final IntOption itemCapacityUpgrade1 = IntOption.create(3*64, 1, 1728);
        public final IntOption itemCapacityUpgrade2 = IntOption.create(6*64, 2, 1728);
        public final IntOption itemCapacityUpgrade3 = IntOption.create(9*64, 3, 1728);

        //Enchantment Settings
        public final MoneyValueOption moneyMendingRepairCost = MoneyValueOption.createNonEmpty(() -> new CoinValue(1));
        public final IntOption coinMagnetBaseRange = IntOption.create(5,1,50);
        public final IntOption coinMagnetLeveledRange = IntOption.create(2,1,50);

        //Auction House Settings
        public final BooleanOption auctionHouseEnabled = BooleanOption.createTrue();
        public final BooleanOption auctionHouseOnTerminal = BooleanOption.createTrue();
        public final IntOption auctionHouseDurationMin = IntOption.create(0,0);
        public final IntOption auctionHouseDurationMax = IntOption.create(30,1);

        //Player Trading Options
        //public final DoubleOption playerTradingRange = DoubleOption.create(-1d,-1d);


        @Override
        protected void setup(@NotNull ConfigBuilder builder)
        {

            builder.comment("Notification Settings").push("notifications")
                    .comment("The maximum number of notifications each player and/or machine can have before old entries are deleted.",
                            "Lower if you encounter packet size problems.")
                    .add("limit", this.notificationLimit)
                    .pop();

            builder.comment("Machine Protection Settings").push("machine_protection")
                    .comment("Whether illegally broken traders (such as being replaced with /setblock, or modded machines that break blocks) will safely eject their block/contents into a temporary storage area for the owner to collect safely.",
                            "If disabled, illegally broken traders will throw their items on the ground, and can thus be griefed by modded machines.")
                    .add("safeEjection", this.safelyEjectMachineContents)
                    .pop();

            builder.comment("Coin Mint Settings").push("coin_mint");

            builder.comment("Whether or not Coin Mint recipes of mintType \"MINT\" will function.",
                            "Defaults to the built-in recipes that turn resources into coins.")
                    .add("canMint", this.coinMintCanMint);

            builder.comment("Whether or not Coin Mint recipes of mintType \"MELT\" will function.",
                            "Defaults to the built-in recipes that turn coins back into resources.")
                    .add("canMelt", this.coinMintCanMelt);

            builder.comment("Default number of ticks it takes to process a Coin Mint recipe.",
                            "Does not apply to Coin Mint recipes with a defined \"duration\" input.")
                    .add("defaultMintDuration", this.coinMintDefaultDuration);

            builder.comment("Default Recipes").push("recipes").comment("Minting").push("mint");

            builder.comment("Whether recipes of mintType \"MINT\" with an output of copper coins will function.")
                    .add("copper", this.coinMintMintableCopper);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of iron coins will function.")
                    .add("iron", this.coinMintMintableIron);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of gold coins will function.")
                    .add("gold", this.coinMintMintableGold);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of emerald coins will function.")
                    .add("emerald", this.coinMintMintableEmerald);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of diamond coins will function.")
                    .add("diamond", this.coinMintMintableDiamond);
            builder.comment("Whether recipes of mintType \"MINT\" with an output of netherite coins will function.")
                    .add("netherite", this.coinMintMintableNetherite);

            builder.pop().comment("Melting").push("melt");

            builder.comment("Whether recipes of mintType \"MELT\" with an output of copper ingots will function.")
                    .add("copper", this.coinMintMeltableCopper);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of iron ingots will function.")
                    .add("iron", this.coinMintMeltableIron);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of gold ingots will function.")
                    .add("gold", this.coinMintMeltableGold);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of emeralds will function.")
                    .add("emerald", this.coinMintMeltableEmerald);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of diamonds will function.")
                    .add("diamond", this.coinMintMeltableDiamond);
            builder.comment("Whether recipes of mintType \"MELT\" with an output of netherite ingots will function.")
                    .add("netherite", this.coinMintMeltableNetherite);

            //Pop melt -> recipes -> coin_mint
            builder.pop().pop().pop();

            builder.comment("Wallet Settings").push("wallet");

            builder.comment("The lowest level wallet capable of exchanging coins.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("exchangeLevel", this.walletExchangeLevel);

            builder.comment("The lowest level wallet capable of automatically collecting coins while equipped.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("pickupLevel", this.walletPickupLevel);

            builder.comment("The lowest level wallet capable of allowing transfers to/from your bank account.",
                            "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet; 6-No Wallet")
                    .add("bankLevel", this.walletBankLevel);

            builder.pop();

            builder.comment("Coin value display settings.").push("coin_value_display");

            builder
                    .comment("Tooltip type displayed on coin items.",
                            "DEFAULT: Conversion tooltips, explaining it's value based on the coins it can be converted to/from.",
                            "VALUE: Coins numerical display value as defined by the coinValueType option below. Not recommend if using the DEFAULT coinValueType.")
                    .add("coinTooltipType", this.coinTooltipType);

            builder.comment("Value display method used throughout the mod.",
                            "DEFAULT: Coin Count & Icon aglomerate (1n5g for 1 netherite and 5 gold)",
                            "VALUE: Coin numerical display value as defined by the baseValueCoin and valueFormat config options below.")
                    .add("coinValueType", this.coinValueType);

            builder.comment("Input method used for the Coin Value Input.",
                            "DEFAULT: Default coin input with up/down buttons for each coin type.",
                            "VALUE: Text box input for the coins display value.")
                    .add("coinValueInputType", this.coinValueInputType);

            builder.comment("Coin item defined as 1 value unit for display purposes. Any coins worth less than the base coin will have a decimal value.")
                    .add("baseValueCoin", this.valueBaseCoin);

            builder.comment("Value display format. Used to add currency signs to coin value displays.",
                            "{value} will be replaced with the coins numerical value. Only 1 should be present at any given time.")
                    .add("valueFormat", this.valueFormat);

            builder.pop();

            builder.comment("Upgrade Settings").push("upgrades").comment("Item Capacity Upgrade").push("item_capacity");

            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Iron)")
                    .add("itemCapacity1", this.itemCapacityUpgrade1);
            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Gold)")
                    .add("itemCapacity2", this.itemCapacityUpgrade2);
            builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Diamond)")
                    .add("itemCapacity3", this.itemCapacityUpgrade3);

            builder.pop().pop();

            builder.comment("Enchantment Settings").push("enchantments");

            builder.comment("The cost required to repair a single item durability point with the Money Mending enchantment.")
                    .add("moneyMendingRepairCost", this.moneyMendingRepairCost);

            builder.comment("The coin collection radius of the Coin Magnet I enchantment.")
                    .add("coinMagnetBaseRange", this.coinMagnetBaseRange);
            builder.comment("The increase in the coin collection radius added by each additional level of the Coin Magnet enchantment.")
                    .add("coinMagnetLeveledRange", this.coinMagnetLeveledRange);

            builder.pop();

            builder.comment("Auction House Settings").push("auction_house");

            builder.comment("Whether the Auction House will be automatically generated and accessible.",
                            "If disabled after players have interacted with it, items & money in the auction house cannot be accessed until re-enabled.",
                            "If disabled, it is highly recommended that you also disable the 'crafting.allowAuctionStandCrafting' option in the common config.")
                    .add("enabled", this.auctionHouseEnabled);

            builder.comment("Whether the Auction House will appear in the trading terminal.",
                            "If false, you will only be able to access the Auction House from an Auction Stand.")
                    .add("visibleOnTerminal", this.auctionHouseOnTerminal);

            builder.comment("The minimum number of days an auction can have its duration set to.",
                            "If given a 0 day minimum, the minimum auction duration will be 1 hour.")
                    .add("minimumDuration", this.auctionHouseDurationMin);

            builder.comment("The maxumim number of day an auction can have its duration set to.")
                    .add("maximumDuration", this.auctionHouseDurationMax);

            builder.pop();

            /*builder.comment("Player <-> Player Trading Options").push("player_trading");

            builder.comment("The maximum distance allowed between players in order for a player trade to persist.",
                            "-1 will always allow trading regardless of dimension.",
                            "0 will allow infinite distance but require that both players be in the same dimension.")
                    .add("maxPlayerDistance", this.playerTradingRange);

            builder.pop();*/

        }

        public String formatValueDisplay(double value)
        {
            return this.valueFormat.get().replace("{value}", formatValueOnly(value));
        }

        public String formatValueOnly(double value)
        {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(this.getMaxDecimal());
            return df.format(value);
        }

        public int getMaxDecimal()
        {
            double minFraction = MoneyUtil.getData(new CoinValue(1).coinValues.get(0).coin).getDisplayValue() % 1d;
            if(minFraction > 0d)
            {
                //-2 to ignore the 0.
                return Double.toString(minFraction).length() - 2;
            }
            else
                return 0;
        }

        public boolean allowCoinMintRecipe(@NotNull CoinMintRecipe recipe)
        {
            switch (recipe.getMintType())
            {
                case OTHER: return true;
                case MINT: {
                    if(!this.coinMintCanMint.get())
                        return false;
                    Item resultItem = recipe.getOutputItem().getItem();
                    if(resultItem == ModItems.COIN_COPPER)
                        return this.coinMintMintableCopper.get();
                    if(resultItem == ModItems.COIN_IRON)
                        return this.coinMintMintableIron.get();
                    if(resultItem == ModItems.COIN_GOLD)
                        return this.coinMintMintableGold.get();
                    if(resultItem == ModItems.COIN_EMERALD)
                        return this.coinMintMintableEmerald.get();
                    if(resultItem == ModItems.COIN_DIAMOND)
                        return this.coinMintMintableDiamond.get();
                    if(resultItem == ModItems.COIN_NETHERITE)
                        return this.coinMintMintableNetherite.get();
                }
                case MELT: {
                    if(!this.coinMintCanMelt.get())
                        return false;
                    Item resultItem = recipe.getOutputItem().getItem();
                    if(resultItem == Items.COPPER_INGOT)
                        return this.coinMintMeltableCopper.get();
                    if(resultItem == Items.IRON_INGOT)
                        return this.coinMintMeltableIron.get();
                    if(resultItem == Items.GOLD_INGOT)
                        return this.coinMintMeltableGold.get();
                    if(resultItem == Items.EMERALD)
                        return this.coinMintMeltableEmerald.get();
                    if(resultItem == Items.DIAMOND)
                        return this.coinMintMeltableDiamond.get();
                    if(resultItem == Items.NETHERITE_INGOT)
                        return this.coinMintMeltableNetherite.get();
                }
            }
            return true;
        }

    }

}