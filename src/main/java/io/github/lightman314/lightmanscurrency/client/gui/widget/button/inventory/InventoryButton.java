package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class InventoryButton extends PlainButton {

    protected final HandledScreen<?> parent;
    protected final boolean isParentCreative;

    public InventoryButton(HandledScreen<?> parent, int sizeX, int sizeY, PressAction pressable, Identifier buttonResource, int resourceX, int resourceY) {
        super(0, 0, sizeX, sizeY, pressable, buttonResource, resourceX, resourceY);
        this.parent = parent;
        this.isParentCreative = this.parent instanceof CreativeInventoryScreen;
    }

    public InventoryButton(HandledScreen<?> parent, int sizeX, int sizeY, PressAction pressable, Identifier buttonResource, Supplier<Pair<Integer, Integer>> resourceSource) {
        super(0, 0, sizeX, sizeY, pressable, buttonResource, resourceSource);
        this.parent = parent;
        this.isParentCreative = this.parent instanceof CreativeInventoryScreen;
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTick) {
        this.getScreenPosition(ScreenUtil.getScreenCorner(this.parent), this.isParentCreative).setPosition(this);
        if(this.parent instanceof CreativeInventoryScreen cs)
            this.visible = cs.isInventoryTabSelected();
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @NotNull
    protected abstract ScreenPosition getScreenPosition(ScreenPosition screenCorner, boolean isParentCreative);



}
