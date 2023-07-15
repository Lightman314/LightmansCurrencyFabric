package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageSetBankNotificationLevel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NotificationTab extends ATMTab {

    public NotificationTab(ATMScreen screen) { super(screen); }

    CoinValueInput notificationSelection;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.notification"); }

    @Override
    public void init() {

        SimpleSlot.SetInactive(this.screen.getScreenHandler());

        Text accountName = this.screen.getScreenHandler().getPlayer().getDisplayName();
        if(this.screen.getScreenHandler().getBankAccount() != null)
            accountName = this.screen.getScreenHandler().getBankAccount() .getName();
        this.notificationSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), accountName, this.screen.getScreenHandler().getBankAccount().getNotificationValue(), this.screen.getFont(), this::onValueChanged, this.screen::addRenderableTabWidget));
        this.notificationSelection.drawBG = false;
        this.notificationSelection.allowFreeToggle = false;
        this.notificationSelection.init();

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.hideCoinSlots(gui);

        BankAccount account = this.screen.getScreenHandler().getBankAccount();
        if(account != null)
            TextRenderUtil.drawCenteredMultilineText(gui, account.getNotificationLevel() > 0 ? Text.translatable("gui.lightmanscurrency.notification.details", account.getNotificationValue().getString()) : Text.translatable("gui.lightmanscurrency.notification.disabled"), this.screen.getGuiLeft() + 5, this.screen.getImageWidth() - 10, this.screen.getGuiTop() + 70, 0x404040);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) { }

    @Override
    public void tick() { this.notificationSelection.tick(); }

    @Override
    public void onClose() { SimpleSlot.SetActive(this.screen.getScreenHandler()); }

    public void onValueChanged(CoinValue value) {
        this.screen.getScreenHandler().getBankAccount().setNotificationValue(value);
        new CMessageSetBankNotificationLevel(value).sendToServer();
    }

}