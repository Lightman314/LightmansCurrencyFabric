package io.github.lightman314.lightmanscurrency.common.traders.tradedata;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.function.UnaryOperator;

public class AlertData {

    public enum AlertType {
        HELPFUL(0x00FF00, Formatting.GREEN, 1),
        WARN(0xFF7F00, Formatting.GOLD, 3),
        ERROR(0xFF0000, Formatting.RED, 5);

        private final int priority;
        private final int color;
        private final UnaryOperator<Style> format;
        AlertType(int color, Formatting format, int priority) { this.color = color; this.format = s -> s.withFormatting(format); this.priority = priority;}
    }

    private final MutableText message;
    public final int priority;
    public final int color;
    private final UnaryOperator<Style> formatting;

    private AlertData(MutableText message, int priority, int color, UnaryOperator<Style> format) {
        this.message = message;
        this.priority = priority;
        this.color = color;
        this.formatting = format;
    }

    @Environment(EnvType.CLIENT)
    public void setShaderColor(DrawContext gui, float mult) {
        float red = (float)(this.color >> 16 & 255) / 255.0f;
        float green = (float)(this.color >> 8 & 255) / 255.0f;
        float blue = (float)(this.color & 255) / 255.0f;
        gui.setShaderColor(red * mult, green * mult, blue * mult, 1f);
    }

    public MutableText getFormattedMessage() { return this.message.styled(this.formatting); }

    public static final int compare(AlertData a, AlertData b) {return Integer.compare(a.priority, b.priority) * -1; }

    public static final AlertData helpful(MutableText message) { return of(message, AlertType.HELPFUL); }
    public static final AlertData warn(MutableText message) { return of(message, AlertType.WARN); }
    public static final AlertData error(MutableText message) { return of(message, AlertType.ERROR); }

    private static final AlertData of(MutableText message, AlertType type) { return of(message, type.priority, type.color, type.format); }

    public static final AlertData of(MutableText message, int priority, int color, UnaryOperator<Style> style) {
        return new AlertData(message, priority, color, style);
    }

}