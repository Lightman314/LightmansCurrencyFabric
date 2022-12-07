package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class DisplayData
{
    public final int xOffset;
    public final int yOffset;
    public final int width;
    public final int height;
    public DisplayData(int xOffset, int yOffset, int width, int height) { this.xOffset = xOffset; this.yOffset = yOffset; this.width = width; this.height = height; }

    /**
     * Divides the display area horizontally into the given number of pieces.
     * Will always return a list of the length count
     */
    public List<DisplayData> divide(int count)
    {
        if(count <= 1)
            return Lists.newArrayList(this);
        int partialWidth = this.width / count;
        int x = this.xOffset;
        List<DisplayData> result = new ArrayList<>();
        for(int i = 0; i < count; ++i)
        {
            result.add(new DisplayData(x, this.yOffset, partialWidth, this.height));
            x += partialWidth;
        }
        return result;
    }

}
