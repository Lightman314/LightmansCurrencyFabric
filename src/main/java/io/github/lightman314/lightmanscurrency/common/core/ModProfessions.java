package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public class ModProfessions {

    public static final VillagerProfession BANKER = create("banker", new Identifier(LightmansCurrency.MODID,"banker"), ModSounds.COINS_CLINKING);
    public static final VillagerProfession CASHIER = create("cashier", new Identifier(LightmansCurrency.MODID,"cashier"), ModSounds.COINS_CLINKING);


    public static void registerProfessions() {

        Registry.register(Registry.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "banker"), BANKER);
        Registry.register(Registry.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "cashier"), CASHIER);

    }

    private static VillagerProfession create(String name, Identifier poiType, SoundEvent sound) {
        Predicate<RegistryEntry<PointOfInterestType>> pred = p -> p.matchesId(poiType);
        return new VillagerProfession(name, pred, pred, ImmutableSet.of(), ImmutableSet.of(), sound);
    }

}
