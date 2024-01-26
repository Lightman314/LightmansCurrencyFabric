package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MintScreen extends MenuScreen<MintMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/coinmint.png");

    public MintScreen(MintMenu container, PlayerInventory inventory, Text title)
    {
        super(container, inventory, title);
        this.backgroundHeight = 138;
        this.backgroundWidth = 176;
    }

    @Override
    protected void drawBackground(MatrixStack poseStack, float partialTicks, int mouseX, int mouseY)
    {


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.drawTexture(poseStack, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if(this.handler.coinMint == null)
            return;
        int blitWidth = MathUtil.clamp((int)(23 * this.handler.coinMint.getMintProgress()), 0, 22);
        this.drawTexture(poseStack, this.x + 80, this.y + 21, 176, 0, blitWidth, 16);



    }

    @Override
    protected void drawForeground(MatrixStack poseStack, int mouseX, int mouseY)
    {
        this.textRenderer.draw(poseStack, this.title, 8.0f, 6.0f, 0x404040);
        this.textRenderer.draw(poseStack, this.playerInventoryTitle, 8.0f, (this.backgroundHeight - 94), 0x404040);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(matrixStack, mouseX,  mouseY);

    }

}