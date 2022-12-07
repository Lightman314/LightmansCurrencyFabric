package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.ceil;

public class MarkAsSeenButton extends ButtonWidget {

    public static final int HEIGHT = 11;

    public MarkAsSeenButton(int rightPos, int yPos, Text text, PressAction onPress) {
        super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, onPress);
    }

    private static int getWidth(Text text) { return TextRenderUtil.getFont().getWidth(text) + 4; }

    @Override
    public void renderButton(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        TextRenderer font = minecraft.textRenderer;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isNarratable());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int topSize = this.height / 2;
        int bottomSize = topSize;
        if(this.height % 2 != 0)
            bottomSize++;
        this.drawTexture(pose, this.x, this.y, 0, 46 + i * 20, this.width / 2, topSize);
        this.drawTexture(pose, this.x, this.y + topSize, 0, 66 - bottomSize + i * 20, this.width / 2, bottomSize);
        this.drawTexture(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);
        this.drawTexture(pose, this.x + this.width / 2, this.y + topSize, 200 - this.width / 2, 66 - bottomSize + i * 20, this.width / 2, bottomSize);
        this.renderBackground(pose, minecraft, mouseX, mouseY);
        int j = this.getFGColor();
        drawCenteredText(pose, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255f) << 24);
    }

    private int getFGColor() { return this.active ? 16777215 : 10526880; /* White : Light Grey */ }

}