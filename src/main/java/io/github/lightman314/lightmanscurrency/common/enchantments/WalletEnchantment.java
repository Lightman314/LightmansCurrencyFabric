package io.github.lightman314.lightmanscurrency.common.enchantments;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public abstract class WalletEnchantment extends Enchantment {

    private final boolean requiresPickup;
    protected WalletEnchantment(Rarity rarity, boolean requiresPickup) { super(rarity, EnchantmentTarget.ARMOR, new EquipmentSlot[0]); this.requiresPickup = requiresPickup; }

    @Override
    public final boolean isAcceptableItem(ItemStack stack) { return stack.getItem() instanceof WalletItem walletItem && !(this.requiresPickup || WalletItem.CanPickup(walletItem)); }

    public abstract void addWalletTooltips(List<Text> tooltip, int enchantLevel, ItemStack item);

    public static void addWalletEnchantmentTooltips(List<Text> tooltip, ItemStack item) {
        EnchantmentHelper.get(item).forEach((e,l) -> {
            if(e instanceof WalletEnchantment we && l > 0)
                we.addWalletTooltips(tooltip, l, item);
        });
    }

}
