package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.network.server.messages.coinmint.CMessageMintCoin;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.PlayerInfo;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MintScreen extends MenuScreen<MintMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/coinmint.png");

    private ButtonWidget buttonMint;

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

    }

    @Override
    protected void drawForeground(MatrixStack poseStack, int mouseX, int mouseY)
    {
        this.textRenderer.draw(poseStack, this.title, 8.0f, 6.0f, 0x404040);
        this.textRenderer.draw(poseStack, this.playerInventoryTitle, 8.0f, (this.backgroundHeight - 94), 0x404040);
    }

    @Override
    protected void init()
    {
        super.init();

        this.buttonMint = this.addDrawableChild(new PlainButton(this.x + 79, this.y + 21, 24, 16, this::mintCoin, GUI_TEXTURE, this.backgroundWidth, 0));
        this.buttonMint.visible = false;

        this.handledScreenTick();

    }

    @Override
    protected void handledScreenTick()
    {
        this.buttonMint.visible = this.handler.coinMint.validMintInput();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(matrixStack, mouseX,  mouseY);

        if(this.buttonMint != null && this.buttonMint.visible && this.buttonMint.isMouseOver(mouseX, mouseY))
        {
            if(this.handler.isMeltInput())
                this.renderTooltip(matrixStack, Text.translatable("gui.button.lightmanscurrency.melt"), mouseX, mouseY);
            else
                this.renderTooltip(matrixStack, Text.translatable("gui.button.lightmanscurrency.mint"), mouseX, mouseY);
        }

    }

    private void mintCoin(ButtonWidget button)
    {
        new CMessageMintCoin(Screen.hasShiftDown()).sendToServer();
    }

}