package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import net.minecraft.client.gui.DrawContext;

public abstract class ATMTab implements ITab
{
    protected final ATMScreen screen;

    protected ATMTab(ATMScreen screen) { this.screen = screen; }

    public abstract void init();

    public abstract void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks);

    public abstract void postRender(DrawContext gui, int mouseX, int mouseY);

    public abstract void tick();

    public abstract void onClose();

    public boolean blockInventoryClosing() { return false; }

    public final int getColor() { return 0xFFFFFF; }

    protected final void hideCoinSlots(DrawContext gui) {
        gui.drawTexture(ATMScreen.GUI_TEXTURE, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 128, 7, 79, 162, 18);
    }

}