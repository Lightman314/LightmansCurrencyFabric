package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.LCConfigCommon;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
    private final Ingredient ingredient;
    private final Item result;

    public CoinMintRecipe(Identifier id, MintType type, Ingredient ingredient, ItemConvertible result)
    {
        this.id = id;
        this.type = type;
        this.ingredient = ingredient;
        this.result = result.asItem();
    }

    public Ingredient getIngredient() { return this.ingredient; }
    public ItemStack getResult() { if(this.isValid()) return new ItemStack(this.result); return ItemStack.EMPTY; }
    public MintType getMintType() { return this.type; }

    public boolean allowed()
    {
        if(this.type == MintType.MINT)
        {
            return LCConfigCommon.INSTANCE.allowCoinMinting.get() && LCConfigCommon.INSTANCE.canMint(this.result);
        }
        else if(this.type == MintType.MELT)
        {
            try {
                return LCConfigCommon.INSTANCE.allowCoinMelting.get() && LCConfigCommon.INSTANCE.canMelt(this.ingredient.getMatchingStacks()[0].getItem());
            } catch(Exception e) { return false; }
        }
        return true;
    }

    public boolean isValid() { return !this.ingredient.isEmpty() && this.result.asItem() != Items.AIR && this.allowed(); }

    @Override
    public boolean matches(Inventory inventory, World level) {
        if(!this.isValid())
            return false;
        ItemStack firstStack = inventory.getStack(0);
        return this.ingredient.test(firstStack);
    }

    @Override
    public ItemStack craft(Inventory inventory) { return this.getResult(); }

    @Override
    public boolean fits(int width, int height) { return true; }

    @Override
    public ItemStack getOutput() { return this.getResult(); }

    @Override
    public Identifier getId() { return this.id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.COIN_MINT_SERIALIZER; }

    @Override
    public RecipeType<?> getType() { return ModRecipes.COIN_MINT_TYPE; }

    public static class Serializer implements RecipeSerializer<CoinMintRecipe> {

        @Override
        public CoinMintRecipe read(Identifier id, JsonObject json) {
            if(!json.has("ingredient"))
            {
                throw new JsonSyntaxException("Missing ingredient, expected to find an item.");
            }
            Ingredient ingredient = Ingredient.fromJson(json.getAsJsonObject("ingredient"));
            if(!json.has("result"))
            {
                throw new JsonSyntaxException("Missing result. Expected to find an item.");
            }
            ItemStack result = new ItemStack(Registry.ITEM.get(new Identifier(json.get("result").getAsString())));
            if(result.isEmpty())
            {
                throw new JsonSyntaxException("Result is empty.");
            }
            MintType type = MintType.OTHER;
            if(json.has("mintType"))
                type = CoinMintRecipe.readType(json.get("mintType"));

            return new CoinMintRecipe(id, type, ingredient, result.getItem());
        }

        @Override
        public CoinMintRecipe read(Identifier id, PacketByteBuf buffer) {
            CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readString());
            Ingredient ingredient = Ingredient.fromPacket(buffer);
            ItemStack result = buffer.readItemStack();
            return new CoinMintRecipe(id, type, ingredient, result.getItem());
        }

        @Override
        public void write(PacketByteBuf buffer, CoinMintRecipe recipe) {
            buffer.writeString(recipe.getMintType().name());
            recipe.getIngredient().write(buffer);
            buffer.writeItemStack(recipe.getResult());
        }
    }


}