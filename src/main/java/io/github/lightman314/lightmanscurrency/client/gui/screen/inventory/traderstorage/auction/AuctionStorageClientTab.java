package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener.IScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.auction.AuctionStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class AuctionStorageClientTab extends TraderStorageClientTab<AuctionStorageTab> implements IScrollListener, IScrollable {

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 17;
    private static final int COLUMNS = 10;
    private static final int ROWS = 4;

    public AuctionStorageClientTab(TraderStorageScreen screen, AuctionStorageTab commonTab) { super(screen, commonTab); }

    int scroll = 0;

    ScrollBarWidget scrollBar;

    ButtonWidget buttonCollectItems;

    IconButton buttonCollectMoney;

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_STORAGE; }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.auction.storage"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public void onOpen() {

        this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + (18 * COLUMNS), this.screen.getGuiTop() + Y_OFFSET, ROWS * 18, this));

        this.buttonCollectItems = this.screen.addRenderableTabWidget(IconAndButtonUtil.quickExtractButton(this.screen.getGuiLeft() + 11, this.screen.getGuiTop() + Y_OFFSET + 18 * ROWS + 8, b -> this.commonTab.quickTransfer()));

        this.buttonCollectMoney = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + 25, this.screen.getGuiTop() + 118, b -> this.commonTab.collectCoins(), IconAndButtonUtil.ICON_COLLECT_COINS));

        this.screen.addTabListener(new ScrollListener(this.screen.getGuiLeft(), this.screen.getGuiTop(), this.screen.getImageWidth(), 118, this));

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        gui.drawText(this.font, Text.translatable("tooltip.lightmanscurrency.auction.storage"), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);

        this.scrollBar.beforeWidgetRender(mouseY);

        if(this.menu.getTrader() instanceof AuctionHouseTrader)
        {
            //Validate the scroll
            this.validateScroll();
            //Render each display slot
            int index = this.scroll * COLUMNS;
            AuctionPlayerStorage storage = ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player);
            if(storage != null)
            {
                List<ItemStack> storedItems = storage.getStoredItems();
                int hoverSlot = this.isMouseOverSlot(mouseX, mouseY) + (this.scroll * COLUMNS);
                for(int y = 0; y < ROWS && index < storedItems.size(); ++y)
                {
                    int yPos = this.screen.getGuiTop() + Y_OFFSET + y * 18;
                    for(int x = 0; x < COLUMNS && index < storedItems.size(); ++x)
                    {
                        //Get the slot position
                        int xPos = this.screen.getGuiLeft() + X_OFFSET + x * 18;
                        //Render the slot background
                        gui.setShaderColor(1f, 1f, 1f, 1f);
                        gui.drawTexture(TraderScreen.GUI_TEXTURE, xPos, yPos, TraderScreen.WIDTH, 0, 18, 18);
                        //Render the slots item
                        if(index < storedItems.size())
                            ItemRenderUtil.drawItemStack(gui, this.font, storedItems.get(index), xPos + 1, yPos + 1);
                        if(index == hoverSlot)
                            HandledScreen.drawSlotHighlight(gui, xPos + 1, yPos + 1, 0);
                        index++;
                    }
                }

                if(storedItems.size() == 0)
                    TextRenderUtil.drawCenteredMultilineText(gui, Text.translatable("tooltip.lightmanscurrency.auction.storage.items.none"), this.screen.getGuiLeft() + 10, this.screen.getImageWidth() - 20, this.screen.getGuiTop() + X_OFFSET + (18 * ROWS / 2), 0x404040);

                this.buttonCollectItems.active = storedItems.size() > 0;

                //Render the stored money amount
                if(storage.getStoredCoins().hasAny())
                {
                    this.buttonCollectMoney.active = true;
                    gui.drawText(this.font, Text.translatable("tooltip.lightmanscurrency.auction.storage.money", storage.getStoredCoins().getString("0")), this.screen.getGuiLeft() + 50, this.screen.getGuiTop() + 118, 0x404040, false);
                }
                else
                {
                    this.buttonCollectMoney.active = false;
                    gui.drawText(this.font, Text.translatable("tooltip.lightmanscurrency.auction.storage.money.none"), this.screen.getGuiLeft() + 50, this.screen.getGuiTop() + 118, 0x404040, false);
                }

            }

        }

    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        if(this.menu.getTrader() instanceof AuctionHouseTrader && this.menu.getCursorStack().isEmpty())
        {
            int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
            if(hoveredSlot >= 0)
            {
                hoveredSlot += scroll * COLUMNS;
                AuctionPlayerStorage storage = ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player);
                if(hoveredSlot < storage.getStoredItems().size())
                {
                    ItemStack stack = storage.getStoredItems().get(hoveredSlot);
                    gui.drawTooltip(this.font, ItemRenderUtil.getTooltipFromItem(stack), mouseX, mouseY);
                }
            }
        }
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
        if(this.menu.getTrader() instanceof AuctionHouseTrader)
            return ((AuctionHouseTrader)this.menu.getTrader()).getStorage(this.menu.player).getStoredItems().size();
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

        if(this.menu.getTrader() instanceof AuctionHouseTrader)
        {
            int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
            if(hoveredSlot >= 0)
            {
                hoveredSlot += this.scroll * COLUMNS;
                this.commonTab.clickedOnSlot(hoveredSlot, Screen.hasShiftDown());
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

}