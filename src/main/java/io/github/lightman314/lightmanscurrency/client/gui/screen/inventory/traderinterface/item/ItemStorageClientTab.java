package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.item;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.item.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.ConfigurableSidedHandler.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class ItemStorageClientTab extends TraderInterfaceClientTab<ItemStorageTab> implements IScrollListener, IScrollable{

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 17;
    private static final int COLUMNS = 8;
    private static final int ROWS = 2;

    private static final int WIDGET_OFFSET = Y_OFFSET + 18 * ROWS + 4;

    DirectionalSettingsWidget inputSettings;
    DirectionalSettingsWidget outputSettings;

    public ItemStorageClientTab(TraderInterfaceScreen screen, ItemStorageTab commonTab) { super(screen, commonTab); }

    int scroll = 0;

    ScrollBarWidget scrollBar;

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.interface.storage"); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    private DirectionalSettings getInputSettings() {
        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity itemInterface)
            return itemInterface.getItemHandler().getInputSides();
        return new DirectionalSettings();
    }

    private DirectionalSettings getOutputSettings() {
        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity itemInterface)
            return itemInterface.getItemHandler().getOutputSides();
        return new DirectionalSettings();
    }

    @Override
    public void onOpen() {

        this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * COLUMNS), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));

        this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getImageWidth(), 118, this));

        this.inputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this.getInputSettings()::get, this.getInputSettings().ignoreSides, this::ToggleInputSide, this.screen::addRenderableTabWidget);
        this.outputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 116, this.screen.getGuiTop() + WIDGET_OFFSET + 9, this.getOutputSettings()::get, this.getInputSettings().ignoreSides,  this::ToggleOutputSide, this.screen::addRenderableTabWidget);

        this.screen.addRenderableTabWidget(IconAndButtonUtil.quickInsertButton(this.screen.getGuiLeft() + 22, this.screen.getGuiTop() + Y_OFFSET + 18 * 5 + 8, b -> this.commonTab.quickTransfer(0)));
        this.screen.addRenderableTabWidget(IconAndButtonUtil.quickExtractButton(this.screen.getGuiLeft() + 34, this.screen.getGuiTop() + Y_OFFSET + 18 * 5 + 8, b -> this.commonTab.quickTransfer(1)));

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        gui.drawText(this.font, Text.translatable("tooltip.lightmanscurrency.interface.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);

        this.scrollBar.beforeWidgetRender(mouseY);

        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity traderInterface)
        {
            //Validate the scroll
            this.validateScroll();
            //Render each display slot
            int index = this.scroll * COLUMNS;
            TraderItemStorage storage = traderInterface.getItemBuffer();
            int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY) + (this.scroll * COLUMNS);
            for(int y = 0; y < ROWS; ++y)
            {
                int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
                for(int x = 0; x < COLUMNS; ++x)
                {
                    //Get the slot position
                    int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
                    //Render the slot background
                    gui.setShaderColor(1f, 1f, 1f, 1f);
                    gui.drawTexture(TraderInterfaceScreen.GUI_TEXTURE, xPos, yPos, TraderInterfaceScreen.WIDTH, 0, 18, 18);
                    //Render the slots item
                    if(index < storage.getSlotCount())
                        ItemRenderUtil.drawItemStack(gui, this.font, storage.getContents().get(index), xPos + 1, yPos + 1, this.getCountText(storage.getContents().get(index)));
                    if(index == hoveredSlot)
                        HandledScreen.drawSlotHighlight(gui, xPos + 1, yPos + 1, 0);
                    index++;
                }
            }

            //Render the slot bg for the upgrade slots
            gui.setShaderColor(1f, 1f, 1f, 1f);
            for(Slot slot : this.commonTab.getSlots())
            {
                gui.drawTexture(TraderInterfaceScreen.GUI_TEXTURE, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderInterfaceScreen.WIDTH, 0, 18, 18);
            }

            //Render the input/output labels
            gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.settings.iteminput.side"), this.screen.getGuiLeft() + 33, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040, false);
            int textWidth = this.font.getWidth(Text.translatable("gui.lightmanscurrency.settings.itemoutput.side"));
            gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.settings.itemoutput.side"), this.screen.getGuiLeft() + 173 - textWidth, this.screen.getGuiTop() + WIDGET_OFFSET, 0x404040, false);
        }

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
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity itemInterface)
        {
            if(this.menu.getCursorStack().isEmpty())
            {
                int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
                if(hoveredSlot >= 0)
                {
                    hoveredSlot += scroll * COLUMNS;
                    TraderItemStorage storage = itemInterface.getItemBuffer();
                    if(hoveredSlot < storage.getContents().size())
                    {
                        ItemStack stack = storage.getContents().get(hoveredSlot);
                        List<Text> tooltip = ItemRenderUtil.getTooltipFromItem(stack);
                        tooltip.add(Text.translatable("tooltip.lightmanscurrency.itemstorage", stack.getCount()));
                        if(stack.getCount() >= 64)
                        {
                            if(stack.getCount() % 64 == 0)
                                tooltip.add(Text.translatable("tooltip.lightmanscurrency.itemstorage.stacks.single", stack.getCount() / 64));
                            else
                                tooltip.add(Text.translatable("tooltip.lightmanscurrency.itemstorage.stacks.multi", stack.getCount() / 64, stack.getCount() % 64));
                        }
                        gui.drawTooltip(this.font, tooltip, mouseX, mouseY);
                    }
                }
            }

            this.inputSettings.renderTooltips(gui, mouseX, mouseY, this.font);
            this.outputSettings.renderTooltips(gui, mouseX, mouseY, this.font);

        }
    }

    @Override
    public void tick() {
        this.inputSettings.tick();
        this.outputSettings.tick();
    }

    private void validateScroll() {
        if(this.scroll < 0)
            this.scroll = 0;
        if(this.scroll > this.getMaxScroll())
            this.scroll = this.getMaxScroll();
    }

    private int isMouseOverSlot(double mouseX, double mouseY) {

        int foundColumn = -1;
        int foundRow = -1;

        int leftEdge = this.screen.getGuiLeft() + X_OFFSET;
        int topEdge = this.screen.getGuiTop() + Y_OFFSET;
        for(int x = 0; x < COLUMNS && foundColumn < 0; ++x)
        {
            if(mouseX >= leftEdge + x * 18 && mouseX < leftEdge + (x * 18) + 18)
                foundColumn = x;
        }
        for(int y = 0; y < ROWS && foundRow < 0; ++y)
        {
            if(mouseY >= topEdge + y * 18 && mouseY < topEdge + (y * 18) + 18)
                foundRow = y;
        }
        if(foundColumn < 0 || foundRow < 0)
            return -1;
        return (foundRow * COLUMNS) + foundColumn;
    }

    private int totalStorageSlots() {
        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity itemInterface)
        {
            return itemInterface.getItemBuffer().getContents().size();
        }
        return 0;
    }

    private boolean canScrollDown() {
        return this.totalStorageSlots() - this.scroll * COLUMNS > ROWS * COLUMNS;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(delta < 0)
        {
            if(this.canScrollDown())
                this.scroll++;
            else
                return false;
        }
        else if(delta > 0)
        {
            if(this.scroll > 0)
                scroll--;
            else
                return false;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if(this.menu.getTraderInterface() instanceof ItemTraderInterfaceBlockEntity)
        {
            int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
            if(hoveredSlot >= 0)
            {
                hoveredSlot += this.scroll * COLUMNS;
                this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown(), button == 0);
                return true;
            }
        }
        this.scrollBar.onMouseClicked(mouseX, mouseY, button);
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
        return Math.max(((this.totalStorageSlots() - 1) / COLUMNS) - ROWS + 1, 0);
    }

    private void ToggleInputSide(Direction side) {
        this.commonTab.toggleInputSlot(side);
    }

    private void ToggleOutputSide(Direction side) {
        this.commonTab.toggleOutputSlot(side);
    }

}