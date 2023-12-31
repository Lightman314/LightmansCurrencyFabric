package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract class DisplayEntry
{

    public static final DisplayEntry EMPTY = of(Text.empty(), TextRenderUtil.TextFormatting.create());

    private final List<Text> tooltip;

    @Deprecated
    protected DisplayEntry() { this.tooltip = null; }

    protected DisplayEntry (List<Text> tooltip) { this.tooltip = tooltip; }

    protected final TextRenderer getFont() {
        MinecraftClient m = MinecraftClient.getInstance();
        return m.textRenderer;
    }

    protected List<Text> getTooltip() {
        if(this.tooltip == null)
            return new ArrayList<>();
        return this.tooltip;
    }

    public abstract void render(ClickableWidget widget, DrawContext gui, int x, int y, DisplayData area);

    public abstract boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY);

    public static DisplayEntry of(ItemStack item, int count) { return new ItemEntry(item, count, null); }
    public static DisplayEntry of(ItemStack item, int count, List<Text> tooltip) { return new ItemEntry(item, count, tooltip); }
    public static DisplayEntry of(Pair<Identifier,Identifier> background) { return new EmptySlotEntry(background, null); }
    public static DisplayEntry of(Pair<Identifier,Identifier> background, List<Text> tooltip) { return new EmptySlotEntry(background, tooltip); }

    public static DisplayEntry of(Text text, TextRenderUtil.TextFormatting format) { return new TextEntry(text, format, null); }
    public static DisplayEntry of(Text text, TextRenderUtil.TextFormatting format, List<Text> tooltip) { return new TextEntry(text, format, tooltip); }

    public static DisplayEntry of(CoinValue price) { return new PriceEntry(price, null, false); }
    public static DisplayEntry of(CoinValue price, List<Text> additionalTooltips) { return new PriceEntry(price, additionalTooltips, false); }
    public static DisplayEntry of(CoinValue price, List<Text> additionalTooltips, boolean tooltipOverride) { return new PriceEntry(price, additionalTooltips, tooltipOverride); }

    private static class ItemEntry extends DisplayEntry
    {
        private final ItemStack item;

        private ItemEntry(ItemStack item, int count, List<Text> tooltip) { super(tooltip); this.item = item.copy(); this.item.setCount(count);  }

        private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

        @Override
        public void render(ClickableWidget widget, DrawContext gui, int x, int y, DisplayData area) {
            if(this.item.isEmpty())
                return;
            gui.setShaderColor(1f,1f,1f,1f);
            TextRenderer font = this.getFont();
            //Center the x & y positions
            int left = getTopLeft(x + area.xOffset, area.width);
            int top = getTopLeft(y + area.yOffset, area.height);
            ItemRenderUtil.drawItemStack(gui, font, this.item, left, top);
        }

        @Override
        public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
            int left = getTopLeft(x + area.xOffset, area.width);
            int top = getTopLeft(y + area.yOffset, area.height);
            return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
        }
    }

    private static class EmptySlotEntry extends DisplayEntry
    {
        private final Pair<Identifier,Identifier> background;

        private EmptySlotEntry(Pair<Identifier,Identifier> background, List<Text> tooltip) { super(tooltip); this.background = background; }

        private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

        @Override
        public void render(ClickableWidget widget, DrawContext gui, int x, int y, DisplayData area) {
            gui.setShaderColor(1f,1f,1f,1f);
            int left = getTopLeft(x + area.xOffset, area.width);
            int top = getTopLeft(y + area.yOffset, area.height);
            ItemRenderUtil.drawSlotBackground(gui, left, top, this.background);
        }

        @Override
        public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
            int left = getTopLeft(x + area.xOffset, area.width);
            int top = getTopLeft(y + area.yOffset, area.height);
            return mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16;
        }

    }

    private static class TextEntry extends DisplayEntry
    {

        private final Text text;
        private final TextRenderUtil.TextFormatting format;

        private TextEntry(Text text, TextRenderUtil.TextFormatting format, List<Text> tooltip) { super(tooltip); this.text = text; this.format = format; }

        protected int getTextLeft(int x, int availableWidth) {
            if(this.format.centering().isCenter())
                return x + (availableWidth / 2) - (this.getTextWidth() / 2);
            if(this.format.centering().isRight())
                return x + availableWidth - this.getTextWidth();
            return x;
        }

        protected int getTextTop(int y, int availableHeight) {
            if(this.format.centering().isMiddle())
                return y + (availableHeight / 2) - (this.getFont().fontHeight / 2);
            if(this.format.centering().isBottom())
                return y + availableHeight - this.getFont().fontHeight;
            return y;
        }

        protected int getTextWidth() { return this.getFont().getWidth(this.text); }

        @Override
        public void render(ClickableWidget widget, DrawContext gui, int x, int y, DisplayData area) {
            if(this.text.getString().isBlank())
                return;
            gui.setShaderColor(1f,1f,1f,1f);
            TextRenderer font = this.getFont();
            //Define the x position
            int left = this.getTextLeft(x + area.xOffset, area.width);
            //Define the y position
            int top = this.getTextTop(y + area.yOffset, area.height);
            //Draw the text
            gui.drawText(font, this.text, left, top, this.format.color(), true);
        }

        @Override
        public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
            int left = this.getTextLeft(x + area.xOffset, area.width);
            int top = this.getTextTop(y + area.yOffset, area.height);
            return mouseX >= left && mouseX < left + this.getTextWidth() && mouseY >= top && mouseY < top + this.getFont().fontHeight;
        }

    }

    private static class PriceEntry extends DisplayEntry {
        private final CoinValue price;

        public PriceEntry(CoinValue price, List<Text> additionalTooltips, boolean tooltipOverride) {
            super(getTooltip(price, additionalTooltips, tooltipOverride));
            this.price = price;
        }

        private int getTopLeft(int xOrY, int availableWidthOrHeight) { return xOrY + (availableWidthOrHeight / 2) - 8; }

        private static List<Text> getTooltip(CoinValue price, List<Text> additionalTooltips, boolean tooltipOverride) {
            List<Text> tooltips = new ArrayList<>();
            if(tooltipOverride && additionalTooltips != null)
                return additionalTooltips;
            if(!price.isFree() && price.isValid())
                tooltips.add(Text.literal(price.getString()));
            if(additionalTooltips != null)
                tooltips.addAll(additionalTooltips);
            return tooltips;
        }

        @Override
        public void render(ClickableWidget widget, DrawContext gui, int x, int y, DisplayData area) {
            gui.setShaderColor(1f,1f,1f,1f);
            if(this.price.isFree())
            {
                TextRenderer font = this.getFont();
                int left = x + area.xOffset + (area.width / 2) - (font.getWidth(this.price.getString()) / 2);
                int top = y + area.yOffset + (area.height / 2) - (font.fontHeight / 2);
                gui.drawText(font, Text.literal(this.price.getString()), left, top, 0xFFFFFF, false);
            }
            else
            {
                List<CoinValue.CoinValuePair> entries = this.price.getEntries();
                if(entries.size() * 16 <= area.width)
                {
                    List<DisplayData> entryPositions = area.divide(entries.size());
                    for(int i = 0; i < entryPositions.size() && i < entries.size(); ++i)
                    {
                        gui.setShaderColor(1f,1f,1f,1f);
                        DisplayData pos = entryPositions.get(i);
                        int left = this.getTopLeft(x + pos.xOffset, pos.width);
                        int top = this.getTopLeft(y + pos.yOffset, pos.height);
                        ItemStack stack = new ItemStack(entries.get(i).coin);
                        stack.setCount(entries.get(i).amount);
                        ItemRenderUtil.drawItemStack(gui, this.getFont(), stack, left, top);
                    }
                }
                else if(entries.size() > 0)
                {
                    gui.setShaderColor(1f,1f,1f,1f);
                    int spacing = (area.width - 16) / entries.size();
                    int top = this.getTopLeft(y + area.yOffset, area.height);
                    int left = x + area.xOffset + area.width - 16;
                    //Draw cheapest to most expensive
                    for(int i = entries.size() - 1; i >= 0; --i)
                    {
                        ItemStack stack = new ItemStack(entries.get(i).coin);
                        stack.setCount(entries.get(i).amount);
                        ItemRenderUtil.drawItemStack(gui, this.getFont(), stack, left, top);
                        left -= spacing;
                    }
                }
            }

        }

        @Override
        public boolean isMouseOver(int x, int y, DisplayData area, int mouseX, int mouseY) {
            int left = x + area.xOffset;
            int top = y + area.yOffset;
            return mouseX >= left && mouseX < left + area.width && mouseY >= top && mouseY < top + area.height;
        }

    }

}
