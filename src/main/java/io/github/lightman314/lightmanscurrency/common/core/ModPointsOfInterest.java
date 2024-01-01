package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModPointsOfInterest {

    public static void registerPointsOfInterest() {

        PointOfInterestHelper.register(new Identifier(LightmansCurrency.MODID, "banker"), 1, 1, ModBlocks.MACHINE_ATM.block);
        PointOfInterestHelper.register(new Identifier(LightmansCurrency.MODID, "cashier"), 1, 1, ModBlocks.CASH_REGISTER.block);

        DebugUtil.DebugRegistryEntries("Registered Poi Types:", Registries.POINT_OF_INTEREST_TYPE);

    }

}
