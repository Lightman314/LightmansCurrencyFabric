package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class PlainButton extends ButtonWidget {

    private Identifier buttonResource;
    private Supplier<Pair<Integer,Integer>> resourceSource;


    public PlainButton(int x, int y, int sizeX, int sizeY, PressAction pressable, Identifier buttonResource, int resourceX, int resourceY) {
        this(x, y, sizeX, sizeY, pressable, buttonResource, () -> Pair.of(resourceX, resourceY));
    }

    public PlainButton(int x, int y, int sizeX, int sizeY, PressAction pressable, Identifier buttonResource, Supplier<Pair<Integer, Integer>> resourceSource)
    {
        super(x, y, sizeX, sizeY, Text.empty(), pressable);
        this.buttonResource = buttonResource;
        this.resourceSource = resourceSource;
    }

    public void setResource(Identifier buttonResource, int resourceX, int resourceY) { this.setResource(buttonResource, () -> Pair.of(resourceX, resourceY)); }

    public void setResource(Identifier buttonResource, Supplier<Pair<Integer, Integer>> resourceSource)
    {
        this.buttonResource = buttonResource;
        this.resourceSource = resourceSource;
    }

    @Override
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.buttonResource);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int offset = this.hovered ? this.height : 0;
        if(!this.active)
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        Pair<Integer,Integer> resource = this.resourceSource.get();
        this.drawTexture(poseStack, this.x, this.y, resource.getFirst(), resource.getSecond() + offset, this.width, this.height);

    }

}