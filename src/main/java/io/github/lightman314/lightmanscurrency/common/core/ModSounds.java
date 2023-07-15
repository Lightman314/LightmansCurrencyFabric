package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

	public static final SoundEvent COINS_CLINKING = SoundEvent.of(new Identifier(LightmansCurrency.MODID, "coins_clinking"));

	public static void registerSounds() {
		Registry.register(Registries.SOUND_EVENT, new Identifier(LightmansCurrency.MODID, "coins_clinking"), COINS_CLINKING);
	}
	
}
