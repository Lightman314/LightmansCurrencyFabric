package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption.OptionWidgets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PermissionsTab extends SettingsTab {

    public static PermissionsTab INSTANCE = new PermissionsTab();

    private PermissionsTab() {}

    List<OptionWidgets> widgets = Lists.newArrayList();
    List<PermissionOption> options;

    protected int startHeight() { return 5; }
    private int calculateStartHeight()
    {
        return this.getScreen().guiTop() + this.startHeight();
    }

    @Override
    public int getColor() { return 0xFFFFFF; }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.BOOKSHELF); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.settings.allyperms"); }

    @Override
    public boolean canOpen() { return this.hasPermissions(Permissions.EDIT_PERMISSIONS); }

    @Override
    public void initTab() {
        this.options = new ArrayList<>();
        this.options.addAll(this.getScreen().getTrader().getPermissionOptions());
        int startHeight = this.calculateStartHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            int xPos = this.getXPos(i);
            int yPos = this.getYPosOffset(i) + startHeight;
            PermissionOption option = this.options.get(i);
            OptionWidgets optionWidgets = option.initWidgets(this.getScreen(), xPos, yPos);
            optionWidgets.getRenderableWidgets().forEach(widget -> this.getScreen().addRenderableTabWidget(widget));
            optionWidgets.getListeners().forEach(listener -> this.getScreen().addTabListener(listener));
            this.widgets.add(optionWidgets);
        }
    }

    private int getYPosOffset(int index)
    {
        int yIndex = index / 2;
        return 20 * yIndex;
    }

    private int getXPos(int index)
    {
        return this.getScreen().guiLeft() + (index % 2 == 0 ? 5 : 105);
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        int startHeight = this.calculateStartHeight();
        for(int i = 0; i < this.options.size(); ++i)
        {
            PermissionOption option = this.options.get(i);
            int xPos = this.getXPos(i) + option.widgetWidth();
            int yPos = this.getYPosOffset(i) + startHeight;
            int textWidth = 90 - option.widgetWidth();
            int textHeight = this.getFont().getWrappedLinesHeight(option.widgetName().getString(), textWidth);
            int yStart = ((20 - textHeight) / 2) + yPos;
            TextRenderUtil.drawVerticallyCenteredMultilineText(gui, option.widgetName(), xPos, textWidth, yStart, textHeight, 0xFFFFFF);
        }
    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() {
        for (PermissionOption option : this.options) {
            option.tick();
        }
    }

    @Override
    public void closeTab() {

    }

}