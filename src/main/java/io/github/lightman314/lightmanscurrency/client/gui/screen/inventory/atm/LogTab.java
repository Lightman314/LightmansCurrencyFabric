package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class LogTab extends ATMTab{

    public LogTab(ATMScreen screen) { super(screen); }

    NotificationDisplayWidget logWidget;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.log"); }

    @Override
    public void init() {

        SimpleSlot.SetInactive(this.screen.getScreenHandler());

        this.logWidget = this.screen.addRenderableTabWidget(new NotificationDisplayWidget(this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 15, this.screen.getImageWidth() - 14, 6, this.screen.getFont(), this::getNotifications));
        this.logWidget.backgroundColor = 0;

    }

    private List<Notification> getNotifications() {
        BankAccount ba = this.screen.getScreenHandler().getBankAccount();
        if(ba != null)
            return ba.getNotifications();
        return new ArrayList<>();
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.hideCoinSlots(gui);
        gui.drawText(this.screen.getFont(), this.getTooltip(), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);
    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) { this.logWidget.tryRenderTooltip(gui, mouseX, mouseY); }

    @Override
    public void tick() { }

    @Override
    public void onClose() { SimpleSlot.SetActive(this.screen.getScreenHandler()); }

}