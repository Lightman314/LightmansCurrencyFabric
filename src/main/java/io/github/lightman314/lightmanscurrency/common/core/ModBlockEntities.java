package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    //Item Trader
    public static final BlockEntityType<ItemTraderBlockEntity> ITEM_TRADER = BlockEntityType.Builder.create(ItemTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null);
    //Armor variant of the trader
    public static final BlockEntityType<ArmorDisplayTraderBlockEntity> ARMOR_TRADER = BlockEntityType.Builder.create(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY.block).build(null);
    //Freezer variant of the trader
    public static final BlockEntityType<FreezerTraderBlockEntity> FREEZER_TRADER = BlockEntityType.Builder.create(FreezerTraderBlockEntity::new, ModBlocks.FREEZER.block).build(null);
    //Ticket variant of the trader
    public static final BlockEntityType<TicketTraderBlockEntity> TICKET_TRADER = BlockEntityType.Builder.create(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK.block).build(null);
    //Slot Machine
    public static final BlockEntityType<SlotMachineTraderBlockEntity> SLOT_MACHINE = BlockEntityType.Builder.create(SlotMachineTraderBlockEntity::new, ModBlocks.SLOT_MACHINE.block).build(null);


    //Sided Inventory Extension Block Entity
    public static final BlockEntityType<SidedStorageExtensionBlockEntity> STORAGE_EXTENSION = BlockEntityType.Builder.create(SidedStorageExtensionBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.INVENTORY_EXTENSION_TYPE)).build(null);

    //Trader Interface Terminal
    public static final BlockEntityType<ItemTraderInterfaceBlockEntity> ITEM_TRADER_INTERFACE = BlockEntityType.Builder.create(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE.block).build(null);

    //Cash Register
    public static final BlockEntityType<CashRegisterBlockEntity> CASH_REGISTER = BlockEntityType.Builder.create(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER.block).build(null);
    //Coin Mint
    public static final BlockEntityType<CoinMintBlockEntity> COIN_MINT = BlockEntityType.Builder.create(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT.block).build(null);
    //Ticket Machine
    public static final BlockEntityType<TicketMachineBlockEntity> TICKET_MACHINE = BlockEntityType.Builder.create(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE.block).build(null);

    //Paygate
    public static final BlockEntityType<PaygateBlockEntity> PAYGATE = BlockEntityType.Builder.create(PaygateBlockEntity::new, ModBlocks.PAYGATE.block).build(null);

    //Coin Jar
    public static final BlockEntityType<CoinJarBlockEntity> COIN_JAR = BlockEntityType.Builder.create(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK.block, ModBlocks.COINJAR_BLUE.block).build(null);

    public static void registerBlockEntities() {

        //Item Traders
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "item_trader"), ITEM_TRADER);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "armor_trader"), ARMOR_TRADER);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "freezer_trader"), FREEZER_TRADER);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "ticket_trader"), TICKET_TRADER);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "slot_machine"), SLOT_MACHINE);

        //Inventory Extension
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "storage_extension"), STORAGE_EXTENSION);

        //Trader Interface Terminal
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "item_trader_interface"), ITEM_TRADER_INTERFACE);

        //Cash Register
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "cash_register"), CASH_REGISTER);
        //Coin Mint
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT);
        //Ticket Machine
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "ticket_machine"), TICKET_MACHINE);

        //Paygate
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "paygate"), PAYGATE);

        //Coin Jar
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "coin_jar"), COIN_JAR);

    }

}
