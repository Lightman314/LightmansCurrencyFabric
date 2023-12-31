package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.WalletMagnetUpgradeRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.WalletUpgradeRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {

    public static final RecipeType<CoinMintRecipe> COIN_MINT_TYPE = new RecipeType<>() { @Override public String toString() {return "lightmanscurrency:coin_mint"; }};

    public static final RecipeSerializer<CoinMintRecipe> COIN_MINT_SERIALIZER = new CoinMintRecipe.Serializer();
    public static final RecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = new WalletUpgradeRecipe.Serializer();
    public static final RecipeSerializer<WalletMagnetUpgradeRecipe> WALLET_MAGNET_UPGRADE = new SpecialRecipeSerializer<>(WalletMagnetUpgradeRecipe::new);

    public static void registerRecipes() {

        //Register Recipe Types
        Registry.register(Registries.RECIPE_TYPE, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT_TYPE);

        //Register Recipe Serializers
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(LightmansCurrency.MODID, "coin_mint"), COIN_MINT_SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(LightmansCurrency.MODID, "crafting_wallet_upgrade"), WALLET_UPGRADE);
        Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(LightmansCurrency.MODID, "wallet_coin_magnet_upgrade"), WALLET_MAGNET_UPGRADE);


    }

}
