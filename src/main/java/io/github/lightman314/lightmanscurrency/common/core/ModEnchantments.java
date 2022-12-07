package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.enchantments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEnchantments {

    public static final MoneyMendingEnchantment MONEY_MENDING = new MoneyMendingEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.values());
    public static final CoinMagnetEnchantment COIN_MAGNET = new CoinMagnetEnchantment(Enchantment.Rarity.COMMON);


    public static void registerEnchantments() {

        Registry.register(Registry.ENCHANTMENT, new Identifier(LightmansCurrency.MODID, "money_mending"), MONEY_MENDING);
        Registry.register(Registry.ENCHANTMENT, new Identifier(LightmansCurrency.MODID, "coin_magnet"), COIN_MAGNET);

    }

}
