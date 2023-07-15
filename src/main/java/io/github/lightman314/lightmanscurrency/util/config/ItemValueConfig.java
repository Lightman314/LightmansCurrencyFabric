package io.github.lightman314.lightmanscurrency.util.config;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemValueConfig implements Supplier<Item> {

    private final ForgeConfigSpec.ConfigValue<String> baseConfig;
    private final Supplier<Item> defaultSupplier;
    private final Supplier<ForgeConfigSpec> specSupplier;
    private final Predicate<Item> isAllowed;

    private Item cachedItem = null;
    private ItemValueConfig(ForgeConfigSpec.ConfigValue<String> baseConfig, Supplier<Item> defaultSupplier, Predicate<Item> isAllowed, Supplier<ForgeConfigSpec> specSupplier) {
        this.baseConfig = baseConfig;
        this.defaultSupplier = defaultSupplier;
        this.specSupplier = specSupplier;
        this.isAllowed = isAllowed;
        //Register to the mod event bus
        CustomConfigUtil.RegisterConfigListeners(this::onConfigReloaded);
    }

    public void onConfigReloaded(ModConfig config)
    {
        if(config.getSpec() == this.specSupplier.get())
            this.cachedItem = null;
    }

    @Override
    public Item get() {
        if(this.cachedItem != null)
            return this.cachedItem;
        String itemID = this.baseConfig.get();
        try{ this.cachedItem = Registries.ITEM.get(new Identifier(itemID));
        } catch(Throwable t) { LightmansCurrency.LogDebug("Error loading item from config value."); }
        if(this.cachedItem == null)
            this.cachedItem = this.defaultSupplier.get();
        //Test the predicate on each get, just in case the requirement has been updated.
        return this.isAllowed.test(this.cachedItem) ? this.cachedItem : this.defaultSupplier.get();
    }

    private static Supplier<Item> convertDefault(Identifier defaultItem) { return () -> Registries.ITEM.get(defaultItem); }

    private static boolean IsValidInput(Object o) {
        if(o instanceof String s)
        {
            try{
                return Registries.ITEM.get(new Identifier(s)) != Items.AIR;
            } catch(Throwable ignored) {}
        }
        return false;
    }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, Identifier defaultItem, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, convertDefault(defaultItem), specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, Identifier defaultItem, Supplier<Item> defaultItemSupplier, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, defaultItemSupplier, i -> true, specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, Identifier defaultItem, Predicate<Item> itemAllowed, Supplier<ForgeConfigSpec> specSupplier) { return define(builder, path, defaultItem, convertDefault(defaultItem), itemAllowed, specSupplier); }

    public static ItemValueConfig define(ForgeConfigSpec.Builder builder, String path, Identifier defaultItem, Supplier<Item> defaultItemSupplier, Predicate<Item> itemAllowed, Supplier<ForgeConfigSpec> specSupplier) {
        ForgeConfigSpec.ConfigValue<String> baseConfig = builder.define(path, defaultItem.toString(), ItemValueConfig::IsValidInput);
        return new ItemValueConfig(baseConfig, defaultItemSupplier, itemAllowed, specSupplier);
    }

}