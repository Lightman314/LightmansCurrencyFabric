package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.CoinItem;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.config.Config;
import io.github.lightman314.lightmanscurrency.config.options.*;
import io.github.lightman314.lightmanscurrency.config.options.custom.IdentifierListOption;
import io.github.lightman314.lightmanscurrency.config.options.custom.VillagerItemOverrideListOption;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.VillagerItemOverride;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;

public class LCConfigCommon extends Config {

    public static final LCConfigCommon INSTANCE = new LCConfigCommon(ConfigBuilder.create());

    //Former Common Config Options
    public final BooleanOption canCraftNetworkTraders;
    public final BooleanOption canCraftTraderInterface;

    //Custom Villager Trades
    public final BooleanOption addCustomWanderingTrades;
    public final BooleanOption addBankerVillager;
    public final BooleanOption addCashierVillager;
    public final BooleanOption changeVanillaTrades;
    public final BooleanOption changeModdedTrades;
    public final BooleanOption changeWanderingTrades;
    public final ItemOption defaultVillagerCoin;
    public final VillagerItemOverrideListOption villagerOverrides;

    public final Item getVillagerOverrideItem(Identifier professionType) {
        for(VillagerItemOverride override : this.villagerOverrides.get())
        {
            if(override.villagerType.equals(professionType))
                return override.getNewItem();
        }
        return this.defaultVillagerCoin.get();
    }

    //Entity Loot Modifications
    public final BooleanOption enableEntityDrops;
    public final BooleanOption enableSpawnerEntityDrops;
    //Entity Drop Lists
    public final IdentifierListOption copperEntityDrops;
    public final IdentifierListOption ironEntityDrops;
    public final IdentifierListOption goldEntityDrops;
    public final IdentifierListOption emeraldEntityDrops;
    public final IdentifierListOption diamondEntityDrops;
    public final IdentifierListOption netheriteEntityDrops;
    //Boss Drop Lists
    public final IdentifierListOption bossCopperEntityDrops;
    public final IdentifierListOption bossIronEntityDrops;
    public final IdentifierListOption bossGoldEntityDrops;
    public final IdentifierListOption bossEmeraldEntityDrops;
    public final IdentifierListOption bossDiamondEntityDrops;
    public final IdentifierListOption bossNetheriteEntityDrops;

    //Chest Loot Modifications
    public final BooleanOption enableChestLoot;
    public final IdentifierListOption copperChestDrops;
    public final IdentifierListOption ironChestDrops;
    public final IdentifierListOption goldChestDrops;
    public final IdentifierListOption emeraldChestDrops;
    public final IdentifierListOption diamondChestDrops;
    public final IdentifierListOption netheriteChestDrops;


    //Former Server Config Options
    public final IntegerOption notificationLimit;

    public final BooleanOption safelyEjectIllegalBreaks;

    //Coin Minting
    public final BooleanOption allowCoinMinting;
    public final BooleanOption mintCopper;
    public final BooleanOption mintIron;
    public final BooleanOption mintGold;
    public final BooleanOption mintEmerald;
    public final BooleanOption mintDiamond;
    public final BooleanOption mintNetherite;

    public final boolean canMint(Item item) {
        if(item == ModItems.COIN_COPPER)
            return this.mintCopper.get();
        else if(item == ModItems.COIN_IRON)
            return this.mintIron.get();
        else if(item == ModItems.COIN_GOLD)
            return this.mintGold.get();
        else if(item == ModItems.COIN_EMERALD)
            return this.mintEmerald.get();
        else if(item == ModItems.COIN_DIAMOND)
            return this.mintDiamond.get();
        else if(item == ModItems.COIN_NETHERITE)
            return this.mintNetherite.get();
        //If no rule is against it, allow the minting
        return true;
    }

    //Coin Melting
    public final BooleanOption allowCoinMelting;
    public final BooleanOption meltCopper;
    public final BooleanOption meltIron;
    public final BooleanOption meltGold;
    public final BooleanOption meltEmerald;
    public final BooleanOption meltDiamond;
    public final BooleanOption meltNetherite;

    public boolean canMelt(Item item)
    {
        if(item == ModItems.COIN_COPPER)
            return this.meltCopper.get();
        else if(item == ModItems.COIN_IRON)
            return this.meltIron.get();
        else if(item == ModItems.COIN_GOLD)
            return this.meltGold.get();
        else if(item == ModItems.COIN_EMERALD)
            return this.meltEmerald.get();
        else if(item == ModItems.COIN_DIAMOND)
            return this.meltDiamond.get();
        else if(item == ModItems.COIN_NETHERITE)
            return this.meltNetherite.get();
        //If no rule is against it, allow the minting
        return true;
    }

    //Wallet
    public final IntegerOption walletExchangeLevel;
    public final IntegerOption walletPickupLevel;
    public final IntegerOption walletBankLevel;

    //Coin Value Display
    public final EnumOption<CoinItem.CoinItemTooltipType> coinTooltipType;
    public final EnumOption<CoinValue.ValueType> coinValueType;
    public final EnumOption<CoinValue.ValueType> coinValueInputType;
    public final ItemOption valueBaseCoin;
    public final StringOption valueFormat;
    public String formatValueDisplay(double value)
    {
        return this.valueFormat.get().replace("{value}", formatValueOnly(value));
    }
    public String formatValueOnly(double value)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(getMaxDecimal());
        return df.format(value);
    }
    private int getMaxDecimal()
    {
        CoinData data = MoneyUtil.getData(new CoinValue(1).coinValues.get(0).coin);
        if(data == null)
            return 0;
        double minFraction = data.getDisplayValue() % 1d;
        if(minFraction > 0d)
        {
            //-2 to ignore the 0.
            return Double.toString(minFraction).length() - 2;
        }
        else
            return 0;
    }

    //Item Capacity Upgrade Settings
    public final IntegerOption itemUpgradeCapacity1;
    public final IntegerOption itemUpgradeCapacity2;
    public final IntegerOption itemUpgradeCapacity3;

    //Enchantment Settings
    public final ItemOption moneyMendingCoinCost;
    public final IntegerOption coinMagnetRangeBase;
    public final IntegerOption coinMagnetRangeLevel;

    //Auction House Settings
    public final BooleanOption enableAuctionHouse;
    public final IntegerOption minAuctionDuration;
    public final IntegerOption maxAuctionDuration;

    private LCConfigCommon(ConfigBuilder builder) {

        super(LightmansCurrency.MODID + "/config-common", builder);

        //Crafting Settings
        builder.comment("Crafting Settings").push("crafting");

        this.canCraftNetworkTraders = builder.comment("Whether Network Traders can be crafted.",
                "Disabling will not remove any existing Network Traders from the world, nor prevent their use.",
                "Disabling does NOT disable the recipes of Network Upgrades or the Trading Terminals.")
                .option("allowNetworkTraderCrafting", BooleanOption.create(true));

        this.canCraftTraderInterface = builder.comment("Whether Trader Interface blocks can be crafted.",
                        "Disabling will not remove any existing Trader Interfaces from the world, nor prevent their use.")
                .option("allowTraderInterfaceCrafting", BooleanOption.create(true));

        builder.pop();

        //Villager Settings
        builder.comment("Villager Related Settings").push("villagers");

        this.addCustomWanderingTrades = builder.comment("Whether the wandering trader will have additional trades that allow you to buy misc items with money.")
                .option("addCustomWanderingTrades", BooleanOption.create(true));

        this.addBankerVillager = builder.comment("Whether the banker villager profession will have any registered trades. The banker sells Lightmans Currency items for coins.")
                .option("addCBanker", BooleanOption.create(true));

        this.addCashierVillager = builder.comment("Whether the cashier villager profession will have any registered trades. The cashier sells an amalgamation of vanilla traders products for coins.")
                .option("addCashier", BooleanOption.create(true));

        builder.comment("Settings Related to other Villagers").push("other_villagers");

        this.changeVanillaTrades = builder.comment("Whether vanilla villagers should have the Emeralds from their trades replaced with coins.")
                .option("changeVanillaTrades", BooleanOption.create(false));

        this.changeModdedTrades = builder.comment("Whether villagers added by other mods should have the Emeralds from their trades replaces with coins.")
                .option("changeModdedTrades", BooleanOption.create(false));

        this.changeWanderingTrades = builder.comment("Whether the wandering trader should have the emeralds from their trades replaced with the default trader coin.")
                .option("changeWanderingTrades", BooleanOption.create(false));

        this.defaultVillagerCoin = builder.comment("The default coin to replace a villagers emeralds trades with.")
                .option("defaultTraderCoin", ItemOption.create(ModItems.COIN_EMERALD));

        this.villagerOverrides = builder.comment("List of trader coin overrides.",
                "Trader: The villager profession id of the villager trades to override.",
                "Coin: The coin item that will be used to replace that villagers emeralds.")
                .option("villagerCoinOverrides", VillagerItemOverrideListOption.create(
                        VillagerItemOverride.of(new Identifier("minecraft:butcher"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:cartographer"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:farmer"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:fisherman"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:fletcher"), new Identifier("lightmanscurrency:coin_copper")),
                        VillagerItemOverride.of(new Identifier("minecraft:leatherworker"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:mason"), new Identifier("lightmanscurrency:coin_iron")),
                        VillagerItemOverride.of(new Identifier("minecraft:shepherd"), new Identifier("lightmanscurrency:coin_iron"))
                ));

        builder.pop().pop();

        builder.comment("Entity Loot Settings").push("entity_loot");

        this.enableEntityDrops = builder.comment("Whether coins can be dropped by entities. Does not effect chest loot generation.")
                .option("enableEntityDrops", BooleanOption.create(true));

        this.enableSpawnerEntityDrops = builder.comment("Whether coins can be dropped by entities that were spawned by the vanilla spawner.")
                .option("enableSpawnerEntityDrops", BooleanOption.create(false));

        builder.comment("Regular Coin Drop Entity Lists. Require player kills.").push("normal_drop_lists");

        this.copperEntityDrops = builder.comment("Entities that will drop copper coins.")
                .option("copper", IdentifierListOption.create(LootManager.ENTITY_COPPER_DROPLIST));
        this.ironEntityDrops = builder.comment("Entities that will drop copper -> iron coins.")
                .option("iron", IdentifierListOption.create(LootManager.ENTITY_IRON_DROPLIST));
        this.goldEntityDrops = builder.comment("Entities that will drop copper -> gold coins.")
                .option("gold", IdentifierListOption.create(LootManager.ENTITY_GOLD_DROPLIST));
        this.emeraldEntityDrops = builder.comment("Entities that will drop copper > emerald coins.")
                .option("emerald", IdentifierListOption.create(LootManager.ENTITY_EMERALD_DROPLIST));
        this.diamondEntityDrops = builder.comment("Entities that will drop copper -> diamond coins.")
                .option("diamond", IdentifierListOption.create(LootManager.ENTITY_DIAMOND_DROPLIST));
        this.netheriteEntityDrops = builder.comment("Entities that will drop copper -> netherite coins.")
                .option("netherite", IdentifierListOption.create(LootManager.ENTITY_NETHERITE_DROPLIST));

        builder.pop();

        builder.comment("Boss Coin Drop Entity Lists. Do not require player kills. Drops large quantities of coins.").push("boss_drop_lists");

        this.bossCopperEntityDrops = builder.comment("Entities that will drop a large amount of copper coins.")
                .option("copper", IdentifierListOption.create(LootManager.ENTITY_BOSS_COPPER_DROPLIST));
        this.bossIronEntityDrops = builder.comment("Entities that will drop a large amount of copper -> iron coins.")
                .option("iron", IdentifierListOption.create(LootManager.ENTITY_BOSS_IRON_DROPLIST));
        this.bossGoldEntityDrops = builder.comment("Entities that will drop a large amount of copper -> gold coins.")
                .option("gold", IdentifierListOption.create(LootManager.ENTITY_BOSS_GOLD_DROPLIST));
        this.bossEmeraldEntityDrops = builder.comment("Entities that will drop a large amount of copper -> emerald coins.")
                .option("emerald", IdentifierListOption.create(LootManager.ENTITY_BOSS_EMERALD_DROPLIST));
        this.bossDiamondEntityDrops = builder.comment("Entities that will drop a large amount of copper -> diamond coins.")
                .option("diamond", IdentifierListOption.create(LootManager.ENTITY_BOSS_DIAMOND_DROPLIST));
        this.bossNetheriteEntityDrops = builder.comment("Entities that will drop a large amount of copper -> netherite coins.")
                .option("netherite", IdentifierListOption.create(LootManager.ENTITY_BOSS_NETHERITE_DROPLIST));

        builder.pop().pop();

        builder.comment("Chest Loot Settings").push("chest_loot");

        this.enableChestLoot = builder.comment("Whether coins can spawn in chests. Does not effect entity loot drops.")
                .option("enableChestLoot", BooleanOption.create(true));

        builder.comment("Coin Spawn Chest Lists").push("spawn_lists");

        this.copperChestDrops = builder.comment("Chests that will occasionally spawn copper coins.")
                .option("copper", IdentifierListOption.create(LootManager.CHEST_COPPER_DROPLIST));
        this.ironChestDrops = builder.comment("Chests that will occasionally spawn copper -> iron coins.")
                .option("iron", IdentifierListOption.create(LootManager.CHEST_IRON_DROPLIST));
        this.goldChestDrops = builder.comment("Chests that will occasionally spawn copper -> gold coins.")
                .option("gold", IdentifierListOption.create(LootManager.CHEST_GOLD_DROPLIST));
        this.emeraldChestDrops = builder.comment("Chests that will occasionally spawn copper -> emerald coins.")
                .option("emerald", IdentifierListOption.create(LootManager.CHEST_EMERALD_DROPLIST));
        this.diamondChestDrops = builder.comment("Chests that will occasionally spawn copper -> diamond coins.")
                .option("diamond", IdentifierListOption.create(LootManager.CHEST_DIAMOND_DROPLIST));
        this.netheriteChestDrops = builder.comment("Chests that will occasionally spawn copper -> netherite coins.")
                .option("netherite", IdentifierListOption.create(LootManager.CHEST_NETHERITE_DROPLIST));

        builder.pop().pop();


        //Notification Settings
        builder.comment("Notification Settings").push("notifications");

        this.notificationLimit = builder.comment("The maximum number of notification each player/trader can have before old entries are deleted.",
                "Lower if you encounter packet size problems.")
                .option("notificationLimit", IntegerOption.create(100, 10, Integer.MAX_VALUE));

        builder.pop();

        builder.comment("Safety Settings").push("safety");

        this.safelyEjectIllegalBreaks = builder.comment("Whether illegally broken traders (such as being replaced with /setblock, or modded machines that break blocks) will safely eject their block/contents into a temporary storage area for the owner to collect safely.",
                "If dsiabled, illegally brokoen traders will throw their items on the ground, and can this be griefed by modded machines.")
                .option("ejectIllegalBreaks", BooleanOption.create(true));

        builder.pop();


        //Coin Minting
        builder.comment("Coin Minting Settings").push("coin_minting");

        this.allowCoinMinting = builder.comment("Determines whether or not coins should be craftable via the Coin Minting Machine.")
                .option("canMintCoins", BooleanOption.create(true));

        builder.comment("Specific Coin Minting Settings").push("detailed");

        this.mintCopper = builder.comment("Whether copper coins can be minted.").option("canMintCopper", BooleanOption.create(true));
        this.mintIron = builder.comment("Whether iron coins can be minted.").option("canMintIron", BooleanOption.create(true));
        this.mintGold = builder.comment("Whether gold coins can be minted.").option("canMintGold", BooleanOption.create(true));
        this.mintEmerald = builder.comment("Whether emerald coins can be minted.").option("canMintEmerald", BooleanOption.create(true));
        this.mintDiamond = builder.comment("Whether diamond coins can be minted.").option("canMintDiamond", BooleanOption.create(true));
        this.mintNetherite = builder.comment("Whether netherite coins can be minted.").option("canMintNetherite", BooleanOption.create(true));

        builder.pop().pop();

        //Coin Melting
        builder.comment("Coin Melting Settings").push("coin_melting");

        this.allowCoinMelting = builder.comment("Determines whether or not coins can be melted back into their source material in the Coin Minting Machine.")
                .option("canMeltCoins", BooleanOption.create(true));

        builder.comment("Specific Coin Melting Settings").push("detailed");

        this.meltCopper = builder.comment("Whether copper coins can be melted.").option("canMeltCopper", BooleanOption.create(true));
        this.meltIron = builder.comment("Whether iron coins can be melted.").option("canMeltIron", BooleanOption.create(true));
        this.meltGold = builder.comment("Whether gold coins can be melted.").option("canMeltGold", BooleanOption.create(true));
        this.meltEmerald = builder.comment("Whether emerald coins can be melted.").option("canMeltEmerald", BooleanOption.create(true));
        this.meltDiamond = builder.comment("Whether diamond coins can be melted.").option("canMeltDiamond", BooleanOption.create(true));
        this.meltNetherite = builder.comment("Whether netherite coins can be melted.").option("canMeltNetherite", BooleanOption.create(true));

        builder.pop().pop();

        //Wallet
        builder.comment("Wallet Settings").push("wallet");

        this.walletExchangeLevel = builder.comment("The lowest level wallet capable of exchanging coins in the UI.",
                "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet",
                "Must be less than or equal to 'pickupLevel'.")
                .option("exchangeLevel", IntegerOption.create(1, 0, 5));

        this.walletPickupLevel = builder.comment("The lowest level wallet capable of automatically collecting coins while equipped.",
                "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet")
                .option("pickupLevel", IntegerOption.create(2, 0, 5));

        this.walletBankLevel = builder.comment("The lowest level wallet capable of allowing transfers to/from your bank account.",
                "0-Copper Wallet; 1-Iron Wallet; 2-Gold Wallet; 3-Emerald Wallet; 4-Diamond Wallet; 5-Netherite Wallet")
                .option("bankLevel", IntegerOption.create(5, 0, 5));

        builder.pop();

        //Coin Value Display
        builder.comment("Coin Value Display Settings").push("coin_value_display");

        this.coinTooltipType = builder.comment("Tooltip type displayed on coin items.",
                "DEFAULT: Conversion tooltips, explaining it's value based on the coins it can be converted to/from.",
                "VALUE: Coins numerical display value as defined by the coinValueType option below. Not recommend if using the DEFAULT coinValueType.")
                .option("coinTooltipType", EnumOption.create(CoinItem.CoinItemTooltipType.DEFAULT, CoinItem.CoinItemTooltipType.values()));

        this.coinValueType = builder.comment("Value display method used throughout the mod.",
                "DEFAULT: Coin Count & Icon aglomerate (1n5g for 1 netherite and 5 gold)",
                "VALUE: Coin numerical display value as defined by the baseValueCoin and valueFormat config options below.")
                .option("coinValueType", EnumOption.create(CoinValue.ValueType.DEFAULT, CoinValue.ValueType.values()));

        this.coinValueInputType = builder.comment("Input method used for the Coin Value Input.",
                        "DEFAULT: Default coin input with up/down buttons for each coin type.",
                        "VALUE: Text box input for the coins display value.")
                .option("coinValueInputType", EnumOption.create(CoinValue.ValueType.DEFAULT, CoinValue.ValueType.values()));

        this.valueBaseCoin = builder.comment("Coin item defined as 1 value unit for display purposes. Any coins worth less than the base coin will have a decimal value.")
                .option("baseValueCoin", ItemOption.create(ModItems.COIN_COPPER));

        this.valueFormat = builder.comment("Value display format. Used to add currency signs to coin value displays in 'VALUE' mode.",
                "{value} will be replaced with the coins numerical value. Only 1 should be present at any given time.")
                .option("valueFormat", StringOption.create("${value}"));

        builder.pop();

        builder.comment("Item Capacity Upgrade Settings").push("upgrades");

        this.itemUpgradeCapacity1 = builder.comment("The amount of item storage added by the first Item Capacity Upgrade (Iron).")
                .option("upgradeCapacity1", IntegerOption.create(3 * 64, 1, 1728));
        this.itemUpgradeCapacity2 = builder.comment("The amount of item storage added by the second Item Capacity Upgrade (Gold).")
                .option("upgradeCapacity2", IntegerOption.create(6 * 64, 1, 1728));
        this.itemUpgradeCapacity3 = builder.comment("The amount of item storage added by the third Item Capacity Upgrade (Diamond).")
                .option("upgradeCapacity3", IntegerOption.create(9 * 64, 1, 1728));

        builder.pop();

        builder.comment("Enchantment Settings").push("enchantments");

        this.moneyMendingCoinCost = builder.comment("The coin cost required to repair a single durability point with the Money Mending enchantment.")
                .option("moneyMendingBaseCost", ItemOption.create(ModItems.COIN_COPPER));

        this.coinMagnetRangeBase = builder.comment("The base radius around the player that the Coin Magnet enchantment will collect coins from.")
                .option("coinMagnetRangeBase", IntegerOption.create(5, 0, 50));

        this.coinMagnetRangeLevel = builder.comment("The increase in collection radius added by each additional level of the Coin Magnet enchantment.")
                .option("coinMagnetRangeLevel", IntegerOption.create(2, 0, 50));

        builder.pop();

        builder.comment("Auction House Settings").push("auction_house");

        this.enableAuctionHouse = builder.comment("Whether the Auction House will appear on the trader list.",
                "If disabled after players have interacted with it, items & money in the auction house cannot be accessed until re-enabled.")
                .option("enabled", BooleanOption.create(true));

        this.minAuctionDuration = builder.comment("The minimum number of days an auction can be carried out.")
                .option("minDuration", IntegerOption.create(0, 0, Integer.MAX_VALUE));

        this.maxAuctionDuration = builder.comment("The maximum number of days an auction can be carried out.")
                .option("maxDuration", IntegerOption.create(30, 1, Integer.MAX_VALUE));

        builder.pop();

        //LC Discord settings if it gets added later


        this.lock();

    }

}
