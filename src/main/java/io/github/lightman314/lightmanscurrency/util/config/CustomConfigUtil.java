package io.github.lightman314.lightmanscurrency.util.config;

public class CustomConfigUtil {

    private static String modid = "minecraft";
    public static String getActiveModid() { return modid; }
    public static void setActiveModid(String newid) { if(newid != null) modid = newid; }
    public static void resetActiveModid() { setActiveModid("minecraft"); }

}
