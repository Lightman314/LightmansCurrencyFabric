package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

public class LogTab extends ATMTab{

    public LogTab(ATMScreen screen) { super(screen); }

    NotificationDisplayWidget logWidget;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.WRITABLE_BOOK); }

    @Override
    public MutableText getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.log"); }

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
    public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
        this.hideCoinSlots(pose);
        this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
    }

    @Override
    public void postRender(MatrixStack pose, int mouseX, int mouseY) { this.logWidget.tryRenderTooltip(pose, this.screen, mouseX, mouseY); }

    @Override
    public void tick() { }

    @Override
    public void onClose() { SimpleSlot.SetActive(this.screen.getScreenHandler()); }

}