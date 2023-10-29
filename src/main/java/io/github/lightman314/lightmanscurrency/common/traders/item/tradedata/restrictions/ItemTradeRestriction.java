package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class ItemTradeRestriction {

    public static final Identifier DEFAULT_BACKGROUND = new Identifier(LightmansCurrency.MODID, "item/empty_item_slot");
    public static final Pair<Identifier,Identifier> BACKGROUND = Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, DEFAULT_BACKGROUND);

    public static final ItemTradeRestriction NONE = new ItemTradeRestriction();

    public ItemTradeRestriction() { }

    public ItemStack modifySellItem(ItemStack sellItem, String customName, ItemTradeData trade) { return sellItem; }

    public boolean allowSellItem(ItemStack itemStack) { return true; }

    public ItemStack filterSellItem(ItemStack itemStack) { return itemStack; }

    public boolean allowItemSelectItem(ItemStack itemStack) { return true; }

    public boolean allowExtraItemInStorage(ItemStack itemStack) { return false; }

    public int getSaleStock(TraderItemStorage traderStorage, ItemStack... sellItemList) {
        int minStock = Integer.MAX_VALUE;
        for(ItemStack sellItem : InventoryUtil.combineQueryItems(sellItemList))
            minStock = Math.min(this.getItemStock(sellItem, traderStorage), minStock);
        return minStock;
    }

    protected final int getItemStock(ItemStack sellItem, TraderItemStorage traderStorage)
    {
        if(sellItem.isEmpty())
            return Integer.MAX_VALUE;
        return traderStorage.getItemCount(sellItem) / sellItem.getCount();
    }

    public void removeItemsFromStorage(TraderItemStorage traderStorage, ItemStack... sellItemList)
    {
        for(ItemStack sellItem : sellItemList)
            this.removeFromStorage(sellItem, traderStorage);
    }

    protected final void removeFromStorage(ItemStack sellItem, TraderItemStorage traderStorage)
    {
        if(sellItem.isEmpty())
            return;
        traderStorage.removeItem(sellItem);
    }

    @Environment(EnvType.CLIENT)
    public Pair<Identifier,Identifier> getEmptySlotBG() { return BACKGROUND; }

}