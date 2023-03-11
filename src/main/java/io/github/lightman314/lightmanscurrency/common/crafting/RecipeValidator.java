package io.github.lightman314.lightmanscurrency.common.crafting;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;


public class RecipeValidator {

    public static Results getValidRecipes(World level)
    {
        Results results = new Results();
        RecipeManager recipeManager = level.getRecipeManager();
        for(Recipe<?> recipe : getRecipes(recipeManager, ModRecipes.COIN_MINT_TYPE))
        {
            if(recipe instanceof CoinMintRecipe)
            {
                CoinMintRecipe mintRecipe = (CoinMintRecipe)recipe;
                if(mintRecipe.isValid())
                {
                    results.coinMintRecipes.add(mintRecipe);
                }
            }
        }
        return results;
    }

    private static <C extends Inventory,T extends Recipe<C>> List<T> getRecipes(RecipeManager recipeManager, RecipeType<T> recipeType)
    {
        return recipeManager.listAllOfType(recipeType);
    }

    public static class Results
    {
        private final List<CoinMintRecipe> coinMintRecipes = Lists.newArrayList();

        public List<CoinMintRecipe> getCoinMintRecipes() { return this.coinMintRecipes; }
    }

}