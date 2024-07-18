package io.github.lightman314.lightmanscurrency.common.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.Reference.*;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.structure.Structure;

public class VillagerTradeManager {

    public static List<TradeOffers.Factory> getGenericWandererTrades() {
        return ImmutableList.of(
                //Machines
                new LazyTrade(ModItems.COIN_GOLD, 1, ModBlocks.MACHINE_ATM),
                new LazyTrade(ModItems.COIN_IRON, 5, ModBlocks.CASH_REGISTER),
                new LazyTrade(ModItems.COIN_IRON, 5, ModBlocks.TERMINAL)
        );
    }
    public static List<TradeOffers.Factory> getRareWandererTrades() {
        return ImmutableList.of(
                //Traders
                new LazyTrade(ModItems.COIN_GOLD, 2, ModItems.COIN_IRON, 4, ModBlocks.DISPLAY_CASE),
                new LazyTrade(ModItems.COIN_GOLD, 4, ModBlocks.ARMOR_DISPLAY)
        );
    }

    //Bankers sell miscellaneous trade-related stuff
    //Can also trade raw materials for coins to allow bypassing of the coin-mint
    public static Map<Integer,List<TradeOffers.Factory>> getBankerTrades() {
        return ImmutableMap.of(
                1,
                ImmutableList.of(
                        //Sell Coin Mint
                        new LazyTrade(2, ModItems.COIN_IRON, 5, ModBlocks.MACHINE_MINT),
                        //Sell ATM
                        new LazyTrade(2, ModItems.COIN_GOLD, 1, ModBlocks.MACHINE_ATM),
                        //Sell Cash Register
                        new LazyTrade(1, ModItems.COIN_IRON, 5, ModBlocks.CASH_REGISTER),
                        //Sell Trading Core
                        new LazyTrade(1, ModItems.COIN_IRON, 4, ModItems.COIN_COPPER, 8, ModItems.TRADING_CORE)
                ),
                2,
                ImmutableList.of(
                        //Sell first 4 shelves
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemConvertible[] { ModBlocks.SHELF.get(WoodType.OAK), ModBlocks.SHELF.get(WoodType.BIRCH), ModBlocks.SHELF.get(WoodType.SPRUCE), ModBlocks.SHELF.get(WoodType.JUNGLE) }, 12, 5, 0.05f),
                        //Sell 4 "rare" shelves
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemConvertible[] { ModBlocks.SHELF.get(WoodType.ACACIA), ModBlocks.SHELF.get(WoodType.DARK_OAK), ModBlocks.SHELF.get(WoodType.CRIMSON), ModBlocks.SHELF.get(WoodType.WARPED) }, 12, 5, 0.05f),
                        //Sell display case
                        new LazyTrade(5, ModItems.COIN_IRON, 10, ModBlocks.DISPLAY_CASE)
                ),
                3,
                ImmutableList.of(
                        //Sell first 4 card displays
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 15), new ItemConvertible[] { ModBlocks.CARD_DISPLAY.get(WoodType.OAK), ModBlocks.CARD_DISPLAY.get(WoodType.BIRCH), ModBlocks.CARD_DISPLAY.get(WoodType.SPRUCE), ModBlocks.CARD_DISPLAY.get(WoodType.JUNGLE) }, 12, 10, 0.05f),
                        //Sell second 4 card displays
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 15), new ItemConvertible[] { ModBlocks.CARD_DISPLAY.get(WoodType.ACACIA), ModBlocks.CARD_DISPLAY.get(WoodType.DARK_OAK), ModBlocks.CARD_DISPLAY.get(WoodType.CRIMSON), ModBlocks.CARD_DISPLAY.get(WoodType.WARPED) }, 12, 10, 0.05f),
                        //Sell armor display
                        new LazyTrade(10, ModItems.COIN_IRON, 20, ModBlocks.ARMOR_DISPLAY),
                        //Sell small trader server
                        new LazyTrade(10, ModItems.COIN_IRON, 15, ModBlocks.ITEM_NETWORK_TRADER_1),
                        //Sell Terminal
                        new LazyTrade(10, ModItems.COIN_IRON, 10, ModBlocks.TERMINAL)
                ),
                4,
                ImmutableList.of(
                        //Sell Vending Machines
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 25), new ArrayList<>(ModBlocks.VENDING_MACHINE.getAll()), 12, 15, 0.05f),
                        //Sell medium trader server
                        new LazyTrade(15, ModItems.COIN_IRON, 30, ModBlocks.ITEM_NETWORK_TRADER_2),
                        //Sell Freezer
                        new LazyTrade(20, ModItems.COIN_IRON, 30, ModBlocks.FREEZER),
                        //Sell Money Mending book
                        new LazyTrade(20, ModItems.COIN_DIAMOND, 15, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(ModEnchantments.MONEY_MENDING, 1)))
                ),
                5,
                ImmutableList.of(
                        //Sell Large Vending Machines
                        new RandomItemForItemTrade(new ItemStack(ModItems.COIN_IRON, 25), new ArrayList<>(ModBlocks.VENDING_MACHINE_LARGE.getAll()), 12, 30, 0.05f),
                        //Sell large trader server
                        new LazyTrade(30, ModItems.COIN_GOLD, 6, ModBlocks.ITEM_NETWORK_TRADER_3),
                        //Sell extra-large trader server
                        new LazyTrade(30, ModItems.COIN_GOLD, 10, ModBlocks.ITEM_NETWORK_TRADER_4),
                        //Sell Money Mending book
                        new LazyTrade(30, ModItems.COIN_DIAMOND, 10, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(ModEnchantments.MONEY_MENDING, 1)))
                )
        );
    }

    private static final float ENCHANTMENT_PRICE_MODIFIER = 0.25f;

    //Cashiers are a mashup of every vanilla trade where the player buys items from the trader, however the payment is in coins instead of emeralds.
    //Will not buy items and give coins, it will only sell items for coins
    public static Map<Integer,List<TradeOffers.Factory>> getCashierTrades() {
        return ImmutableMap.of(
                1,
                ImmutableList.of(
                        //Farmer
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.BREAD, 6), 16, 1, 0.05f),
                        //Fisherman
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(Items.COD_BUCKET), 16, 1, 0.05f),
                        //Shepherd
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(Items.SHEARS), 12, 1, 0.05f),
                        //Fletcher
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 8), new ItemStack(Items.ARROW, 16), 12, 1, 0.05f),
                        //Librarian
                        new EnchantedBookForCoinsTrade(1),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Blocks.BOOKSHELF), 12, 1, 0.05f),
                        //Cartographer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.MAP), 12, 1, 0.05f),
                        //Cleric
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.REDSTONE), 12, 1, 0.05f),
                        //Armorer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.IRON_LEGGINGS), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.IRON_BOOTS), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.IRON_HELMET), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.IRON_CHESTPLATE), 12, 1, 0.05f),
                        //Weaponsmith
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.IRON_AXE), 12, 1, 0.05f),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 3, Items.IRON_SWORD, 12, 1, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Toolsmith
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_AXE), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_SHOVEL), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_PICKAXE), 12, 1, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON), new ItemStack(Items.STONE_HOE), 12, 1, 0.05f),
                        //Butcher
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.RABBIT_STEW), 12, 1, 0.05f),
                        //Leatherworker (dyed armor only)
                        //Mason
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.BRICK, 10), 16, 1, 0.05f)
                ),
                2,
                ImmutableList.of(
                        //Farmer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.PUMPKIN_PIE, 4), 12, 5, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.APPLE, 4), 16, 5, 0.05f),
                        //Fisherman
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 3), new ItemStack(Items.COD, 15), 16, 10, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.CAMPFIRE), 12, 5, 0.05f),
                        //Shepherd
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.WHITE_WOOL), 16, 5, 0.05f),
                        //Fletcher
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 4), new ItemStack(Items.BOW), 12, 5, 0.05f),
                        //Librarian
                        new EnchantedBookForCoinsTrade(5),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.LANTERN), 12, 5, 0.05f),
                        //Cartographer
                        new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD, 3), StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapIcon.Type.MONUMENT, 12, 5),
                        //Cleric
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Items.LAPIS_LAZULI), 12, 5, 0.05f),
                        //Armorer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 4), new ItemStack(Blocks.BELL), 12, 5, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 9), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.CHAINMAIL_LEGGINGS), 12, 5, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(ModItems.COIN_COPPER, 3), new ItemStack(Items.CHAINMAIL_BOOTS), 12, 5, 0.05f),
                        //Weaponsmith (bell trade duplicate)
                        //Toolsmith (bell trade duplicate)
                        //Butcher
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.PORKCHOP, 6), 16, 5, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Items.COOKED_CHICKEN, 8), 16, 5, 0.05f),
                        //Leatherworker (dyed armor only)
                        //Mason
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.CHISELED_STONE_BRICKS, 4), 16, 5, 0.05f)
                ),
                3,
                ImmutableList.of(
                        //Farmer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.COOKIE, 18), 18, 10, 0.05f),
                        //Fisherman
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 2, Items.FISHING_ROD, 3, 10, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Shepherd (none)
                        //Fletcher
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(Items.CROSSBOW), 12, 10, 0.05f),
                        //Librarian
                        new EnchantedBookForCoinsTrade(10),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 3), new ItemStack(Blocks.GLASS,4), 12, 10, 0.05f),
                        //Cartographer
                        new ItemsForMapTrade(new ItemStack(ModItems.COIN_GOLD, 4), StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapIcon.Type.MANSION, 12, 10),
                        //Cleric
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Blocks.GLOWSTONE), 12, 10, 0.05f),
                        //Armorer
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 7), new ItemStack(ModItems.COIN_COPPER, 4), new ItemStack(Items.CHAINMAIL_HELMET), 12, 10, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 10), new ItemStack(ModItems.COIN_COPPER, 5), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 12, 10, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 6), new ItemStack(Items.SHIELD), 12, 10, 0.05f),
                        //Weaponsmith (none)
                        //Toolsmith
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 5, Items.IRON_AXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 4, Items.IRON_SHOVEL, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 6, Items.IRON_PICKAXE, 3, 12, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new LazyTrade(new ItemStack(ModItems.COIN_DIAMOND, 2), new ItemStack(ModItems.COIN_IRON, 1), new ItemStack(Items.DIAMOND_HOE), 3, 10, 0.05f),
                        //Butcher (none)
                        //Leatherworker (dyed armor only)
                        //Mason
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_ANDESITE, 4), 16, 10, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_DIORITE, 4), 16, 10, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.POLISHED_GRANITE, 4), 16, 10, 0.05f)
                ),
                4,
                ImmutableList.of(
                        //Farmer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Blocks.CAKE), 12, 15, 0.05f),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.NIGHT_VISION, 100, 15),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.JUMP_BOOST, 160, 15),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.WEAKNESS, 100, 15),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.BLINDNESS, 120, 15),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.POISON, 100, 15),
                        new SuspiciousStewForItemTrade(new ItemStack(ModItems.COIN_EMERALD, 1), StatusEffects.SATURATION, 7, 15),
                        //Fisherman (none)
                        //Shepherd (none)
                        //Fletcher
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 5, Items.BOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Librarian
                        new EnchantedBookForCoinsTrade(15),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 5), new ItemStack(Items.CLOCK), 12, 15, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.COMPASS), 12, 15, 0.05f),
                        //Cartographer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Items.ITEM_FRAME), 12, 15, 0.05f),
                        //Cleric
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD), new ItemStack(Items.ENDER_PEARL), 12, 15, 0.05f),
                        //Armorer
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 7, Items.DIAMOND_LEGGINGS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 5, Items.DIAMOND_BOOTS, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Weaponsmith
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Toolsmith
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 3, Items.DIAMOND_AXE, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 2, Items.DIAMOND_SHOVEL, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Butcher (none)
                        //Leatherworker (dyed horse armor only)
                        //Mason
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.TERRACOTTA, 1), 16, 15, 0.05f)
                ),
                5,
                ImmutableList.of(
                        //Farmer
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 15), new ItemStack(Items.GOLDEN_CARROT), 12, 30, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 2), new ItemStack(Items.GLISTERING_MELON_SLICE), 12, 30, 0.05f),
                        //Fisherman (none)
                        //Shepherd
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.PAINTING), 12, 30, 0.05f),
                        //Fletcher
                        new EnchantedItemForCoinsTrade(ModItems.COIN_IRON, 10, Items.CROSSBOW, 3, 15, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Librarian
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.NAME_TAG), 12, 30, 0.05f),
                        //Cartographer
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.GLOBE_BANNER_PATTERN), 12, 30, 0.05f),
                        //Cleric
                        new LazyTrade(new ItemStack(ModItems.COIN_EMERALD, 1), new ItemStack(Blocks.NETHER_WART, 12), 12, 30, 0.05f),
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 5), new ItemStack(Items.EXPERIENCE_BOTTLE), 12, 30, 0.05f),
                        //Armorer
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 8, Items.DIAMOND_CHESTPLATE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 6, Items.DIAMOND_HELMET, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Weaponsmith
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_SWORD, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Toolsmith
                        new EnchantedItemForCoinsTrade(ModItems.COIN_DIAMOND, 4, Items.DIAMOND_PICKAXE, 3, 30, 0.05f, ENCHANTMENT_PRICE_MODIFIER),
                        //Butcher (none)
                        //Leatherworker (dyed armor)
                        new LazyTrade(new ItemStack(ModItems.COIN_GOLD, 1), new ItemStack(Items.SADDLE), 12, 30, 0.05f),
                        //Mason
                        new LazyTrade(new ItemStack(ModItems.COIN_IRON, 2), new ItemStack(Blocks.QUARTZ_BLOCK), 12, 30, 0.05f)
                )
        );
    }

    public static void registerVillagerTrades() {

        //Wandering Trader
        TradeOfferHelper.registerWanderingTraderOffers(1, VillagerTradeManager::modifyGenericWandererTrades);
        TradeOfferHelper.registerWanderingTraderOffers(2, VillagerTradeManager::modifyRareWandererTrades);

        //My Traders
        for(int i = 1; i <= 5; ++i)
        {
            final int level = i;
            TradeOfferHelper.registerVillagerOffers(ModProfessions.BANKER, i, l -> addBankerTrades(level,l));
            TradeOfferHelper.registerVillagerOffers(ModProfessions.CASHIER, i, l -> addCashierTrades(level,l));
        }

    }

    private static void modifyGenericWandererTrades(List<TradeOffers.Factory> existingOffers)
    {
        if(LCConfig.COMMON.addCustomWanderingTrades.get())
            existingOffers.addAll(getGenericWandererTrades());
    }

    private static void modifyRareWandererTrades(List<TradeOffers.Factory> existingOffers)
    {
        if(LCConfig.COMMON.addCustomWanderingTrades.get())
            existingOffers.addAll(getRareWandererTrades());
    }

    private static void addBankerTrades(int level, List<TradeOffers.Factory> existingOffers)
    {
        existingOffers.addAll(getBankerTrades().get(level));
    }

    private static void addCashierTrades(int level, List<TradeOffers.Factory> existingOffers)
    {
        existingOffers.addAll(getCashierTrades().get(level));
    }

    public static class LazyTrade implements TradeOffers.Factory
    {

        private static final int MAX_COUNT = 12;
        private static final float PRICE_MULT = 0.05f;

        private final int xp;
        private final int maxCount;
        private final float priceMult;

        private final ItemStack priceItem1;
        private final ItemStack priceItem2;
        private final ItemStack sellItem;

        public LazyTrade(ItemConvertible priceItem, int priceCount, ItemConvertible forsaleItem)
        {
            this(1, priceItem, priceCount, forsaleItem);
        }

        public LazyTrade(ItemConvertible priceItem, int priceCount, ItemConvertible forsaleItem, int forsaleCount)
        {
            this(1, priceItem, priceCount, forsaleItem, forsaleCount);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem, int priceCount, ItemConvertible forsaleItem)
        {
            this(xpValue, priceItem, priceCount, forsaleItem, 1);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem, int priceCount, ItemConvertible forsaleItem, int forsaleCount)
        {
            this(new ItemStack(priceItem, priceCount), ItemStack.EMPTY, new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
        }

        public LazyTrade(ItemConvertible priceItem1, int priceCount1, ItemConvertible priceItem2, int priceCount2, ItemConvertible forsaleItem)
        {
            this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
        }

        public LazyTrade(ItemConvertible priceItem1, int priceCount1, ItemConvertible priceItem2, int priceCount2, ItemConvertible forsaleItem, int forsaleCount)
        {
            this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem1, int priceCount1, ItemConvertible priceItem2, int priceCount2, ItemConvertible forsaleItem)
        {
            this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem1, int priceCount1, ItemConvertible priceItem2, int priceCount2, ItemConvertible forsaleItem, int forsaleCount)
        {
            this(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_COUNT, xpValue, PRICE_MULT);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem1, int priceCount1, ItemStack forSaleItem)
        {
            this(new ItemStack(priceItem1, priceCount1), ItemStack.EMPTY, forSaleItem, MAX_COUNT, xpValue, PRICE_MULT);
        }

        public LazyTrade(int xpValue, ItemConvertible priceItem1, int priceCount1, ItemConvertible priceItem2, int priceCount2, ItemStack forSaleItem)
        {
            this(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), forSaleItem, MAX_COUNT, xpValue, PRICE_MULT);
        }

        public LazyTrade(ItemStack priceItem, ItemStack sellItem, int maxCount, int xp, float priceMult) {
            this(priceItem, ItemStack.EMPTY, sellItem, maxCount, xp, priceMult);
        }

        public LazyTrade(ItemStack priceItem1, ItemStack priceItem2, ItemStack sellItem, int maxCount, int xp, float priceMult) {
            this.priceItem1 = priceItem1;
            this.priceItem2 = priceItem2;
            this.sellItem = sellItem;
            this.xp = xp;
            this.maxCount = maxCount;
            this.priceMult = priceMult;
        }

        @Override
        public TradeOffer create(Entity entity, Random random)
        {
            return new TradeOffer(this.priceItem1, this.priceItem2, this.sellItem, this.maxCount, this.xp, this.priceMult);
        }

    }

    private static class SuspiciousStewForItemTrade implements TradeOffers.Factory
    {

        private final ItemStack price1;
        private final ItemStack price2;
        private final StatusEffect effect;
        private final int duration;
        private final int xpValue;

        private SuspiciousStewForItemTrade(ItemStack price, StatusEffect effect, int duration, int xpValue)
        {
            this(price, ItemStack.EMPTY, effect, duration, xpValue);
        }

        private SuspiciousStewForItemTrade(ItemStack price1, ItemStack price2, StatusEffect effect, int duration, int xpValue)
        {
            this.price1 = price1;
            this.price2 = price2;
            this.effect = effect;
            this.duration = duration;
            this.xpValue = xpValue;
        }

        @Override
        public TradeOffer create(Entity trader, Random rand) {
            ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.addEffectToStew(itemstack, this.effect, this.duration);
            return new TradeOffer(this.price1, this.price2, itemstack, 12, this.xpValue, 0.05f);
        }



    }

    private static class EnchantedItemForCoinsTrade implements TradeOffers.Factory
    {

        private final Item baseCoin;
        private final int baseCoinCount;
        private final Item sellItem;
        private final int maxUses;
        private final int xpValue;
        private final float priceMultiplier;
        private final double basePriceModifier;

        private EnchantedItemForCoinsTrade(ItemConvertible baseCoin, int baseCoinCount, ItemConvertible sellItem, int maxUses, int xpValue, float priceMultiplier, double basePriceModifier)
        {
            this.baseCoin = baseCoin.asItem();
            this.baseCoinCount = baseCoinCount;
            this.sellItem = sellItem.asItem();
            this.maxUses = maxUses;
            this.xpValue = xpValue;
            this.priceMultiplier = priceMultiplier;
            this.basePriceModifier = basePriceModifier;
        }

        @Override
        public TradeOffer create(Entity trader, Random rand) {
            int i = 5 + rand.nextInt(15);
            ItemStack itemstack = EnchantmentHelper.enchant(rand, new ItemStack(sellItem), i, false);

            long coinValue = MoneyUtil.getValue(this.baseCoin);
            long baseValue = coinValue * this.baseCoinCount;
            long priceValue = baseValue + (long)(coinValue * i * this.basePriceModifier);

            ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
            List<ItemStack> priceStacks = MoneyUtil.getCoinsOfValue(priceValue);
            if(!priceStacks.isEmpty())
                price1 = priceStacks.get(0);
            if(priceStacks.size() > 1)
                price2 = priceStacks.get(1);

            LightmansCurrency.LogDebug("EnchantedItemForCoinsTrade.getOffer() -> \n" +
                    "i=" + i +
                    "\ncoinValue=" + coinValue +
                    "\nbaseValue=" + baseValue +
                    "\npriceValue=" + priceValue +
                    "\nprice1=" + price1.getCount() + "x" + Registries.ITEM.getId(price1.getItem()) +
                    "\nprice2=" + price2.getCount() + "x" + Registries.ITEM.getId(price2.getItem())
            );

            return new TradeOffer(price1, price2, itemstack, this.maxUses, this.xpValue, this.priceMultiplier);
        }

    }

    private static class EnchantedBookForCoinsTrade implements TradeOffers.Factory
    {

        private static final Item baseCoin = ModItems.COIN_GOLD;
        private static final int baseCoinAmount = 5;

        private final int xpValue;

        public EnchantedBookForCoinsTrade(int xpValue)
        {
            this.xpValue = xpValue;
        }

        @Override
        public TradeOffer create(Entity trader, Random rand) {

            List<Enchantment> list = Registries.ENCHANTMENT.stream().filter(Enchantment::isAvailableForEnchantedBookOffer).toList();
            Enchantment enchantment = list.get(rand.nextInt(list.size()));

            int level = 1;
            if(enchantment.getMaxLevel() > 0)
                level = rand.nextInt(enchantment.getMaxLevel()) + 1;
            else
                LightmansCurrency.LogError("Enchantment of type '" + Registries.ENCHANTMENT.getId(enchantment) + "' has a max enchantment level of " + enchantment.getMaxLevel() + ". Unable to properly randomize the enchantment level for a villager trade. Will default to a level 1 enchantment.");
            ItemStack itemstack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, level));

            long coinValue = MoneyUtil.getValue(baseCoin);
            long baseValue = coinValue * baseCoinAmount;

            int valueRandom = rand.nextInt(5 + level * 10);
            long value = baseValue + coinValue * (level + valueRandom);
            if (enchantment.isTreasure())
                value *= 2;

            List<ItemStack> coins = MoneyUtil.getCoinsOfValue(value);
            ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
            if(!coins.isEmpty())
                price1 = coins.get(0);
            if(coins.size() > 1)
                price2 = coins.get(1);

            LightmansCurrency.LogDebug("EnchantedBookForCoinsTrade.getOffer() -> \n" +
                    "baseValue=" + baseValue +
                    "\ncoinValue=" + coinValue +
                    "\nlevel=" + level +
                    "\nvalueRandom=" + valueRandom +
                    "\nvalue=" + value +
                    "\nprice1=" + price1.getCount() + "x" + Registries.ITEM.getId(price1.getItem()) +
                    "\nprice2=" + price2.getCount() + "x" + Registries.ITEM.getId(price2.getItem())
            );

            return new TradeOffer(price1, price2, itemstack, 12, this.xpValue, 0.05f);

        }

    }

    private static class ItemsForMapTrade implements TradeOffers.Factory
    {

        private final ItemStack price1;
        private final ItemStack price2;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final MapIcon.Type mapDecorationType;
        private final int maxUses;
        private final int xpValue;

        public ItemsForMapTrade(ItemStack price, TagKey<Structure> destination, String displayName, MapIcon.Type mapDecorationType, int maxUses, int xpValue)
        {
            this(price, ItemStack.EMPTY, destination, displayName, mapDecorationType, maxUses, xpValue);
        }

        public ItemsForMapTrade(ItemStack price1, ItemStack price2, TagKey<Structure> destination, String displayName, MapIcon.Type mapDecorationType, int maxUses, int xpValue)
        {
            this.price1 = price1;
            this.price2 = price2;
            this.destination = destination;
            this.displayName = displayName;
            this.mapDecorationType = mapDecorationType;
            this.maxUses = maxUses;
            this.xpValue = xpValue;
        }

        @Override
        public TradeOffer create(Entity trader, Random rand) {

            if(!(trader.getWorld() instanceof ServerWorld serverworld))
                return null;
            else
            {
                BlockPos blockPos = serverworld.locateStructure(this.destination, trader.getBlockPos(), 100, true);
                if(blockPos != null)
                {
                    ItemStack itemstack = FilledMapItem.createMap(serverworld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                    FilledMapItem.fillExplorationMap(serverworld, itemstack);
                    MapState.addDecorationsNbt(itemstack, blockPos, "+", this.mapDecorationType);
                    itemstack.setCustomName(Text.translatable(this.displayName));
                    return new TradeOffer(this.price1, this.price2, itemstack, this.maxUses, this.xpValue, 0.05f);
                }
                else
                    return null;
            }
        }

    }

    public static class RandomItemForItemTrade implements TradeOffers.Factory {

        private final ItemStack price1;
        private final ItemStack price2;
        private final List<? extends ItemConvertible> sellItemOptions;
        private final int maxTrades;
        private final int xpValue;
        private final float priceMult;

        public RandomItemForItemTrade(ItemStack price, ItemConvertible[] sellItemOptions, int maxTrades, int xpValue, float priceMult) {
            this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
        }

        public RandomItemForItemTrade(ItemStack price1, ItemStack price2, ItemConvertible[] sellItemOptions, int maxTrades, int xpValue, float priceMult) {
            this(price1, price2, Lists.newArrayList(sellItemOptions), maxTrades, xpValue, priceMult);
        }

        public RandomItemForItemTrade(ItemStack price, List<? extends ItemConvertible> sellItemOptions, int maxTrades, int xpValue, float priceMult) {
            this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
        }

        public RandomItemForItemTrade(ItemStack price1, ItemStack price2, List<? extends ItemConvertible> sellItemOptions, int maxTrades, int xpValue, float priceMult)
        {
            this.price1 = price1;
            this.price2 = price2;
            this.sellItemOptions = sellItemOptions;
            this.maxTrades = maxTrades;
            this.xpValue = xpValue;
            this.priceMult = priceMult;
        }

        @Override
        public TradeOffer create(Entity trader, Random rand) {

            int index = rand.nextInt(this.sellItemOptions.size());
            ItemStack sellItem = new ItemStack(this.sellItemOptions.get(index));

            return new TradeOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xpValue, this.priceMult);
        }

    }

}