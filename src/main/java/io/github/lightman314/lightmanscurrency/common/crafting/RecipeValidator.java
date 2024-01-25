package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;


public class RecipeValidator {

    public static List<CoinMintRecipe> getValidMintRecipes(World level)
    {
        List<CoinMintRecipe> results = new ArrayList<>();
        RecipeManager recipeManager = level.getRecipeManager();
        for(Recipe<?> recipe : getRecipes(recipeManager, ModRecipes.COIN_MINT_TYPE))
        {
            if(recipe instanceof CoinMintRecipe mintRecipe)
            {
                if(mintRecipe.isValid())
                    results.add(mintRecipe);
            }
        }
        return ImmutableList.copyOf(results);
    }

    private static <C extends Inventory,T extends Recipe<C>> List<T> getRecipes(RecipeManager recipeManager, RecipeType<T> recipeType)
    {
        return recipeManager.listAllOfType(recipeType);
    }

}