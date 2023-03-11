package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.TicketMachineMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.ticket_machine.CMessageCraftTicket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TicketMachineScreen extends MenuScreen<TicketMachineMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/ticket_machine.png");

    private ButtonWidget buttonCraft;

    public TicketMachineScreen(TicketMachineMenu container, PlayerInventory inventory, Text title)
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

        this.buttonCraft = this.addDrawableChild(new PlainButton(this.x + 79, this.y + 21, 24, 16, this::craftTicket, GUI_TEXTURE, this.backgroundWidth, 0));
        this.buttonCraft.visible = false;

    }

    @Override
    public void handledScreenTick()
    {

        this.buttonCraft.visible = this.handler.validInputs() && this.handler.roomForOutput();

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(matrixStack, mouseX,  mouseY);

        if(this.buttonCraft != null && this.buttonCraft.active && this.buttonCraft.isMouseOver(mouseX, mouseY))
        {
            if(this.handler.hasMasterTicket())
                this.renderTooltip(matrixStack, EasyText.translatable("gui.button.lightmanscurrency.craft_ticket"), mouseX, mouseY);
            else
                this.renderTooltip(matrixStack, EasyText.translatable("gui.button.lightmanscurrency.craft_master_ticket"), mouseX, mouseY);
        }

    }

    private void craftTicket(ButtonWidget button)
    {
        new CMessageCraftTicket(Screen.hasShiftDown()).sendToServer();
    }

}