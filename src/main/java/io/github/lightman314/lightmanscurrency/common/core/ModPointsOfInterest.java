package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class ModPointsOfInterest {

    private static PointOfInterestType BANKER = null;
    public static PointOfInterestType getBanker() {
        if(BANKER == null)
            registerPointsOfInterest();
        return BANKER;
    }
    private static PointOfInterestType CASHIER = null;
    public static PointOfInterestType getCashier() {
        if(CASHIER == null)
            registerPointsOfInterest();
        return CASHIER;
    }

    public static void registerPointsOfInterest() {
        if(BANKER != null)
            return;
        BANKER = PointOfInterestHelper.register(new Identifier(LightmansCurrency.MODID, "banker"), 1, 1, ModBlocks.MACHINE_ATM.block);
        CASHIER = PointOfInterestHelper.register(new Identifier(LightmansCurrency.MODID, "cashier"), 1, 1, ModBlocks.CASH_REGISTER.block);
    }

}
