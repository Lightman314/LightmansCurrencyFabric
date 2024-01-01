package io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SlotMachineEntryDisplayWidget extends ClickableWidget {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 46;

    public final Supplier<SlotMachineTraderData> trader;
    public final Supplier<Integer> index;

    private final TextRenderer font;

    private static final int ITEM_POSY = 22;

    public SlotMachineEntryDisplayWidget(ScreenPosition pos, Supplier<SlotMachineTraderData> trader, Supplier<Integer> index, TextRenderer font) { this(pos.x, pos.y, trader, index, font); }
    public SlotMachineEntryDisplayWidget(int x, int y, Supplier<SlotMachineTraderData> trader, Supplier<Integer> index, TextRenderer font) {
        super(x, y, WIDTH, HEIGHT, EasyText.empty());
        this.trader = trader;
        this.index = index;
        this.font = font;
    }

    @Nullable
    private SlotMachineEntry getEntry() {
        SlotMachineTraderData trader = this.trader.get();
        if(trader != null)
        {
            int index = this.index.get();
            List<SlotMachineEntry> entries = trader.getValidEntries();
            if(index >= 0 && index < entries.size())
                return entries.get(index);
        }
        return null;
    }



    @Override
    public void renderButton(MatrixStack pose, int mouseX, int mouseY, float delta) {
        SlotMachineEntry entry = this.getEntry();
        SlotMachineTraderData trader = this.trader.get();
        if(trader != null && entry != null)
        {
            //Draw label
            this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.entry_label", this.index.get() + 1), this.x , this.y, 0x404040);
            //Draw Weight label
            this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.trader.slot_machine.odds_label", trader.getOdds(entry.getWeight())), this.x, this.y + 12, 0x404040);
            //Render Items
            for(int i = 0; i < SlotMachineEntry.ITEM_LIMIT; ++i)
            {
                if(i < entry.items.size() && !entry.items.get(i).isEmpty())
                    ItemRenderUtil.drawItemStack(this, this.font, entry.items.get(i), this.x + (18 * i), this.y + ITEM_POSY);
            }
        }
    }

    private int getItemSlotIndex(double mouseX)
    {
        int x = (int)mouseX - this.x;
        if(x < 0)
            return -1;
        int result = x / 18;
        return result >= SlotMachineEntry.ITEM_LIMIT ? -1 : result;
    }

    public void renderTooltips(MatrixStack pose, int mouseX, int mouseY)
    {
        List<Text> tooltips = this.getTooltipText(mouseX, mouseY);
        if(tooltips != null) {
            assert MinecraftClient.getInstance().currentScreen != null;
            MinecraftClient.getInstance().currentScreen.renderTooltip(pose, tooltips, mouseX, mouseY);
        }
    }

    public List<Text> getTooltipText(int mouseX, int mouseY) {
        if(!this.visible)
            return null;
        SlotMachineEntry entry = this.getEntry();
        if(entry != null)
        {
            if(mouseY >= this.y + ITEM_POSY && mouseY < this.y + ITEM_POSY + 16)
            {
                int itemIndex = this.getItemSlotIndex(mouseX);
                if(itemIndex >= 0 && itemIndex < entry.items.size())
                {
                    if(entry.isMoney())
                        return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.money", entry.getMoneyValue().getComponent("0")));
                    else
                    {
                        ItemStack item = entry.items.get(itemIndex);
                        if(!item.isEmpty()) {
                            assert MinecraftClient.getInstance().currentScreen != null;
                            return MinecraftClient.getInstance().currentScreen.getTooltipFromItem(item);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) { }

}