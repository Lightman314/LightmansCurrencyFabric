package io.github.lightman314.lightmanscurrency.config.options.custom.values;

import net.minecraft.client.gui.widget.ClickableWidget;

public class ScreenPosition {

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }
    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }

    public ScreenPosition withOffset(ScreenPosition position) { return new ScreenPosition(this.x + position.x, this.y + position.y); }
    public ScreenPosition withOffset(int x, int y) { return new ScreenPosition(this.x + x, this.y + y); }

    public void moveWidget(ClickableWidget widget) { widget.x = this.x; widget.y = this.y; }

}
