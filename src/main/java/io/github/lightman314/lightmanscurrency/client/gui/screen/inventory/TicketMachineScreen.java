package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TicketMachineMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.ticket_machine.CMessageCraftTicket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY)
    {

        gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY)
    {
        gui.drawText(this.textRenderer, this.title, 8, 6, 0x404040, false);
        gui.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
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
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX,  mouseY);

        if(this.buttonCraft != null && this.buttonCraft.active && this.buttonCraft.isMouseOver(mouseX, mouseY))
        {
            if(this.handler.hasMasterTicket())
                gui.drawTooltip(this.textRenderer, Text.translatable("gui.button.lightmanscurrency.craft_ticket"), mouseX, mouseY);
            else
                gui.drawTooltip(this.textRenderer, Text.translatable("gui.button.lightmanscurrency.craft_master_ticket"), mouseX, mouseY);
        }

    }

    private void craftTicket(ButtonWidget button)
    {
        new CMessageCraftTicket(Screen.hasShiftDown()).sendToServer();
    }

}