package io.github.lightman314.lightmanscurrency.util.config;

import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Consumer;

public class CustomConfigUtil {

    public static void setActiveModid(String newid) { }
    public static void resetActiveModid() { }

    public static void RegisterConfigListeners(Consumer<ModConfig> consumer) {
        ModConfigEvent.LOADING.register(consumer::accept);
        ModConfigEvent.LOADING.register(consumer::accept);
    }

}