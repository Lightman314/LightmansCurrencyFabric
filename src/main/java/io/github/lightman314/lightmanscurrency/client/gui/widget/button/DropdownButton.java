package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DropdownButton extends ButtonWidget {

    private final Text optionText;
    private final TextRenderer font;

    public DropdownButton(int x, int y, int width, TextRenderer font, Text optionText, PressAction pressable)
    {
        super(x , y, width, DropdownWidget.HEIGHT, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.optionText = optionText;
        this.font = font;
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        //Draw the background
        gui.setShaderColor(1f, 1f, 1f, 1f);
        int offset = (this.hovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        gui.drawTexture(DropdownWidget.GUI_TEXTURE, this.getX(), this.getY(), 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
            int xPart = Math.min(this.width - 4 - xOffset, 252);
            gui.drawTexture(DropdownWidget.GUI_TEXTURE, this.getX() + 2 + xOffset, this.getY(), 2, offset, xPart, DropdownWidget.HEIGHT);
            xOffset += xPart;
        }
        gui.drawTexture(DropdownWidget.GUI_TEXTURE, this.getX() + this.width - 2, this.getY(), 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
        gui.drawText(this.font, TextRenderUtil.fitString(this.optionText, this.width - 4), this.getX() + 2, this.getY() + 2, 0x404040, false);
        gui.setShaderColor(1f,1f,1f,1f);

    }

}