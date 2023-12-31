package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class NetworkTraderButton extends ButtonWidget {

    public static final Identifier BUTTON_TEXTURES = new Identifier(LightmansCurrency.MODID, "textures/gui/universaltraderbuttons.png");

    public static final int WIDTH = 146;
    public static final int HEIGHT = 30;

    TraderData data;
    public TraderData getData() { return this.data; }

    TextRenderer font;

    public boolean selected = false;

    public NetworkTraderButton(int x, int y, PressAction pressable, TextRenderer font)
    {
        super(x, y, WIDTH, HEIGHT, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.font = font;
    }

    /**
     * Updates the trader data for this buttons trade.
     */
    public void SetData(TraderData data) { this.data = data; }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        //Set active status
        this.active = this.data != null && !this.selected;
        //Render nothing if there is no data
        if(this.data == null)
            return;

        if(this.active)
            gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        else
            gui.setShaderColor(0.5f, 0.5f, 0.5f, 1.0F);

        int offset = 0;
        if(this.hovered || this.selected)
            offset = HEIGHT;
        //Draw Button BG
        gui.drawTexture(BUTTON_TEXTURES, this.getX(), this.getY(), 0, offset, WIDTH, HEIGHT);

        //Draw the icon
        this.data.getIcon().render(gui, this.font, this.getX() + 4, this.getY() + 7);

        //Draw the name & owner of the trader
        Style style = this.data.isCreative() ? Style.EMPTY.withFormatting(Formatting.GREEN) : Style.EMPTY;
        gui.drawText(this.font, TextRenderUtil.fitString(this.data.getName(), this.width - 26, style), this.getX() + 24, this.getY() + 6, 0x404040, false);
        gui.drawText(this.font, TextRenderUtil.fitString(this.data.getOwner().getOwnerName(true), this.width - 26), this.getX() + 24, this.getY() + 16, 0x404040, false);

        gui.setShaderColor(1f,1f,1f,1f);

    }

}