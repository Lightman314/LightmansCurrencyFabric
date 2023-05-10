package io.github.lightman314.lightmanscurrency.util;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class DebugUtil {

    public static String getItemDebug(ItemStack item) { return item.getCount() + "x " + Registry.ITEM.getId(item.getItem()); }

    public static String getSideText(Entity entity) { return getSideText(entity.world); }

    public static String getSideText(World level) { return level.isClient ? "client" : "server"; }

    public static void DebugRegistryEntries(String initialMessage, Registry<?> registry)
    {
        LightmansCurrency.LogDebug(initialMessage);

        registry.getIds().forEach(type -> LightmansCurrency.LogDebug(type.toString()));

    }

}