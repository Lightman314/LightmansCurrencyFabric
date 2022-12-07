package io.github.lightman314.lightmanscurrency.common.core;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class ModGameRules {

    public static GameRules.Key<GameRules.BooleanRule> KEEP_WALLET;
    public static GameRules.Key<GameRules.IntRule> COIN_DROP_PERCENT;


    public static void registerGameRules()
    {
        KEEP_WALLET = GameRuleRegistry.register("keepWallet", GameRules.Category.PLAYER,GameRuleFactory.createBooleanRule(true));
        COIN_DROP_PERCENT = GameRuleRegistry.register("coinDropPercent", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 100));
    }

}
