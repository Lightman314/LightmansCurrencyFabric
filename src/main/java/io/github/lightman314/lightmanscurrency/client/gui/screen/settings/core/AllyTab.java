package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.core;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class AllyTab extends SettingsTab {

    public static final AllyTab INSTANCE = new AllyTab();

    private AllyTab() { }

    @Override
    public int getColor() {
        return 0xFFFFFFFF;
    }

    TextFieldWidget nameInput;
    ButtonWidget buttonAddAlly;
    ButtonWidget buttonRemoveAlly;

    ScrollTextDisplay display;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.settings.ally"); }

    @Override
    public boolean canOpen() { return this.hasPermissions(Permissions.ADD_REMOVE_ALLIES); }

    @Override
    public void initTab() {

        TraderSettingsScreen screen = this.getScreen();

        this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 10, 160, 20, Text.empty()));
        this.nameInput.setMaxLength(16);

        this.buttonAddAlly = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 35, 74, 20, Text.translatable("gui.button.lightmanscurrency.allies.add"), this::AddAlly));
        this.buttonRemoveAlly = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 35, 74, 20, Text.translatable("gui.button.lightmanscurrency.allies.remove"), this::RemoveAlly));

        this.display = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 5, screen.guiTop() + 60, 190, 135, screen.getFont(), this::getAllyList));
        this.display.setColumnCount(2);

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    private List<Text> getAllyList()
    {
        List<Text> list = Lists.newArrayList();
        this.getScreen().getTrader().getAllies().forEach(ally -> list.add(ally.getNameComponent(true)));
        return list;
    }

    @Override
    public void tick()
    {
        this.nameInput.tick();
        this.buttonAddAlly.active = this.buttonRemoveAlly.active = !this.nameInput.getText().isEmpty();
    }

    @Override
    public void closeTab() { }

    private void AddAlly(ButtonWidget button)
    {
        String allyName = this.nameInput.getText();
        NbtCompound message = new NbtCompound();
        message.putString("AddAlly", allyName);
        this.sendNetworkMessage(message);
        this.nameInput.setText("");
    }

    private void RemoveAlly(ButtonWidget button)
    {
        String allyName = this.nameInput.getText();
        NbtCompound message = new NbtCompound();
        message.putString("RemoveAlly", allyName);
        this.sendNetworkMessage(message);
        this.nameInput.setText("");
    }

}