package io.github.lightman314.lightmanscurrency.client.util;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


public class ItemRenderUtil {

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
    public static void drawItemStack(DrawableHelper gui, TextRenderer font, ItemStack stack, int x, int y) { drawItemStack(gui, font, stack, x, y, null); }

    /**
     * Draws an ItemStack.
     */
    public static void drawItemStack(DrawableHelper gui, TextRenderer font, ItemStack stack, int x, int y, @Nullable String customCount) {

        MinecraftClient minecraft = MinecraftClient.getInstance();

        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        PlayerEntity player = minecraft.player;
        Screen screen = minecraft.currentScreen;
        int imageWidth = 0;
        if(screen != null)
            imageWidth = screen.width;
        if(screen instanceof MenuScreen<?>)
            imageWidth = ((MenuScreen) screen).getImageWidth();

        if(font == null)
            font = minecraft.textRenderer;

        gui.setZOffset(ITEM_BLIT_OFFSET);
        itemRenderer.zOffset = ITEM_BLIT_OFFSET;

        RenderSystem.enableDepthTest();

        itemRenderer.renderInGuiWithOverrides(player, stack, x, y, x + y * imageWidth);
        itemRenderer.renderGuiItemOverlay(font, stack, x, y, customCount);

        itemRenderer.zOffset = 0.0F;
        gui.setZOffset(0);

    }

    /**
     * Renders an item slots background
     */
    public static void drawSlotBackground(MatrixStack matrixStack, int x, int y, Pair<Identifier,Identifier> background)
    {
        if(background == null)
            return;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        Sprite textureatlassprite = minecraft.getSpriteAtlas(background.getFirst()).apply(background.getSecond());
        RenderSystem.setShaderTexture(0, textureatlassprite.getAtlas().getId());
        Screen.drawSprite(matrixStack, x, y, ITEM_BLIT_OFFSET, 16, 16, textureatlassprite);
    }

    /**
     * Gets the tooltip for an item stack
     */
    public static List<Text> getTooltipFromItem(ItemStack stack) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        return stack.getTooltip(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
    }

}