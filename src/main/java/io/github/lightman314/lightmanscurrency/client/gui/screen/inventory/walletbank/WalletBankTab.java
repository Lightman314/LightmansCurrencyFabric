package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletBankScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import net.minecraft.client.gui.DrawContext;

public abstract class WalletBankTab implements ITab
{
    protected final WalletBankScreen screen;

    protected WalletBankTab(WalletBankScreen screen) { this.screen = screen; }

    public abstract void init();

    public abstract void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks);

    public abstract void postRender(DrawContext gui, int mouseX, int mouseY);

    public abstract void tick();

    public abstract void onClose();

    public boolean blockInventoryClosing() { return false; }

    public final int getColor() { return 0xFFFFFF; }

}