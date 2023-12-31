package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine.SlotMachineStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SlotMachineStorageClientTab extends TraderStorageClientTab<SlotMachineStorageTab> implements ScrollBarWidget.IScrollable {

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 17;
    private static final int COLUMNS_NORMAL = 8;
    private static final int COLUMNS_PERSISTENT = 10;
    private static final int ROWS = 5;

    public SlotMachineStorageClientTab(TraderStorageScreen screen, SlotMachineStorageTab commonTab) { super(screen, commonTab); }

    int scroll = 0;

    ScrollBarWidget scrollBar;

    int columns = COLUMNS_NORMAL;

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }

    @Override
    public MutableText getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.storage"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public void onOpen() {

        this.columns = COLUMNS_NORMAL;
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader && trader.isPersistent())
            this.columns = COLUMNS_PERSISTENT;

        this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * this.columns), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));

        this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getImageWidth(), 118, this::handleScrollWheel));

        if(this.menu.getTrader() instanceof SlotMachineTraderData trader && !trader.isPersistent())
        {
            this.screen.addTabListener(IconAndButtonUtil.quickInsertButton(this.screen.getGuiLeft() + 22, this.screen.getGuiTop() + Y_OFFSET + (18 * ROWS) + 8, b -> this.commonTab.quickTransfer(0)));
            this.screen.addTabListener(IconAndButtonUtil.quickExtractButton(this.screen.getGuiLeft() + 34, this.screen.getGuiTop() + Y_OFFSET + (18 * ROWS) + 8, b -> this.commonTab.quickTransfer(1)));
        }

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        gui.drawText(this.font, EasyText.translatable("gui.lightmanscurrency.storage"), 8, 6, 0x404040, false);

        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            //Validate the scroll
            this.validateScroll();
            //Render each display slot
            int index = this.scroll * this.columns;
            TraderItemStorage storage = trader.getStorage();
            int hoverSlot = this.isMouseOverSlot(ScreenPosition.of(mouseX, mouseY)) + (this.scroll * this.columns);
            for(int y = 0; y < ROWS; ++y)
            {
                int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
                for(int x = 0; x < this.columns; ++x)
                {
                    //Get the slot position
                    int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
                    //Render the slot background
                    gui.setShaderColor(1f,1f,1f,1f);
                    gui.drawTexture(TraderScreen.GUI_TEXTURE, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
                    //Render the slots item
                    if(index < storage.getSlotCount())
                        ItemRenderUtil.drawItemStack(gui, this.font, storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
                    if(index == hoverSlot)
                        MenuScreen.drawSlotHighlight(gui, xPos + 1, yPos + 1, 0);
                    index++;
                }
            }

            //Render the slot bg for the upgrade slots
            gui.setShaderColor(1f,1f,1f,1f);
            for(Slot slot : this.commonTab.getSlots())
                gui.drawTexture(TraderScreen.GUI_TEXTURE, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
        }

    }

    private void validateScroll() {
        if(this.scroll > 0 && this.scroll > this.getMaxScroll())
            this.setScroll(Math.max(this.getMaxScroll(), 0));
    }

    private String getCountText(ItemStack stack) {
        int count = stack.getCount();
        if(count <= 1)
            return null;
        if(count >= 1000)
        {
            String countText = String.valueOf(count / 1000);
            if((count % 1000) / 100 > 0)
                countText += "." + ((count % 1000) / 100);
            return countText + "k";
        }
        return String.valueOf(count);
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY)
    {

        if(this.menu.getTrader() instanceof SlotMachineTraderData trader && this.screen.getScreenHandler().getCursorStack().isEmpty())
        {
            int hoveredSlot = this.isMouseOverSlot(ScreenPosition.of(mouseX,mouseY));
            if(hoveredSlot >= 0)
            {
                hoveredSlot += scroll * this.columns;
                TraderItemStorage storage = trader.getStorage();
                if(hoveredSlot < storage.getContents().size())
                {
                    ItemStack stack = storage.getContents().get(hoveredSlot);
                    List<Text> tooltip = ItemRenderUtil.getTooltipFromItem(stack);
                    tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.itemstorage", stack.getCount()));
                    if(stack.getCount() >= 64)
                    {
                        if(stack.getCount() % 64 == 0)
                            tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.itemstorage.stacks.single", stack.getCount() / 64));
                        else
                            tooltip.add(EasyText.translatable("tooltip.lightmanscurrency.itemstorage.stacks.multi", stack.getCount() / 64, stack.getCount() % 64));
                    }
                    gui.drawTooltip(this.font, tooltip, mouseX, mouseY);
                }
            }
        }
    }

    private int isMouseOverSlot(ScreenPosition mousePos) {

        int foundColumn = -1;
        int foundRow = -1;

        int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
        int topEdge = this.screen.getGuiTop() + Y_OFFSET;
        for(int x = 0; x < this.columns && foundColumn < 0; ++x)
        {
            if(mousePos.x >= leftEdge + x * 18 && mousePos.x < leftEdge + (x * 18) + 18)
                foundColumn = x;
        }
        for(int y = 0; y < ROWS && foundRow < 0; ++y)
        {
            if(mousePos.y >= topEdge + y * 18 && mousePos.y < topEdge + (y * 18) + 18)
                foundRow = y;
        }
        if(foundColumn < 0 || foundRow < 0)
            return -1;
        return (foundRow * this.columns) + foundColumn;
    }

    private int totalStorageSlots() {
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            return trader.getStorage().getContents().size();
        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        this.scrollBar.onMouseClicked(mouseX, mouseY, button);

        if(this.menu.getTrader() instanceof SlotMachineTraderData)
        {
            int hoveredSlot = this.isMouseOverSlot(ScreenPosition.of((int)mouseX, (int)mouseY));
            if(hoveredSlot >= 0)
            {
                hoveredSlot += this.scroll * this.columns;
                this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown(), button == 0);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrollBar.onMouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        this.validateScroll();
    }

    @Override
    public int getMaxScroll() {
        return Math.max(((this.totalStorageSlots() - 1) / this.columns) - ROWS + 1, 0);
    }

}