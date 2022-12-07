package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.WalletUpgradeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRecipes {

    public static final RecipeType<CoinMintRecipe> COIN_MINT_TYPE = new RecipeType<CoinMintRecipe>() { @Override public String toString() {return "lightmanscurrency:coin_mint"; }};

    public static final RecipeSerializer<CoinMintRecipe> COIN_MINT_SERIALIZER = new CoinMintRecipe.Serializer();
    public static final RecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = new WalletUpgradeRecipe.Serializer();

    public static void registerRecipes() {

        //Register Recipe Types
        Registry.register(Registry.RECIPE_TYPE, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT_TYPE);

        //Register Recipe Serializers
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT_SERIALIZER);
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(LightmansCurrency.MODID, "crafting_wallet_upgrade"), WALLET_UPGRADE);


    }

}
