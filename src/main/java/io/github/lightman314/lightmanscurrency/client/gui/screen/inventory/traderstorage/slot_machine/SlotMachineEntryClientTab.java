package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryEditWidget;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryClientTab extends TraderStorageClientTab<SlotMachineEntryTab> implements ScrollBarWidget.IScrollable {

    public static final int ENTRY_ROWS = 3;
    public static final int ENTRY_COLUMNS = 2;
    public static final int ENTRIES_PER_PAGE = ENTRY_ROWS * ENTRY_COLUMNS;

    private int scroll = 0;
    private ButtonWidget buttonAddEntry;

    private final List<SlotMachineEntryEditWidget> editWidgets = new ArrayList<>();

    public SlotMachineEntryClientTab(TraderStorageScreen screen, SlotMachineEntryTab commonTab) { super(screen, commonTab); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_TRADER_ALT; }

    @Override
    public MutableText getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.slot_machine.edit_entries"); }

    @Override
    public void onOpen()
    {

        this.editWidgets.clear();

        this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getImageWidth(), 145, this::handleScrollWheel));

        for(int y = 0; y < ENTRY_ROWS; ++y)
        {
            for(int x = 0; x < ENTRY_COLUMNS; x++)
            {
                SlotMachineEntryEditWidget w = this.screen.addRenderableTabWidget(new SlotMachineEntryEditWidget(ScreenPosition.of(this.screen.getGuiLeft(), this.screen.getGuiTop()).offset(19 + (x * SlotMachineEntryEditWidget.WIDTH), 10 + (y * SlotMachineEntryEditWidget.HEIGHT)), this, this.supplierForIndex((y * 2) + x)));
                w.addChildren(this.screen::addRenderableTabWidget);
                this.editWidgets.add(w);
            }
        }

        this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + 19 + (SlotMachineEntryEditWidget.WIDTH * 2), this.screen.getGuiTop() + 10, SlotMachineEntryEditWidget.HEIGHT * ENTRY_ROWS, this));

        this.buttonAddEntry = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 14, this.screen.getGuiTop() + 4, 10, 10, this::AddEntry, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20));

        this.tick();

        //this.menu.SetCoinSlotsActive(false);

    }

    //@Override
    //public void closeAction() { this.menu.SetCoinSlotsActive(true); }

    public SlotMachineEntry getEntry(int entryIndex)
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            List<SlotMachineEntry> entries = trader.getAllEntries();
            if(entryIndex < 0 || entryIndex >= entries.size())
                return null;
            return entries.get(entryIndex);
        }
        return null;
    }

    private Supplier<Integer> supplierForIndex(int index) { return () -> (this.scroll * ENTRY_COLUMNS) + index; }

    @Override
    public void tick() {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            trader.clearEntriesChangedCache();
        this.validateScroll();
        this.buttonAddEntry.visible = this.menu.hasPermission(Permissions.EDIT_TRADES);
        for(SlotMachineEntryEditWidget w : new ArrayList<>(this.editWidgets))
            w.tick();
    }

    private void validateScroll() {
        if(this.scroll > this.getMaxScroll() && this.scroll > 0)
            this.setScroll(Math.max(this.getMaxScroll(), 0));
    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {
        for(SlotMachineEntryEditWidget w : new ArrayList<>(this.editWidgets))
            w.renderTooltips(gui, mouseX, mouseY);
    }

    private void AddEntry(ButtonWidget button) { this.commonTab.AddEntry(); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = MathUtil.clamp(newScroll, 0, this.getMaxScroll()); }

    private int getEntryCount()
    {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            return trader.getAllEntries().size();
        return 0;
    }

    @Override
    public int getMaxScroll() { return ScrollBarWidget.IScrollable.calculateMaxScroll(ENTRIES_PER_PAGE, ENTRY_COLUMNS, this.getEntryCount()); }

}