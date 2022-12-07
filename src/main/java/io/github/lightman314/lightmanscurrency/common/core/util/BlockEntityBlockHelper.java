package io.github.lightman314.lightmanscurrency.common.core.util;

import java.util.*;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.groups.BlockItemPair;
import io.github.lightman314.lightmanscurrency.common.core.groups.ObjectBundle;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

public class BlockEntityBlockHelper {

    public static final Identifier ITEM_TRADER_TYPE = new Identifier(LightmansCurrency.MODID, "item_trader");
    public static final Identifier INVENTORY_EXTENSION_TYPE = new Identifier(LightmansCurrency.MODID, "inventory_extension");

    private static final Map<Identifier,List<Block>> blockList = new HashMap<>();

    public static Block[] getBlocksForBlockEntity(Identifier beType) {
        List<Block> result = new ArrayList<>();
        for(Block block : blockList.getOrDefault(beType, new ArrayList<>()))
            result.add(block);
        return result.toArray(new Block[result.size()]);
    }

    public static void addBlockToBlockEntity(Identifier beType, Block block) { addBlockToBlockEntity(beType, block); }
    public static void addBlockToBlockEntity(Identifier beType, BlockItemPair block) { addBlockToBlockEntity(beType, block); }
    public static void addBlocksToBlockEntity(Identifier beType, ObjectBundle<Block,?> blocks) { addBlocksToBlockEntity(beType, blocks.getAll()); }
    public static void addBlocksToBlockEntity2(Identifier beType, ObjectBundle<BlockItemPair,?> blocks) { addBlockToBlockEntity2(beType, blocks.getAll()); }
    @SafeVarargs
    public static void addBlocksToBlockEntity(Identifier beType, Block... blocks) {
        List<Block> result = new ArrayList<>();
        for(Block block : blocks)
        {
            if(block != null)
                result.add(block);
        }
        addBlocksToBlockEntity(beType, result);
    }

    public static void addBlocksToBlockEntity(Identifier beType, BlockItemPair... blocks) {
        List<Block> result = new ArrayList<>();
        for(BlockItemPair block : blocks)
        {
            if(block != null && block.block != null)
                result.add(block.block);
        }
        addBlocksToBlockEntity(beType, result);
    }
    public static void addBlockToBlockEntity2(Identifier beType, Collection<BlockItemPair> blocks)
    {
        List<Block> result = new ArrayList<>();
        for(BlockItemPair block : blocks)
        {
            if(block != null && block.block != null)
                result.add(block.block);
        }
        addBlocksToBlockEntity(beType, result);
    }
    public static void addBlocksToBlockEntity(Identifier beType, Collection<Block> blocks)
    {
        List<Block> list = blockList.getOrDefault(beType, new ArrayList<>());
        list.addAll(blocks);
        blockList.put(beType, list);
    }

    static {

        //Item Trader Blocks
        addBlocksToBlockEntity(ITEM_TRADER_TYPE, ModBlocks.DISPLAY_CASE, ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2, ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4);
        addBlocksToBlockEntity2(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE);
        addBlocksToBlockEntity2(ITEM_TRADER_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
        addBlocksToBlockEntity2(ITEM_TRADER_TYPE, ModBlocks.SHELF);
        addBlocksToBlockEntity2(ITEM_TRADER_TYPE, ModBlocks.CARD_DISPLAY);

        //Multi-block Capability Interface Blocks
        addBlocksToBlockEntity2(INVENTORY_EXTENSION_TYPE, ModBlocks.VENDING_MACHINE);
        addBlocksToBlockEntity2(INVENTORY_EXTENSION_TYPE, ModBlocks.VENDING_MACHINE_LARGE);
        addBlocksToBlockEntity(INVENTORY_EXTENSION_TYPE, ModBlocks.ARMOR_DISPLAY, ModBlocks.FREEZER);

    }

}