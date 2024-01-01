package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public abstract class SlotMachineRenderBlock extends DrawableHelper {

    public abstract void render(MatrixStack pose, int x, int y);
    public final int weight;
    protected SlotMachineRenderBlock(int weight) { this.weight = weight; }

    public static SlotMachineRenderBlock empty() { return Empty.INSTANCE; }
    public static SlotMachineRenderBlock forItem(int weight, ItemStack item) { return new ItemBlock(weight, item); }

    private static class ItemBlock extends SlotMachineRenderBlock
    {
        private final ItemStack item;
        protected ItemBlock(int weight, ItemStack item) { super(weight); this.item = item.copy(); }
        @Override
        public void render(MatrixStack pose, int x, int y) {
            ItemRenderUtil.drawItemStack(this, null, this.item, x, y);
        }
    }

    private static class Empty extends SlotMachineRenderBlock
    {
        protected static final SlotMachineRenderBlock INSTANCE = new Empty();
        private Empty() { super(0); }
        @Override
        public void render(MatrixStack pose, int x, int y) {
            RenderSystem.setShaderTexture(0, IconAndButtonUtil.ICON_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f,1f,1f,1f);
            this.drawTexture(pose, x, y, 16, 32, 16, 16); }
    }

}