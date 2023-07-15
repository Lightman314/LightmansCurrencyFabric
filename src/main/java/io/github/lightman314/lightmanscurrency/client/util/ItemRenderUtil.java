package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


public class ItemRenderUtil {

    public static final Identifier EMPTY_SLOT_BG = new Identifier(LightmansCurrency.MODID, "items/empty_item_slot");
    public static final Pair<Identifier,Identifier> BACKGROUND = Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_SLOT_BG);

    public static final int ITEM_BLIT_OFFSET = 100;

    private static ItemStack alexHead = null;

    public static ItemStack getAlexHead()
    {
        if(alexHead != null)
            return alexHead;
        alexHead = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound headData = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        skullOwner.putIntArray("Id", new int[] {-731408145, -304985227, -1778597514, 158507129 });
        NbtCompound properties = new NbtCompound();
        NbtList textureList = new NbtList();
        NbtCompound texture = new NbtCompound();
        texture.putString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0=");
        textureList.add(texture);
        properties.put("textures", textureList);
        skullOwner.put("Properties", properties);
        headData.put("SkullOwner", skullOwner);
        alexHead.setNbt(headData);
        return alexHead;
    }

    /**
     * Draws an ItemStack.
     */
    public static void drawItemStack(DrawContext gui, TextRenderer font, ItemStack stack, int x, int y) { drawItemStack(gui, font, stack, x, y, null); }

    /**
     * Draws an ItemStack.
     */
    public static void drawItemStack(DrawContext gui, TextRenderer font, ItemStack stack, int x, int y, @Nullable String customCount) {

        if(font == null)
            font = MinecraftClient.getInstance().textRenderer;
        gui.drawItem(stack, x, y, 0);
        gui.drawItemInSlot(font, stack, x, y, customCount);

    }

    /**
     * Renders an item slots background
     */
    public static void drawSlotBackground(DrawContext gui, int x, int y, Pair<Identifier,Identifier> background)
    {
        if(background == null)
            return;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        Sprite textureatlassprite = minecraft.getSpriteAtlas(background.getFirst()).apply(background.getSecond());
        gui.drawSprite(x, y, ITEM_BLIT_OFFSET, 16, 16, textureatlassprite);
    }

    /**
     * Gets the tooltip for an item stack
     */
    public static List<Text> getTooltipFromItem(ItemStack stack) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        return stack.getTooltip(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
    }

}