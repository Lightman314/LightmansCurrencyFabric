package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CashRegisterItem extends BlockItem {

    private static final SoundEvent soundEffect = SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;

    public CashRegisterItem(Block block, Settings properties) {super(block, properties.maxCount(1));}

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        BlockPos lookPos = context.getBlockPos();
        World level = context.getWorld();
        if(lookPos != null)
        {
            if(level.getBlockState(lookPos).getBlock() instanceof ITraderBlock block)
            {
                BlockEntity blockEntity = block.getBlockEntity(level.getBlockState(lookPos), level, lookPos);
                if(!HasEntity(context.getStack(), blockEntity) && blockEntity instanceof TraderBlockEntity)
                {
                    AddEntity(context.getStack(), blockEntity);

                    if(level.isClient)
                    {
                        level.playSound(context.getPlayer(), blockEntity.getPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 0f);
                    }

                    return ActionResult.SUCCESS;
                }
                else if(blockEntity instanceof TraderBlockEntity) //Return even if we have the entity to prevent any accidental placements.
                {
                    if(level.isClient)
                    {
                        level.playSound(context.getPlayer(), blockEntity.getPos(), soundEffect, SoundCategory.NEUTRAL, 1f, 1.35f);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return super.useOnBlock(context);

    }

    private boolean HasEntity(ItemStack stack, BlockEntity blockEntity)
    {

        //Get the tag
        if(!stack.hasNbt())
            return false;

        NbtCompound tag = stack.getOrCreateNbt();

        if(!tag.contains("TraderPos"))
            return false;

        NbtList storageList = tag.getList("TraderPos", NbtElement.COMPOUND_TYPE);

        for(int i = 0; i < storageList.size(); i++)
        {
            NbtCompound thisEntry = storageList.getCompound(i);
            if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
            {
                if(thisEntry.getInt("x") == blockEntity.getPos().getX() && thisEntry.getInt("y") == blockEntity.getPos().getY() && thisEntry.getInt("z") == blockEntity.getPos().getZ())
                    return true;
            }
        }

        return false;

    }

    private void AddEntity(ItemStack stack, BlockEntity blockEntity)
    {
        //Get the tag
        NbtCompound tag = stack.getOrCreateNbt();

        //If the tag contains the TraderPos list, get it. Otherwise create a new list
        NbtList storageList;
        if(tag.contains("TraderPos"))
            storageList = tag.getList("TraderPos", NbtElement.COMPOUND_TYPE);
        else
            storageList = new NbtList();

        //Create the new entry to the list
        NbtCompound newEntry = new NbtCompound();
        newEntry.putInt("x", blockEntity.getPos().getX());
        newEntry.putInt("y", blockEntity.getPos().getY());
        newEntry.putInt("z", blockEntity.getPos().getZ());

        //Add the new entry to the list
        storageList.add(newEntry);

        //Put the modified list into the tag
        tag.put("TraderPos", storageList);

    }

    private List<BlockPos> readNBT(ItemStack stack)
    {
        List<BlockPos> positions = new ArrayList<>();

        //Get the tag
        if(!stack.hasNbt())
            return positions;

        NbtCompound tag = stack.getOrCreateNbt();
        if(tag.contains("TraderPos"))
        {
            NbtList list = tag.getList("TraderPos", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < list.size(); i++)
            {
                NbtCompound thisPos = list.getCompound(i);
                if(thisPos.contains("x") && thisPos.contains("y") && thisPos.contains("z"))
                {
                    positions.add(new BlockPos(thisPos.getInt("x"),thisPos.getInt("y"),thisPos.getInt("z")));
                }
            }
        }

        return positions;

    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flag)
    {
        super.appendTooltip(stack,  level,  tooltip,  flag);
        List<BlockPos> data = this.readNBT(stack);

        TooltipItem.addTooltipAlways(tooltip, LCTooltips.CASH_REGISTER);

        tooltip.add(Text.translatable("tooptip.lightmanscurrency.cash_register", data.size()));

        if(!Screen.hasShiftDown() || data.size() == 0)
        {
            tooltip.add(Text.translatable("tooptip.lightmanscurrency.cash_register.instructions"));
        }

        if(Screen.hasShiftDown())
        {
            //Display details of the
            for(int i = 0; i < data.size(); i++)
            {
                tooltip.add(Text.translatable("tooltip.lightmanscurrency.cash_register.details", i + 1, data.get(i).getX(), data.get(i).getY(), data.get(i).getZ()));
            }
        }
        else if(data.size() > 0)
        {
            tooltip.add(Text.translatable("tooptip.lightmanscurrency.cash_register.holdshift").formatted(Formatting.YELLOW));
        }
    }

}