package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.*;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {

    //Item Trader
    public static final BlockEntityType<ItemTraderBlockEntity> ITEM_TRADER = FabricBlockEntityTypeBuilder.create(ItemTraderBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.ITEM_TRADER_TYPE)).build(null);
    //Armor variant of the trader
    public static final BlockEntityType<ArmorDisplayTraderBlockEntity> ARMOR_TRADER = FabricBlockEntityTypeBuilder.create(ArmorDisplayTraderBlockEntity::new, ModBlocks.ARMOR_DISPLAY.block).build(null);
    //Freezer variant of the trader
    public static final BlockEntityType<FreezerTraderBlockEntity> FREEZER_TRADER = FabricBlockEntityTypeBuilder.create(FreezerTraderBlockEntity::new, ModBlocks.FREEZER.block).build(null);
    //Ticket variant of the trader
    public static final BlockEntityType<TicketTraderBlockEntity> TICKET_TRADER = FabricBlockEntityTypeBuilder.create(TicketTraderBlockEntity::new, ModBlocks.TICKET_KIOSK.block).build(null);
    //Slot Machine
    public static final BlockEntityType<SlotMachineTraderBlockEntity> SLOT_MACHINE = FabricBlockEntityTypeBuilder.create(SlotMachineTraderBlockEntity::new, ModBlocks.SLOT_MACHINE.block).build(null);

    //Sided Inventory Extension Block Entity
    public static final BlockEntityType<SidedStorageExtensionBlockEntity> STORAGE_EXTENSION = FabricBlockEntityTypeBuilder.create(SidedStorageExtensionBlockEntity::new, BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.INVENTORY_EXTENSION_TYPE)).build(null);

    //Trader Interface Terminal
    public static final BlockEntityType<ItemTraderInterfaceBlockEntity> ITEM_TRADER_INTERFACE = FabricBlockEntityTypeBuilder.create(ItemTraderInterfaceBlockEntity::new, ModBlocks.ITEM_TRADER_INTERFACE.block).build(null);

    //Cash Register
    public static final BlockEntityType<CashRegisterBlockEntity> CASH_REGISTER = FabricBlockEntityTypeBuilder.create(CashRegisterBlockEntity::new, ModBlocks.CASH_REGISTER.block).build(null);
    //Coin Mint
    public static final BlockEntityType<CoinMintBlockEntity> COIN_MINT = FabricBlockEntityTypeBuilder.create(CoinMintBlockEntity::new, ModBlocks.MACHINE_MINT.block).build(null);
    //Ticket Machine
    public static final BlockEntityType<TicketMachineBlockEntity> TICKET_MACHINE = FabricBlockEntityTypeBuilder.create(TicketMachineBlockEntity::new, ModBlocks.TICKET_MACHINE.block).build(null);

    //Paygate
    public static final BlockEntityType<PaygateBlockEntity> PAYGATE = FabricBlockEntityTypeBuilder.create(PaygateBlockEntity::new, ModBlocks.PAYGATE.block).build(null);

    //Coin Jar
    public static final BlockEntityType<CoinJarBlockEntity> COIN_JAR = FabricBlockEntityTypeBuilder.create(CoinJarBlockEntity::new, ModBlocks.PIGGY_BANK.block, ModBlocks.COINJAR_BLUE.block).build(null);

    public static void registerBlockEntities() {

        //Item Traders
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "item_trader"), ITEM_TRADER);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "armor_trader"), ARMOR_TRADER);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "freezer_trader"), FREEZER_TRADER);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "ticket_trader"), TICKET_TRADER);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "slot_machine"), SLOT_MACHINE);

        //Inventory Extension
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "storage_extension"), STORAGE_EXTENSION);

        //Trader Interface Terminal
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "item_trader_interface"), ITEM_TRADER_INTERFACE);

        //Cash Register
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "cash_register"), CASH_REGISTER);
        //Coin Mint
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT);
        //Ticket Machine
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "ticket_machine"), TICKET_MACHINE);

        //Paygate
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "paygate"), PAYGATE);

        //Coin Jar
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(LightmansCurrency.MODID, "coin_jar"), COIN_JAR);

    }

}
