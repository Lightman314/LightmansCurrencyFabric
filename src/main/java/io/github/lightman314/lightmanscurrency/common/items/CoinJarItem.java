package io.github.lightman314.lightmanscurrency.common.items;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoinJarItem extends BlockItem {

    public CoinJarItem(Block block, Settings properties) { super(block, properties); }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
    {
        super.appendTooltip(stack,  level,  tooltip,  flagIn);
        List<ItemStack> jarStorage = readJarData(stack);

        if(jarStorage.size() > 0)
        {
            if(Screen.hasShiftDown())
            {
                for(int i = 0; i < jarStorage.size(); i++)
                {
                    ItemStack coin = jarStorage.get(i);
                    if(coin.getCount() > 1)
                        tooltip.add(Text.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.multiple", coin.getCount(), coin.getName()));
                    else
                        tooltip.add(Text.translatable("tooptip.lightmanscurrency.coinjar.storedcoins.single", coin.getName()));
                }
            }
            else
            {
                tooltip.add(Text.translatable("tooptip.lightmanscurrency.coinjar.holdshift").formatted(Formatting.YELLOW));
            }
        }

    }

    private static List<ItemStack> readJarData(ItemStack stack)
    {
        List<ItemStack> storage = new ArrayList<>();
        if(stack.hasNbt())
        {
            NbtCompound compound = stack.getNbt();
            if(compound.contains("JarData", NbtElement.COMPOUND_TYPE))
            {
                NbtCompound jarData = compound.getCompound("JarData");
                if(jarData.contains("Coins"))
                {
                    NbtList storageList = jarData.getList("Coins", NbtElement.COMPOUND_TYPE);
                    for(int i = 0; i < storageList.size(); i++)
                    {
                        NbtCompound thisItem = storageList.getCompound(i);
                        storage.add(ItemStack.fromNbt(thisItem));
                    }
                }
            }
        }
        return storage;
    }

}