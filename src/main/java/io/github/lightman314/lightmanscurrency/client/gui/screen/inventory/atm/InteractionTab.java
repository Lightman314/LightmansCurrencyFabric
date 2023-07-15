package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.BankAccountWidget.IBankAccountWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class InteractionTab extends ATMTab implements IBankAccountWidget{

    public InteractionTab(ATMScreen screen) { super(screen); }

    BankAccountWidget accountWidget;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModBlocks.COINPILE_GOLD); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.interact"); }

    @Override
    public void init() {

        this.accountWidget = new BankAccountWidget(this.screen.getGuiTop(), this, 14);
        this.accountWidget.getAmountSelection().drawBG = false;

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        Text accountName = Text.literal("ERROR FINDING ACCOUNT");
        if(this.screen.getScreenHandler().getBankAccount() != null)
            accountName = this.screen.getScreenHandler().getBankAccount().getName();
        gui.drawText(this.screen.getFont(), accountName, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6 + CoinValueInput.HEIGHT, 0x404040, false);
        this.accountWidget.renderInfo(gui);
    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) { }

    @Override
    public void tick() { this.accountWidget.tick(); }

    @Override
    public void onClose() { }

    @Override
    public <T extends Element & Drawable & Selectable> T addCustomWidget(T button) {
        if(button instanceof ClickableWidget)
            this.screen.addRenderableTabWidget((ClickableWidget)button);
        return button;
    }

    @Override
    public TextRenderer getFont() { return this.screen.getFont(); }

    @Override
    public Screen getScreen() { return this.screen; }

    @Override
    public BankAccount getBankAccount() { return this.screen.getScreenHandler().getBankAccount(); }

    @Override
    public Inventory getCoinAccess() { return this.screen.getScreenHandler().getCoinInput(); }

}