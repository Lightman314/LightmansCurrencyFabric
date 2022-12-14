package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
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
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTick) {
        this.getScreenPosition(ScreenUtil.getScreenCorner(this.parent), this.isParentCreative).moveWidget(this);
        if(this.parent instanceof CreativeInventoryScreen cs)
            this.visible = cs.getSelectedTab() == ItemGroup.INVENTORY.getIndex();
        super.render(matrix, mouseX, mouseY, partialTick);
    }

    @NotNull
    protected abstract ScreenPosition getScreenPosition(ScreenPosition screenCorner, boolean isParentCreative);



}
