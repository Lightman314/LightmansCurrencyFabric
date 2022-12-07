package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class EquipmentRestriction extends ItemTradeRestriction {

    private final EquipmentSlot equipmentType;

    public EquipmentRestriction(EquipmentSlot type)
    {
        this.equipmentType = type;
    }

    public EquipmentSlot getEquipmentSlot() { return this.equipmentType; }

    @Override
    public boolean allowSellItem(ItemStack itemStack)
    {
        return this.equippable(itemStack);
    }

    @Override
    public boolean allowItemSelectItem(ItemStack itemStack)
    {
        return this.equippable(itemStack);
    }

    private boolean equippable(ItemStack item) { return this.vanillaEquippable(item); }

    private boolean vanillaEquippable(ItemStack item) {
        try {
            return MobEntity.getPreferredEquipmentSlot(item) == this.equipmentType;
        } catch(Throwable t) { t.printStackTrace(); return false; }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Pair<Identifier,Identifier> getEmptySlotBG()
    {
        switch(this.equipmentType)
        {
            case HEAD:
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE);
            case CHEST:
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE);
            case LEGS:
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE);
            case FEET:
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE);
            default:
                return null;
        }
    }

}