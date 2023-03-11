package io.github.lightman314.lightmanscurrency.common.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.fabricmc.fabric.mixin.object.builder.PointOfInterestTypeAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Set;

public class ModPointsOfInterest {

    public static final PointOfInterestType BANKER = PointOfInterestTypeAccessor.callCreate("lightmanscurrency:banker", getBlockStates(ModBlocks.MACHINE_ATM.block), 1, 1);
    public static final PointOfInterestType CASHIER = PointOfInterestTypeAccessor.callCreate("lightmanscurrency:cashier", getBlockStates(ModBlocks.CASH_REGISTER.block), 1, 1);


    public static void registerPointsOfInterest() {
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, new Identifier(LightmansCurrency.MODID, "banker"), BANKER);
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, new Identifier(LightmansCurrency.MODID, "cashier"), CASHIER);
    }

    private static Set<BlockState> getBlockStates(Block block) { return ImmutableSet.copyOf(block.getStateManager().getStates()); }

}
