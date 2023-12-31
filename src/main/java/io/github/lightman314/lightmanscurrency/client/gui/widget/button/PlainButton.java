package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
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
        super(x, y, sizeX, sizeY, Text.empty(), pressable, DEFAULT_NARRATION_SUPPLIER);
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
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        gui.setShaderColor(1f, 1f, 1f, 1f);
        int offset = this.hovered ? this.height : 0;
        if(!this.active)
            gui.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        Pair<Integer,Integer> resource = this.resourceSource.get();
        gui.drawTexture(this.buttonResource, this.getX(), this.getY(), resource.getFirst(), resource.getSecond() + offset, this.width, this.height);

        gui.setShaderColor(1f,1f,1f,1f);

    }

}