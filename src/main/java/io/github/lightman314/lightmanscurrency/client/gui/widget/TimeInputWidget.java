package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class TimeInputWidget extends ClickableWidget {


    private final List<TimeUnit> relevantUnits;
    private final int spacing;
    public long maxDuration = Long.MAX_VALUE;
    public long minDuration = 0;
    private final Consumer<TimeData> timeConsumer;

    long days = 0;
    long hours = 0;
    long minutes = 0;
    long seconds = 0;

    public TimeData getTime() { return new TimeData(this.days, this.hours, this.minutes, this.seconds); }

    private List<ButtonWidget> buttons = new ArrayList<>();

    public TimeInputWidget(int x, int y, int spacing, TimeUnit largestUnit, TimeUnit smallestUnit, Consumer<ClickableWidget> widgetAdder, Consumer<TimeData> timeConsumer) {
        super(x, y, 0, 0, Text.empty());
        this.timeConsumer = timeConsumer;
        this.relevantUnits = this.getRelevantUnits(largestUnit, smallestUnit);
        this.spacing = spacing;

        for(int i = 0; i < this.relevantUnits.size(); ++i)
        {
            final TimeUnit unit = this.relevantUnits.get(i);

            int xPos = this.getX() + ((20 + this.spacing) * i);
            PlainButton addButton = new PlainButton(xPos, this.getY(), 20, 10, b -> this.addTime(unit), CoinValueInput.GUI_TEXTURE, 0, CoinValueInput.HEIGHT);
            PlainButton removeButton = new PlainButton(xPos, this.getY() + 23, 20, 10, b -> this.removeTime(unit), CoinValueInput.GUI_TEXTURE, 20, CoinValueInput.HEIGHT);
            widgetAdder.accept(addButton);
            widgetAdder.accept(removeButton);
            this.buttons.add(addButton);
            this.buttons.add(removeButton);
        }
    }

    public void setTime(long milliseconds) {
        this.setTimeInternal(milliseconds);
        this.validateTime();
        this.timeConsumer.accept(this.getTime());
    }

    public void setTime(TimeData time) {
        this.setTimeInternal(time);
        this.validateTime();
        this.timeConsumer.accept(this.getTime());
    }

    public void setTime(long days, long hours, long minutes, long seconds) {
        this.setTimeInternal(days, hours, minutes, seconds);
        this.validateTime();
        this.timeConsumer.accept(this.getTime());
    }

    private void setTimeInternal(long milliseconds) { this.setTimeInternal(new TimeData(milliseconds)); }

    private void setTimeInternal(TimeData time) { this.setTimeInternal(time.days, time.hours, time.minutes, time.seconds); }

    private void setTimeInternal(long days, long hours, long minutes, long seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;

        if(!this.validUnit(TimeUnit.DAY))
        {
            this.hours += this.days * 24;
            this.days = 0;
        }
        if(!this.validUnit(TimeUnit.HOUR))
        {
            this.minutes += this.hours * 60;
            this.hours = 0;
        }
        if(!this.validUnit(TimeUnit.MINUTE))
        {
            this.seconds += this.minutes * 60;
            this.minutes = 0;
        }
        if(!this.validUnit(TimeUnit.SECOND))
            this.seconds = 0;
    }

    private boolean validUnit(TimeUnit unit) { return this.relevantUnits.contains(unit); }

    private void addTime(TimeUnit unit) {
        switch(unit)
        {
            case DAY:
                this.days++;
                break;
            case HOUR:
                this.hours++;
                if(this.hours >= 24 && this.validUnit(TimeUnit.DAY))
                {
                    this.days += this.hours / 24;
                    this.hours = this.hours % 24;
                }
                break;
            case MINUTE:
                this.minutes++;
                if(this.minutes >= 60 && this.validUnit(TimeUnit.HOUR))
                {
                    this.hours += this.minutes / 60;
                    this.minutes = this.minutes % 60;
                }
                break;
            case SECOND:
                this.seconds++;
                if(this.seconds >= 60 && this.validUnit(TimeUnit.SECOND))
                {
                    this.minutes += this.seconds / 60;
                    this.seconds = this.seconds % 60;
                }
                break;
        }
        this.validateTime();
        this.timeConsumer.accept(this.getTime());
    }

    private void removeTime(TimeUnit unit) {
        this.removeTimeInternal(unit);
        this.validateTime();
        this.timeConsumer.accept(this.getTime());
    }

    private void removeTimeInternal(TimeUnit unit) {
        switch(unit) {
            case DAY:
                this.days = Math.max(0, this.days - 1);
                break;
            case HOUR:
                this.hours--;
                if(this.hours < 0 && this.days > 0)
                {
                    this.removeTimeInternal(TimeUnit.DAY);
                    this.hours += 24;
                }
                else
                    this.hours = 0;
                break;
            case MINUTE:
                this.minutes--;
                if(this.minutes < 0 && (this.hours > 0 || this.days > 0))
                {
                    this.removeTimeInternal(TimeUnit.HOUR);
                    this.minutes += 60;
                }
                else
                    this.minutes = 0;
                break;
            case SECOND:
                this.seconds--;
                if(this.seconds < 0 && (this.minutes > 0 || this.hours > 0 || this.days > 0))
                {
                    this.removeTimeInternal(TimeUnit.MINUTE);
                    this.seconds += 60;
                }
                break;
        }
    }

    private void validateTime() {
        long duration = this.getTime().miliseconds;
        if(duration > this.maxDuration)
            this.setTimeInternal(this.maxDuration);
        if(duration < this.minDuration)
            this.setTimeInternal(this.minDuration);
    }

    private List<TimeUnit> getRelevantUnits(TimeUnit largestUnit, TimeUnit smallestUnit) {
        List<TimeUnit> results = new ArrayList<>();
        List<TimeUnit> units = TimeUnit.UNITS_LARGE_TO_SMALL;
        int startIndex = units.indexOf(largestUnit);
        if(startIndex < 0)
            throw new RuntimeException("TimeUnit '" + largestUnit + "' could not be found on the TimeUnit list!");
        for(int i = startIndex; i < units.size(); ++i)
        {
            TimeUnit unit = units.get(i);
            results.add(unit);
            if(unit == smallestUnit)
                break;
        }
        return results;
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        for(ButtonWidget b : this.buttons)
        {
            b.active = this.active;
            b.visible = this.visible;
        }
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderButton(DrawContext gui, int mouseX, int mouseY, float delta) {
        for(int i = 0; i < this.relevantUnits.size(); ++i)
        {
            TextRenderUtil.drawCenteredText(gui, this.getTime().getUnitString(this.relevantUnits.get(i), true), this.getX() + ((20 + this.spacing) * i) + 10, this.getY() + 12, 0xFFFFFF);
        }
        gui.setShaderColor(1f,1f,1f,1f);
    }

    public void removeChildren(Consumer<ClickableWidget> remover) { for(ButtonWidget b : this.buttons) remover.accept(b); }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) { }

}