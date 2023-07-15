package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MenuScreen;
import net.minecraft.client.gui.widget.ClickableWidget;

public final class ScreenPosition {

    public static final ScreenPosition ZERO = of(0,0);

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }

    public ScreenPosition offset(int x, int y) { return of(this.x + x, this.y + y); }
    public ScreenPosition offset(ScreenPosition other) { return of(this.x + other.x, this.y + other.y); }
    public ScreenPosition offset(ClickableWidget widget) { return of(this.x + widget.getX(), this.y + widget.getY()); }
    public ScreenPosition offset(MenuScreen<?> screen) { return this.offset(getScreenCorner(screen)); }

    public void setPosition(ClickableWidget widget) { widget.setPosition(this.x,this.y); }

    public boolean isMouseInArea(int mouseX, int mouseY, int width, int height) { return ScreenArea.of(this, width, height).isMouseInArea(mouseX, mouseY); }


    @Override
    public String toString() { return this.x + ", " + this.y; }

    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }
    //public static LazyOptional<ScreenPosition> ofOptional(int x, int y) { return LazyOptional.of(() -> of(x, y)); }
    public static ScreenPosition getScreenCorner(MenuScreen<?> screen) { return of(screen.getGuiLeft(), screen.getGuiTop()); }

}