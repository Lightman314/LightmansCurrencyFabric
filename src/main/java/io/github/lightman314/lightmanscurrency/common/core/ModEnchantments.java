package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.enchantments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static final MoneyMendingEnchantment MONEY_MENDING = new MoneyMendingEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.values());
    public static final CoinMagnetEnchantment COIN_MAGNET = new CoinMagnetEnchantment(Enchantment.Rarity.COMMON);


    public static void registerEnchantments() {

        Registry.register(Registries.ENCHANTMENT, new Identifier(LightmansCurrency.MODID, "money_mending"), MONEY_MENDING);
        Registry.register(Registries.ENCHANTMENT, new Identifier(LightmansCurrency.MODID, "coin_magnet"), COIN_MAGNET);

    }

}
