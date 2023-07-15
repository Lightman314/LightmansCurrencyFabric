package io.github.lightman314.lightmanscurrency.util.config;

import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Consumer;

public class CustomConfigUtil {

    private static String modid = "minecraft";
    public static String getActiveModid() { return modid; }
    public static void setActiveModid(String newid) { if(newid != null) modid = newid; }
    public static void resetActiveModid() { setActiveModid("minecraft"); }

    public static void RegisterConfigListeners(Consumer<ModConfig> consumer) {
        ModConfigEvents.loading(modid).register(consumer::accept);
        ModConfigEvents.reloading(modid).register(consumer::accept);
    }

}
