package io.github.lightman314.lightmanscurrency.common;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.ObjectBundle;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupEventsImpl;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class ModCreativeGroups {

    public static final ItemGroup COIN_GROUP = new FabricItemGroupBuilderImpl()
            .displayName(Text.translatable("itemGroup.lightmanscurrency.coins"))
            .icon(ezIcon(ModBlocks.COINPILE_GOLD))
            .entries((parameters, p) -> {
                //Coin -> Coin Pile -> Coin Block by type
                ezPop(p, ModItems.COIN_COPPER);
                ezPop(p, ModBlocks.COINPILE_COPPER);
                ezPop(p, ModBlocks.COINBLOCK_COPPER);
                ezPop(p, ModItems.COIN_IRON);
                ezPop(p, ModBlocks.COINPILE_IRON);
                ezPop(p, ModBlocks.COINBLOCK_IRON);
                ezPop(p, ModItems.COIN_GOLD);
                ezPop(p, ModBlocks.COINPILE_GOLD);
                ezPop(p, ModBlocks.COINBLOCK_GOLD);
                ezPop(p, ModItems.COIN_EMERALD);
                ezPop(p, ModBlocks.COINPILE_EMERALD);
                ezPop(p, ModBlocks.COINBLOCK_EMERALD);
                ezPop(p, ModItems.COIN_DIAMOND);
                ezPop(p, ModBlocks.COINPILE_DIAMOND);
                ezPop(p, ModBlocks.COINBLOCK_DIAMOND);
                ezPop(p, ModItems.COIN_NETHERITE);
                ezPop(p, ModBlocks.COINPILE_NETHERITE);
                ezPop(p, ModBlocks.COINBLOCK_NETHERITE);
                //Wallets
                ezPop(p, ModItems.WALLET_COPPER);
                ezPop(p, ModItems.WALLET_IRON);
                ezPop(p, ModItems.WALLET_GOLD);
                ezPop(p, ModItems.WALLET_EMERALD);
                ezPop(p, ModItems.WALLET_DIAMOND);
                ezPop(p, ModItems.WALLET_NETHERITE);
                //Trading Core
                ezPop(p, ModItems.TRADING_CORE);
            }).build();

    public static final ItemGroup MACHINE_GROUP = new FabricItemGroupBuilderImpl()
            .displayName(Text.translatable("itemGroup.lightmanscurrency.machines"))
            .icon(ezIcon(ModBlocks.MACHINE_MINT))
            .entries((parameters, p) -> {
                //Coin Mint
                ezPop(p, ModBlocks.MACHINE_MINT);
                //ATM
                ezPop(p, ModBlocks.MACHINE_ATM);
                ezPop(p, ModItems.PORTABLE_ATM);
                //Cash Register
                ezPop(p, ModBlocks.CASH_REGISTER);
                //Terminal
                ezPop(p, ModBlocks.TERMINAL);
                ezPop(p, ModBlocks.GEM_TERMINAL);
                ezPop(p, ModItems.PORTABLE_TERMINAL);
                ezPop(p, ModItems.PORTABLE_GEM_TERMINAL);
                //Trader Interface
                ezPop(p, ModBlocks.ITEM_TRADER_INTERFACE);
                //Ticket Machine
                ezPop(p, ModBlocks.TICKET_MACHINE);
                //Tickets (with a creative default UUID)
                p.add(TicketItem.CreateMasterTicket(new UUID(0,0)));
                p.add(TicketItem.CreateTicket(new UUID(0,0)));
                //Ticket Stub
                ezPop(p, ModItems.TICKET_STUB);
                //Coin Jars
                ezPop(p, ModBlocks.PIGGY_BANK);
                ezPop(p, ModBlocks.COINJAR_BLUE);
            }).build();

    public static final ItemGroup TRADER_GROUP = new FabricItemGroupBuilderImpl()
            .displayName(Text.translatable("itemGroup.lightmanscurrency.trading"))
            .icon(ezIcon(ModBlocks.DISPLAY_CASE))
            .entries((parameters, p) -> {
                //Item Traders (normal)
                ezPop(p, ModBlocks.SHELF.getAllSorted(EnumUtil::sortEnum));
                ezPop(p, ModBlocks.DISPLAY_CASE);
                ezPop(p, ModBlocks.CARD_DISPLAY.getAllSorted(EnumUtil::sortEnum));
                ezPop(p, ModBlocks.VENDING_MACHINE.getAllSorted(EnumUtil::sortEnum));
                ezPop(p, ModBlocks.FREEZER);
                ezPop(p, ModBlocks.VENDING_MACHINE_LARGE.getAllSorted(EnumUtil::sortEnum));
                //Item Traders (specialty)
                ezPop(p, ModBlocks.ARMOR_DISPLAY);
                ezPop(p, ModBlocks.TICKET_KIOSK);
                ezPop(p, ModBlocks.SLOT_MACHINE);
                //Item Traders (network)
                ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_1);
                ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_2);
                ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_3);
                ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_4);
                //Paygate
                ezPop(p, ModBlocks.PAYGATE);
            }).build();

    public static final ItemGroup UPGRADE_GROUP = new FabricItemGroupBuilderImpl()
            .displayName(Text.translatable("itemGroup.lightmanscurrency.upgrades"))
            .icon(ezIcon(ModItems.ITEM_CAPACITY_UPGRADE_1))
            .entries((parameters, p) -> {
                ezPop(p, ModItems.UPGRADE_SMITHING_TEMPLATE);
                ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_1);
                ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_2);
                ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_3);
                ezPop(p, ModItems.SPEED_UPGRADE_1);
                ezPop(p, ModItems.SPEED_UPGRADE_2);
                ezPop(p, ModItems.SPEED_UPGRADE_3);
                ezPop(p, ModItems.SPEED_UPGRADE_4);
                ezPop(p, ModItems.SPEED_UPGRADE_5);
                ezPop(p, ModItems.NETWORK_UPGRADE);
                ezPop(p, ModItems.HOPPER_UPGRADE);
            }).build();


    private static Supplier<ItemStack> ezIcon(ItemConvertible item) { return Suppliers.memoize(() -> new ItemStack(item)); }

    public static void ezPop(ItemGroup.Entries populator, ItemConvertible item)  { populator.add(item); }
    public static void ezPop(ItemGroup.Entries populator, Collection<? extends ItemConvertible> items)  { items.forEach(populator::add); }
    public static void ezPop(ItemGroup.Entries populator, ObjectBundle<? extends ItemConvertible,?> bundle) { bundle.getAll().forEach(populator::add); }

    public static void registerItemGroups()
    {
        Registry.register(Registries.ITEM_GROUP, new Identifier(LightmansCurrency.MODID, "coins"), COIN_GROUP);
        Registry.register(Registries.ITEM_GROUP, new Identifier(LightmansCurrency.MODID, "machines"), MACHINE_GROUP);
        Registry.register(Registries.ITEM_GROUP, new Identifier(LightmansCurrency.MODID, "traders"), TRADER_GROUP);
        Registry.register(Registries.ITEM_GROUP, new Identifier(LightmansCurrency.MODID, "upgrades"), UPGRADE_GROUP);
        try{
            ItemGroupEventsImpl.getOrCreateModifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ModCreativeGroups::addToFunctionalTab);
            ItemGroupEventsImpl.getOrCreateModifyEntriesEvent(ItemGroups.REDSTONE).register(ModCreativeGroups::addToRedstoneTab);
            ItemGroupEventsImpl.getOrCreateModifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(ModCreativeGroups::addToColoredTab);
        } catch(Throwable ignored) { }
    }

    private static void addToFunctionalTab(FabricItemGroupEntries entries)
    {
        entries.add(ModBlocks.PIGGY_BANK);
        entries.add(ModBlocks.COINJAR_BLUE);
    }

    private static void addToRedstoneTab(FabricItemGroupEntries entries) { entries.add(ModBlocks.PAYGATE); }
    private static void addToColoredTab(FabricItemGroupEntries entries)
    {
        entries.addAll(ModBlocks.VENDING_MACHINE.asItemStack(EnumUtil::sortEnum));
        entries.addAll(ModBlocks.VENDING_MACHINE_LARGE.asItemStack(EnumUtil::sortEnum));
    }

}
