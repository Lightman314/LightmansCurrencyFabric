package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public class ModProfessions {

    public static final VillagerProfession BANKER = create(new Identifier(LightmansCurrency.MODID, "banker"), ModSounds.COINS_CLINKING);
    public static final VillagerProfession CASHIER = create(new Identifier(LightmansCurrency.MODID, "cashier"), ModSounds.COINS_CLINKING);


    public static void registerProfessions() {

        Registry.register(Registries.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "banker"), BANKER);
        Registry.register(Registries.VILLAGER_PROFESSION, new Identifier(LightmansCurrency.MODID, "cashier"), CASHIER);

        DebugUtil.DebugRegistryEntries("Registered Villager Professions:", Registries.VILLAGER_PROFESSION);

    }

    private static VillagerProfession create(Identifier id, SoundEvent sound) {
        Predicate<RegistryEntry<PointOfInterestType>> pred = p -> p.matchesId(id);
        return new VillagerProfession(id.toString(), pred, pred, ImmutableSet.of(), ImmutableSet.of(), sound);
    }

}
