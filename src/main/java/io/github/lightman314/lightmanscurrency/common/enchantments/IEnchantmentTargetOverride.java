package io.github.lightman314.lightmanscurrency.common.enchantments;

import net.minecraft.item.Item;

/**
 * Interface to be attached to
 */
public interface IEnchantmentTargetOverride {
    boolean overrideIsAcceptableItem(Item item);
}
