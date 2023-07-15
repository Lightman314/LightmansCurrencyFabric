package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.core;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class NotificationTab extends SettingsTab {

    public static final NotificationTab INSTANCE = new NotificationTab();

    @Override
    public int getColor() { return 0xFFFFFF; }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.ENDER_PEARL); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.settings.notifications"); }

    private NotificationTab() { }

    PlainButton buttonToggleNotifications;
    PlainButton buttonToggleChatNotifications;
    ButtonWidget buttonToggleTeamLevel;

    @Override
    public boolean canOpen() { return this.hasPermissions(Permissions.TRANSFER_OWNERSHIP); }

    @Override
    public void initTab() {

        TraderSettingsScreen screen = this.getScreen();

        this.buttonToggleNotifications = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 35, 10, 10, this::ToggleNotifications, TraderSettingsScreen.GUI_TEXTURE, 10, 200));

        this.buttonToggleChatNotifications = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 55, 10, 10, this::ToggleChatNotifications, TraderSettingsScreen.GUI_TEXTURE, 10, 200));

        this.buttonToggleTeamLevel = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 80, screen.xSize - 40, 20, Text.empty(), this::ToggleTeamNotificationLevel));

        this.tick();

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        TraderSettingsScreen screen = this.getScreen();
        TraderData trader = this.getTrader();

        //Render the enable notification test
        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.notifications.enabled"), screen.guiLeft() + 32, screen.guiTop() + 35, 0x404040, false);

        //Render the enable chat notification text
        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.notifications.chat"), screen.guiLeft() + 32, screen.guiTop() + 55, 0x404040, false);

        this.buttonToggleTeamLevel.visible = trader.getOwner().hasTeam();
        if(this.buttonToggleTeamLevel.visible)
        {
            Text message = Text.translatable("gui.button.lightmanscurrency.team.bank.notifications", Text.translatable("gui.button.lightmanscurrency.team.bank.limit." + trader.teamNotificationLevel()));
            this.buttonToggleTeamLevel.setMessage(message);
        }

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() {

        TraderData trader = this.getTrader();
        if(trader != null)
        {
            this.buttonToggleNotifications.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.notificationsEnabled() ? 200 : 220);
            this.buttonToggleChatNotifications.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.notificationsToChat() ? 200 : 220);
        }

    }

    @Override
    public void closeTab() { }

    private void ToggleNotifications(ButtonWidget button) {
        NbtCompound message = new NbtCompound();
        message.putBoolean("Notifications", !this.getTrader().notificationsEnabled());
        this.getTrader().sendNetworkMessage(message);
    }

    private void ToggleChatNotifications(ButtonWidget button) {
        NbtCompound message = new NbtCompound();
        message.putBoolean("NotificationsToChat", !this.getTrader().notificationsToChat());
        this.getTrader().sendNetworkMessage(message);
    }

    private void ToggleTeamNotificationLevel(ButtonWidget button) {
        NbtCompound message = new NbtCompound();
        message.putInt("TeamNotificationLevel", Team.NextBankLimit(this.getTrader().teamNotificationLevel()));
        this.getTrader().sendNetworkMessage(message);
    }

}