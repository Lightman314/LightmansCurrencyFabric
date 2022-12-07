package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class DropdownButton extends ButtonWidget {

    private final Text optionText;
    private final TextRenderer font;

    public DropdownButton(int x, int y, int width, TextRenderer font, Text optionText, PressAction pressable)
    {
        super(x , y, width, DropdownWidget.HEIGHT, Text.empty(), pressable);
        this.optionText = optionText;
        this.font = font;
    }

    @Override
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        //Draw the background
        RenderSystem.setShaderTexture(0, DropdownWidget.GUI_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = (this.hovered ? this.height : 0) + (DropdownWidget.HEIGHT * 2);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        this.drawTexture(poseStack, this.x, this.y, 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 4)
        {
            int xPart = Math.min(this.width - 4 - xOffset, 252);
            this.drawTexture(poseStack, this.x + 2 + xOffset, this.y, 2, offset, xPart, DropdownWidget.HEIGHT);
            xOffset += xPart;
        }
        this.drawTexture(poseStack, this.x + this.width - 2, this.y, 254, offset, 2, DropdownWidget.HEIGHT);
        //Draw the option text
        this.font.draw(poseStack, TextRenderUtil.fitString(this.optionText, this.width - 4), this.x + 2, this.y + 2, 0x404040);

    }

}