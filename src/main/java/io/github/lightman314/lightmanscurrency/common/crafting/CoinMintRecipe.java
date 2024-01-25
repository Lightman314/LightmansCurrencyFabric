package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CoinMintRecipe implements Recipe<Inventory> {

    public enum MintType { MINT, MELT, OTHER }

    public static MintType readType(JsonElement json)
    {
        try {
            return readType(json.getAsString());
        } catch(Exception e) { e.printStackTrace(); return MintType.OTHER; }
    }

    public static MintType readType(String typeName)
    {
        for(MintType type : MintType.values())
        {
            if(type.name().equals(typeName))
                return type;
        }
        return MintType.OTHER;
    }

    private final Identifier id;
    private final MintType type;
    private final int duration;
    private final Ingredient ingredient;
    public final int ingredientCount;
    private final ItemStack result;

    public CoinMintRecipe(Identifier id, MintType type, int duration, Ingredient ingredient, int ingredientCount, ItemStack result)
    {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.ingredient = ingredient;
        this.ingredientCount = Math.max(ingredientCount,1);
        this.result = result;
    }

    public Ingredient getIngredient() { return this.ingredient; }
    @Override
    public DefaultedList<Ingredient> getIngredients() { return DefaultedList.ofSize(1, this.ingredient); }

    public MintType getMintType() { return this.type; }

    public boolean allowed() { return LCConfig.SERVER.allowCoinMintRecipe(this); }

    public boolean shouldShowInREI() { return !this.ingredient.isEmpty() && this.result.getItem() != Items.AIR; }

    public boolean isValid() { return !this.ingredient.isEmpty() && this.result.getItem() != Items.AIR && this.allowed(); }

    @Override
    public boolean matches(Inventory inventory, World level) {
        if(!this.isValid())
            return false;
        ItemStack firstStack = inventory.getStack(0);
        return this.ingredient.test(firstStack);
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager manager) { return this.getOutput(manager); }

    @Override
    public boolean fits(int width, int height) { return true; }

    public ItemStack getOutputItem() { return this.result.copy(); }

    @Override
    public ItemStack getOutput(DynamicRegistryManager manager) { if(this.isValid()) return this.result.copy(); return ItemStack.EMPTY; }

    public int getInternalDuration() { return this.duration; }
    public int getDuration() { return this.duration > 0 ? this.duration : LCConfig.SERVER.coinMintDefaultDuration.get(); }

    @Override
    public Identifier getId() { return this.id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.COIN_MINT_SERIALIZER; }

    @Override
    public RecipeType<?> getType() { return ModRecipes.COIN_MINT_TYPE; }

    public static class Serializer implements RecipeSerializer<CoinMintRecipe> {

        @Override
        public CoinMintRecipe read(Identifier id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            int ingredientCount = JsonHelper.getInt(json, "count", 1);

            ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
            if(result.isEmpty())
                throw new JsonSyntaxException("Result is empty.");
            MintType type = MintType.OTHER;
            if(json.has("mintType"))
                type = CoinMintRecipe.readType(JsonHelper.getString(json, "mintType", "OTHER"));

            int duration = JsonHelper.getInt(json, "duration", 0);

            return new CoinMintRecipe(id, type, duration, ingredient, ingredientCount, result);
        }

        @Override
        public CoinMintRecipe read(Identifier id, PacketByteBuf buffer) {
            CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readString());
            Ingredient ingredient = Ingredient.fromPacket(buffer);
            int ingredientCount = buffer.readInt();
            ItemStack result = buffer.readItemStack();
            int duration = buffer.readInt();
            return new CoinMintRecipe(id, type, duration, ingredient, ingredientCount, result);
        }

        @Override
        public void write(PacketByteBuf buffer, CoinMintRecipe recipe) {
            buffer.writeString(recipe.getMintType().name());
            recipe.getIngredient().write(buffer);
            buffer.writeInt(recipe.ingredientCount);
            buffer.writeItemStack(recipe.getOutputItem());
            buffer.writeInt(recipe.getInternalDuration());
        }
    }


}