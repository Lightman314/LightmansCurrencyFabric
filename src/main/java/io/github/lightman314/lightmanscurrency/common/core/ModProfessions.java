package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.fabricmc.fabric.mixin.object.builder.VillagerProfessionAccessor;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class ModProfessions {

    public static final VillagerProfession BANKER = create("banker", ModPointsOfInterest.BANKER, ModSounds.COINS_CLINKING);
    public static final VillagerProfession CASHIER = create("cashier", ModPointsOfInterest.CASHIER, ModSounds.COINS_CLINKING);


    public static void registerProfessions() {

        Registry.register(Registry.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "banker"), BANKER);
        Registry.register(Registry.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "cashier"), CASHIER);

    }

    private static VillagerProfession create(String name, PointOfInterestType poiType, SoundEvent sound) {
        return VillagerProfessionAccessor.create(name, poiType, ImmutableSet.of(), ImmutableSet.of(), sound);
    }

}
