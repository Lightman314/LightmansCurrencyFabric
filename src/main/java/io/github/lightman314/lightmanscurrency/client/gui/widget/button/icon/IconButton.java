package io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class IconButton extends ButtonWidget {

    public static final int SIZE = 20;

    private Function<IconButton, IconData> iconSource;

    private Supplier<Tooltip> tooltip = () -> null;

    private Supplier<Boolean> activeCheck = () -> this.active;
    private Supplier<Boolean> visibilityCheck = () -> this.visible;

    public boolean disableTooltip = false;
    public IconButton tempDisableTooltip() { this.disableTooltip = true; return this; }

    public IconButton(int x, int y, PressAction pressable, @NotNull IconData icon)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.setIcon(icon);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Supplier<IconData> iconSource)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.setIcon(iconSource);
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull IconData icon, Supplier<Tooltip> tooltip)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.setIcon(icon);
        this.tooltip = tooltip;
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Supplier<IconData> iconSource, Supplier<Tooltip> tooltip)
    {
        super(x, y, SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.setIcon(iconSource);
        this.tooltip = tooltip;
    }

    public IconButton(int x, int y, PressAction pressable, @NotNull Function<IconButton,IconData> iconSource, Supplier<Tooltip> tooltip)
    {
        super(x,y,SIZE, SIZE, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
        this.setIcon(iconSource);
        this.tooltip = tooltip;
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
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.visible = this.visibilityCheck.get();
        this.active = this.activeCheck.get();
        if(this.active)
            this.setTooltip(this.tooltip.get());
        else
            this.setTooltip(null);
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isSelected()) {
            i = 2;
        }

        return 46 + i * 20;
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {

        gui.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        gui.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);

        this.iconSource.apply(this).render(gui, MinecraftClient.getInstance().textRenderer, this.getX() + 2, this.getY() + 2);

        gui.setShaderColor(1f,1f,1f,1f);

    }

}