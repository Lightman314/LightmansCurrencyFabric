package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ScrollTextDisplay extends ClickableWidget {

    private TextRenderer font;
    private final Supplier<List<? extends Text>> textSource;
    public boolean invertText = false;
    public int backgroundColor = 0xFF000000;
    public int textColor = 0xFFFFFF;
    private int columnCount = 1;
    public int getColumnCount() { return this.columnCount; }
    public void setColumnCount(int columnCount) { this.columnCount = MathUtil.clamp(columnCount, 1, Integer.MAX_VALUE); }

    public ScrollTextDisplay(int x, int y, int width, int height, TextRenderer font, Supplier<List<? extends Text>> textSource)
    {
        super(x, y, width, height, Text.empty());

        this.font = font;
        this.textSource = textSource;
    }

    private int scroll = 0;

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {

        //Render the background
        gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.backgroundColor);

        //Start rendering the text
        List<? extends Text> text = this.textSource.get();

        this.validateScroll(text.size());
        int i = this.getStartingIndex(text.size());
        int columnWidth = this.getColumnWidth();
        int bottom = this.getY() + this.height;
        for(int yPos = this.getY() + 2; yPos < bottom && i >= 0 && i < text.size();)
        {
            int rowHeight = 0;
            for(int col = 0; col < this.columnCount && i >= 0 && i < text.size(); ++col)
            {
                int xPos = this.getXPos(col);
                Text thisText = text.get(i);
                int thisHeight = this.font.getWrappedLinesHeight(thisText.getString(), columnWidth);
                if(yPos + thisHeight < bottom)
                {
                    gui.drawTextWrapped(this.font, thisText, xPos, yPos, columnWidth, this.textColor);
                }
                if(thisHeight > rowHeight)
                    rowHeight = thisHeight;
                //Increment the text index
                i = this.invertText ? i - 1 : i + 1;
            }
            yPos += rowHeight;
        }

        gui.setShaderColor(1f,1f,1f,1f);

    }

    private void validateScroll(int listSize)
    {
        if(this.scroll * columnCount >= listSize)
            this.scroll = MathUtil.clamp(this.scroll, 0, (listSize / columnCount - 1));
    }

    private int getStartingIndex(int listSize)
    {
        return this.invertText ? listSize - 1 - (this.scroll * this.columnCount) : this.scroll * this.columnCount;
    }

    private int getColumnWidth()
    {
        return ((this.width - 4) / this.columnCount);
    }

    private int getXPos(int column)
    {
        int columnSpacing = this.width / this.columnCount;
        return this.getX() + 2 + column * columnSpacing;
    }

    private boolean canScrollDown()
    {
        return this.scroll < this.textSource.get().size();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if(!this.visible)
            return false;

        if(delta < 0)
        {
            if(this.canScrollDown())
                scroll++;
            else
                return false;
        }
        else if(delta > 0)
        {
            if(scroll > 0)
                scroll--;
            else
                return false;
        }

        return true;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) { }

}