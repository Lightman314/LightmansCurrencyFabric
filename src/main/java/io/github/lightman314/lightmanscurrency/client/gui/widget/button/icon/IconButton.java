package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class IconButton extends ButtonWidget {

    public static final int SIZE = 20;

    private Function<IconButton, IconData> iconSource;

    private Supplier<Boolean> activeCheck = () -> this.active;
    private Supplier<Boolean> visibilityCheck = () -> this.visible;

    public IconButton(int x, int y, PressAction pressable, @NotNull IconData icon)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable);
        this.setIcon(icon);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Supplier<IconData> iconSource)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable);
        this.setIcon(iconSource);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull IconData icon, TooltipSupplier tooltip)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, tooltip);
        this.setIcon(icon);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Supplier<IconData> iconSource, TooltipSupplier tooltip)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, tooltip);
        this.setIcon(iconSource);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Function<IconButton,IconData> iconSource, TooltipSupplier tooltip)
    {
        super(x,y,SIZE, SIZE, Text.empty(), pressable, tooltip);
        this.setIcon(iconSource);
    }

    public void setVisiblityCheck(Supplier<Boolean> visibilityCheck) {
        if(visibilityCheck == null)
            this.visibilityCheck = () -> this.visible;
        else
            this.visibilityCheck = visibilityCheck;
    }

    public void setActiveCheck(Supplier<Boolean> activeCheck) {
        if(activeCheck == null)
            this.activeCheck = () -> this.active;
        else
            this.activeCheck = activeCheck;
    }

    public void setIcon(@NotNull IconData icon) { this.iconSource = b -> icon; }

    public void setIcon(@NotNull Supplier<IconData> iconSource) { this.iconSource = b -> iconSource.get(); }

    public void setIcon(@NotNull Function<IconButton,IconData> iconSource) { this.iconSource = iconSource; }

    @Override
    public void render(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
        this.visible = this.visibilityCheck.get();
        this.active = this.activeCheck.get();
        super.render(pose, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1f,  1f,  1f, 1f);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        int offset = this.getYImage(this.hovered);
        this.drawTexture(matrixStack, this.x, this.y, 0, 46 + offset * 20, this.width / 2, this.height);
        this.drawTexture(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + offset * 20, this.width / 2, this.height);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);

        this.iconSource.apply(this).render(matrixStack, this, MinecraftClient.getInstance().textRenderer, this.x + 2, this.y + 2);

    }

}