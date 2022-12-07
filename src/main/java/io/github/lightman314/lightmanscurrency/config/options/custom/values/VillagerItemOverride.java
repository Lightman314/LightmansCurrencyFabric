package io.github.lightman314.lightmanscurrency.config.options.custom.values;

import io.github.lightman314.lightmanscurrency.common.LCConfigCommon;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class VillagerItemOverride {

    public final Identifier villagerType;
    public final Identifier newItemID;
    public Item getNewItem() { return Registry.ITEM.getOrEmpty(this.newItemID).orElse(LCConfigCommon.INSTANCE.defaultVillagerCoin.get()); }
    private VillagerItemOverride(Identifier villagerType, Identifier newItemID) { this.villagerType = villagerType; this.newItemID = newItemID; }

    public static VillagerItemOverride of(Identifier villagerType, Identifier newItem) { return new VillagerItemOverride(villagerType, newItem); }

}
