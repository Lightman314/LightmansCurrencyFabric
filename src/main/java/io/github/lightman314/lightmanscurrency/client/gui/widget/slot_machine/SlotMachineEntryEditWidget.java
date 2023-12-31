package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine.SlotMachineEntryClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SlotMachineEntryEditWidget extends ClickableWidget {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 46;

    public final SlotMachineEntryClientTab tab;
    public final Supplier<Integer> entryIndex;

    private TextFieldWidget weightEdit;
    private PlainButton removeEntryButton;

    private int previousIndex = -1;

    private static final int ITEM_POSY = 22;

    public SlotMachineEntryEditWidget(ScreenPosition pos, SlotMachineEntryClientTab tab, Supplier<Integer> entryIndex) { this(pos.x, pos.y, tab, entryIndex); }
    public SlotMachineEntryEditWidget(int x, int y, SlotMachineEntryClientTab tab, Supplier<Integer> entryIndex) {
        super(x, y, WIDTH, HEIGHT, EasyText.empty());
        this.tab = tab;
        this.entryIndex = entryIndex;
    }

    public void addChildren(Consumer<ClickableWidget> consumer) {
        this.weightEdit = new TextFieldWidget(this.tab.font, this.getX() + this.tab.font.getWidth(EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.weight_label")), this.getY() + 10, 36, 10, EasyText.empty());
        consumer.accept(this.weightEdit);
        this.weightEdit.setMaxLength(4);
        this.removeEntryButton = new PlainButton(this.getX(), this.getY(), 10, 10, this::Remove, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20);
        consumer.accept(this.removeEntryButton);
    }

    private SlotMachineEntry getEntry() { return this.tab.getEntry(this.entryIndex.get()); }

    private void Remove(ButtonWidget button) { this.tab.commonTab.RemoveEntry(this.entryIndex.get()); }


    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            //Draw label
            context.drawText(this.tab.font, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.entry_label", this.entryIndex.get() + 1), this.getX() + 12, this.getY(), 0x404040, false);
            //Draw Weight label
            context.drawText(this.tab.font, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.weight_label"), this.getX(), this.getY() + 12, 0x404040, false);
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                {
                    ItemRenderUtil.drawItemStack(context, this.tab.font, entry.items.get(i), this.getX() + (18 * i), this.getY() + ITEM_POSY);
                }
                else
                {
                    ItemRenderUtil.drawSlotBackground(context, this.getX() + (18 * i), this.getY() + ITEM_POSY, ItemTradeRestriction.BACKGROUND);
                }

            }
        }
    }

    @Override
    protected boolean isValidClickButton(int button) { return button == 0 || button == 1; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //Confirm that the mouse is in the general area
        if(this.clicked(mouseX, mouseY) && this.isValidClickButton(button))
        {
            boolean rightClick = button == 1;
            SlotMachineEntry entry = this.getEntry();
            if(entry != null)
            {
                int entryIndex = this.entryIndex.get();
                ItemStack heldItem = this.tab.menu.getCursorStack();
                if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
                {
                    int itemIndex = getItemSlotIndex(mouseX);
                    if(itemIndex >= 0)
                    {
                        if(itemIndex >= entry.items.size())
                        {
                            if(!heldItem.isEmpty())
                            {
                                if(rightClick) //If right-click, set as 1
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem.copyWithCount(1));
                                else //Otherwise add whole stack
                                    this.tab.commonTab.AddEntryItem(entryIndex, heldItem);
                                return true;
                            }
                        }
                        else if(heldItem.isEmpty())
                        {
                            if(rightClick) //If right-click, reduce by 1
                            {
                                ItemStack newStack = entry.items.get(itemIndex).copy();
                                newStack.decrement(1);
                                if(newStack.isEmpty())
                                    this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                                else
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, newStack);
                            }
                            else //If left-click, remove entirely
                                this.tab.commonTab.RemoveEntryItem(entryIndex, itemIndex);
                            return true;
                        }
                        else {
                            if(rightClick) //If right-click, either set as 1 or increase by 1
                            {
                                ItemStack oldStack = entry.items.get(itemIndex);
                                if(InventoryUtil.ItemMatches(heldItem, oldStack))
                                {
                                    ItemStack newStack = entry.items.get(itemIndex).copy();
                                    if(newStack.getCount() >= newStack.getMaxCount())
                                        return false;
                                    newStack.increment(1);
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, newStack);
                                }
                                else
                                    this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, heldItem.copyWithCount(1));
                                return true;
                            }
                            else //Replace with new held item
                                this.tab.commonTab.EditEntryItem(entryIndex, itemIndex, heldItem);
                        }
                    }
                }
            }
        }
        return false;
    }

    private int getItemSlotIndex(double mouseX)
    {
        int x = (int)mouseX - this.getX();
        if(x < 0)
            return -1;
        int result = x / 18;
        return result >= SlotMachineEntry.ITEM_LIMIT ? -1 : result;
    }

    public void tick()
    {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null && this.tab.menu.getTrader() instanceof SlotMachineTraderData trader)
        {
            this.weightEdit.visible = true;
            boolean hasPerms = this.tab.menu.hasPermission(Permissions.EDIT_TRADES);
            this.removeEntryButton.visible = hasPerms;
            this.weightEdit.setEditable(hasPerms);
            if(trader.areEntriesChanged())
            {
                this.weightEdit.setText(Integer.toString(entry.getWeight()));
                return;
            }

            int thisIndex = this.entryIndex.get();
            if(thisIndex != this.previousIndex)
                this.weightEdit.setText(Integer.toString(entry.getWeight()));
            int newWeight = TextInputUtil.getIntegerValue(this.weightEdit, 1);
            if(newWeight != entry.getWeight())
                this.tab.commonTab.ChangeEntryWeight(thisIndex, newWeight);
            this.previousIndex = thisIndex;
        }
        else
            this.weightEdit.visible = this.removeEntryButton.visible = false;

        TextInputUtil.whitelistInteger(this.weightEdit, 1, 1000);
    }

    public void renderTooltips(DrawContext context, int mouseX, int mouseY)
    {
        List<Text> tooltips = this.getTooltipText(mouseX, mouseY);
        if(tooltips != null)
            context.drawTooltip(this.tab.font, tooltips, mouseX, mouseY);
    }


    public List<Text> getTooltipText(int mouseX, int mouseY) {
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            if(mouseY >= this.getY() + ITEM_POSY && mouseY < this.getY() + ITEM_POSY + 16)
            {
                int itemIndex = this.getItemSlotIndex(mouseX);
                if(itemIndex >= 0 && itemIndex < entry.items.size())
                {
                    ItemStack item = entry.items.get(itemIndex);
                    if(!item.isEmpty())
                        return ItemRenderUtil.getTooltipFromItem(item);
                }
            }
        }
        return null;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

}