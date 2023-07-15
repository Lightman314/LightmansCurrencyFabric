package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

//Copy/pasted from the ShapelessRecipe
public class WalletUpgradeRecipe implements CraftingRecipe {
    private final Identifier id;
    private final String group;
    private final ItemStack recipeOutput;
    private final DefaultedList<Ingredient> ingredients;
    //private final boolean isSimple;

    public WalletUpgradeRecipe(Identifier idIn, String groupIn, ItemStack recipeOutputIn, DefaultedList<Ingredient> ingredients) {
        this.id = idIn;
        this.group = groupIn;
        this.recipeOutput = recipeOutputIn;
        this.ingredients = ingredients;
        //this.isSimple = ingredients.stream().allMatch(Ingredient);
    }

    @Override
    public Identifier getId() { return this.id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.WALLET_UPGRADE; }

    /**
     * Recipes with equal group are combined into one button in the recipe book
     */
    public String getGroup() { return this.group; }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    @Override
    public ItemStack getOutput(DynamicRegistryManager manager) { return this.recipeOutput; }

    @Override
    public DefaultedList<Ingredient> getIngredients() { return this.ingredients; }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(RecipeInputInventory craftingInventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        int i = 0;
        for(int j = 0; j < craftingInventory.size(); ++j) {
            ItemStack itemStack = craftingInventory.getStack(j);
            if (!itemStack.isEmpty()) {
                ++i;
                recipeMatcher.addInput(itemStack, 1);
            }
        }

        return i == this.ingredients.size() && recipeMatcher.match(this, null);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager manager) {
        ItemStack output = this.recipeOutput.copy();
        ItemStack walletStack = this.getWalletStack(inv);
        if(!walletStack.isEmpty())
            WalletItem.CopyWalletContents(walletStack, output);
        return output;
    }

    private ItemStack getWalletStack(RecipeInputInventory inv) {
        for(int i = 0; i < inv.size(); i++)
        {
            ItemStack stack = inv.getStack(i);
            if(stack.getItem() instanceof WalletItem)
                return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean fits(int width, int height) { return width * height >= this.ingredients.size(); }

    public static class Serializer implements RecipeSerializer<WalletUpgradeRecipe> {

        @Override
        public WalletUpgradeRecipe read(Identifier recipeId, JsonObject json) {
            String s = JsonHelper.getString(json, "group", "");
            DefaultedList<Ingredient> nonnulllist = readIngredients(JsonHelper.getArray(json, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (nonnulllist.size() > 3 * 3) {
                throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + (3 * 3));
            } else {
                ItemStack itemstack = new ItemStack(ShapedRecipe.getItem(JsonHelper.getObject(json, "result")));
                return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
            }
        }

        private static DefaultedList<Ingredient> readIngredients(JsonArray ingredientArray) {
            DefaultedList<Ingredient> nonnulllist = DefaultedList.of();

            for(int i = 0; i < ingredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
                if (!ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        @Override
        public WalletUpgradeRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            String s = buffer.readString(32767);
            int i = buffer.readVarInt();
            DefaultedList<Ingredient> nonnulllist = DefaultedList.ofSize(i, Ingredient.EMPTY);

            for(int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, Ingredient.fromPacket(buffer));
            }

            ItemStack itemstack = buffer.readItemStack();
            return new WalletUpgradeRecipe(recipeId, s, itemstack, nonnulllist);
        }

        @Override
        public void write(PacketByteBuf buffer, WalletUpgradeRecipe recipe) {

            buffer.writeString(recipe.group);
            buffer.writeVarInt(recipe.ingredients.size());

            for(Ingredient ingredient : recipe.ingredients) {
                ingredient.write(buffer);
            }

            buffer.writeItemStack(recipe.recipeOutput);
        }
    }
}