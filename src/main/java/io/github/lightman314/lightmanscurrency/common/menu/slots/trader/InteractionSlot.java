package io.github.lightman314.lightmanscurrency.common.menu.slots.trader;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class InteractionSlot extends Slot {

    public final List<InteractionSlotData> slotData;

    public InteractionSlot(List<InteractionSlotData> slotData, int x, int y) {
        super(new SimpleInventory(1), 0, x, y);
        this.slotData = slotData;
    }

    public boolean isType(String type) {
        for(InteractionSlotData slot : this.slotData)
        {
            if(slot.type.contentEquals(type))
                return true;
        }
        return false;
    }

    @Override
    public boolean isEnabled() { return this.slotData.size() > 0; }

    @Override
    public int getMaxItemCount() { return 1; }

    @Override
    public boolean canInsert(ItemStack stack) {
        return InteractionSlotData.allowItemInSlot(this.slotData, stack);
    }

    public List<Pair<Identifier,Identifier>> getPossibleBackgrounds() {
        List<Pair<Identifier,Identifier>> possibleBGs = new ArrayList<>();
        for(InteractionSlotData slot : this.slotData)
        {
            Pair<Identifier,Identifier> bg = slot.emptySlotBG();
            if(bg != null)
                possibleBGs.add(bg);
        }
        return possibleBGs;
    }

    @Override
    @Nullable
    @Environment(EnvType.CLIENT)
    public Pair<Identifier,Identifier> getBackgroundSprite() {
        MinecraftClient mc = MinecraftClient.getInstance();
        //Use the game time as a timer. Divide by 20 ticks to make the timer change the index once a second.
        int timer = (int)(mc.world.getTime() / 20);
        List<Pair<Identifier,Identifier>> bgs = this.getPossibleBackgrounds();
        if(bgs.size() > 0)
            return bgs.get(timer % bgs.size());
        return super.getBackgroundSprite();
    }

}