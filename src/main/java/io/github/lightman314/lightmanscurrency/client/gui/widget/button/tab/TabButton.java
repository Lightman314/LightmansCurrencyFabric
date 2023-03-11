package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TabButton extends ButtonWidget {

    public static final Identifier GUI_TEXTURE = TraderSettingsScreen.GUI_TEXTURE;

    public static final int SIZE = 25;

    public final ITab tab;
    private final TextRenderer font;

    private int rotation = 0;

    public TabButton(PressAction pressable, TextRenderer font, ITab tab)
    {
        super(0, 0, SIZE, SIZE, EasyText.empty(), pressable);
        this.font = font;
        this.tab = tab;
    }

    public void reposition(int x, int y, int rotation)
    {
        this.x = x;
        this.y = y;
        this.rotation = MathUtil.clamp(rotation, 0, 3);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        //Set the texture & color for the button
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        float r = (float)(this.getColor() >> 16 & 255) / 255f;
        float g = (float)(this.getColor() >> 8 & 255) / 255f;
        float b = (float)(this.getColor()& 255) / 255f;
        float activeColor = this.active ? 1f : 0.5f;
        RenderSystem.setShaderColor(r * activeColor, g * activeColor, b * activeColor, 1f);
        int xOffset = this.rotation < 2 ? 0 : this.width;
        int yOffset = (this.rotation % 2 == 0 ? 0 : 2 * this.height) + (this.active ? 0 : this.height);
        //Render the background
        this.drawTexture(matrixStack, x, y, 200 + xOffset, yOffset, this.width, this.height);

        RenderSystem.setShaderColor(activeColor, activeColor, activeColor, 1f);
        this.tab.getIcon().render(matrixStack, this, this.font, this.x + 4, this.y + 4);

    }

    protected int getColor() { return this.tab.getColor(); }

    public void renderTooltip(MatrixStack pose, int mouseX, int mouseY, Screen screen) {
        boolean wasActive = this.active;
        this.active = true;
        if(this.visible && this.isMouseOver(mouseX, mouseY))
            screen.renderTooltip(pose, this.tab.getTooltip(), mouseX, mouseY);
        this.active = wasActive;
    }

}