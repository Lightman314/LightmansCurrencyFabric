package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModSounds {

	public static final SoundEvent COINS_CLINKING = new SoundEvent(new Identifier(LightmansCurrency.MODID, "coins_clinking"));
	
	public static void registerSounds() {
		Registry.register(Registry.SOUND_EVENT, new Identifier(LightmansCurrency.MODID, "coins_clinking"), COINS_CLINKING);
	}
	
}
