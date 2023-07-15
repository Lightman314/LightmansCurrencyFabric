package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Set;

public class ModPointsOfInterest {

    public static final PointOfInterestType BANKER = new PointOfInterestType(getBlockStates(ModBlocks.MACHINE_ATM.block), 1, 1);
    public static final PointOfInterestType CASHIER = new PointOfInterestType(getBlockStates(ModBlocks.CASH_REGISTER.block), 1, 1);


    public static void registerPointsOfInterest() {

        Registry.register(Registries.POINT_OF_INTEREST_TYPE, new Identifier(LightmansCurrency.MODID, "banker"), BANKER);
        Registry.register(Registries.POINT_OF_INTEREST_TYPE, new Identifier(LightmansCurrency.MODID, "cashier"), CASHIER);

        DebugUtil.DebugRegistryEntries("Registered Poi Types:", Registries.POINT_OF_INTEREST_TYPE);

    }

    private static Set<BlockState> getBlockStates(Block block) { return ImmutableSet.copyOf(block.getStateManager().getStates()); }

}
