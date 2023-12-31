package io.github.lightman314.lightmanscurrency.client.gui.widget.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.widget.ClickableWidget;

public class LazyWidgetPositioner {

    public static final Function<LazyWidgetPositioner,Pair<Integer,Integer>> MODE_TOPDOWN = positioner -> {
        return Pair.of(positioner.startX(), positioner.startY() + (positioner.widgetSize * positioner.getPositionIndex()));
    };

    public static final Function<LazyWidgetPositioner,Pair<Integer,Integer>> MODE_BOTTOMUP = positioner -> {
        return Pair.of(positioner.startX(), positioner.startY() - (positioner.widgetSize * positioner.getPositionIndex()));
    };

    private final IScreen screen;
    private final Function<LazyWidgetPositioner,Pair<Integer,Integer>> mode;
    private final List<ClickableWidget> widgetList = new ArrayList<>();

    private final int x1;
    private final int y1;
    public final int widgetSize;
    public final int startX() { return this.screen.getGuiLeft() + this.x1; }
    public final int startY() { return this.screen.getGuiTop() + this.y1; }

    private int posIndex;
    public int getPositionIndex() { return this.posIndex; }

    @SafeVarargs
    public static LazyWidgetPositioner create(IScreen screen, Function<LazyWidgetPositioner,Pair<Integer,Integer>> mode, int x1, int y1, int widgetSize, ClickableWidget... widgets) {
        return new LazyWidgetPositioner(screen, mode, x1, y1, widgetSize, widgets);
    }

    @SafeVarargs
    private LazyWidgetPositioner(IScreen screen, Function<LazyWidgetPositioner,Pair<Integer,Integer>> mode, int x1, int y1, int widgetSize, ClickableWidget... widgets) {
        this.screen = Objects.requireNonNull(screen);
        this.mode = Objects.requireNonNull(mode);
        this.x1 = x1;
        this.y1 = y1;
        this.widgetSize = widgetSize;
        this.screen.addTickListener(this::reposition);
        for(ClickableWidget w : widgets) this.addWidget(w);
    }

    public <T extends ClickableWidget> T addWidget(T widget) {
        if(widget != null && !this.widgetList.contains(widget))
            this.widgetList.add(widget);
        return widget;
    }

    public void addWidgets(Object... widgets) {
        for(Object w : widgets)
        {
            if(w instanceof ClickableWidget cw)
                this.addWidget(cw);
        }
    }

    public void reposition() {
        this.posIndex = 0;
        for (ClickableWidget w : new ArrayList<>(this.widgetList)) {
            if (w.visible) {
                Pair<Integer, Integer> pos = this.mode.apply(this);
                w.setPosition(pos.getFirst(), pos.getSecond());
                this.posIndex++;
            }
        }
    }

    public void clear()
    {
        this.widgetList.clear();
    }

}