package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TabButton extends ButtonWidget {

    public static final Identifier GUI_TEXTURE = TraderSettingsScreen.GUI_TEXTURE;

    public static final int SIZE = 25;

    public final ITab tab;
    private final TextRenderer font;

    private int rotation = 0;

    public TabButton(PressAction pressable, TextRenderer font, ITab tab)
    {
        super(0, 0, SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.font = font;
        this.tab = tab;
    }

    public void reposition(int x, int y, int rotation)
    {
        this.setPosition(x, y);
        this.rotation = MathUtil.clamp(rotation, 0, 3);
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        //Set the texture & color for the button
        float r = (float)(this.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        gui.setShaderColor(r * activeColor, g * activeColor, b * activeColor, 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        gui.drawTexture(GUI_TEXTURE, this.getX(), this.getY(), 200 + xOffset, yOffset, this.width, this.height);

        gui.setShaderColor(activeColor, activeColor, activeColor, 1f);
        this.tab.getIcon().render(gui, this.font, this.getX() + 4, this.getY() + 4);
        gui.setShaderColor(1f, 1f, 1f, 1f);

    }

    protected int getColor() { return this.tab.getColor(); }

    public void renderTooltip(DrawContext gui, int mouseX, int mouseY) {
        boolean wasActive = this.active;
        this.active = true;
        if(this.visible && this.isMouseOver(mouseX, mouseY))
            gui.drawTooltip(this.font, this.tab.getTooltip(), mouseX, mouseY);
        this.active = wasActive;
    }

}