package io.github.lightman314.lightmanscurrency.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.UnaryOperator;

public class TextRenderUtil {

    public static class TextFormatting
    {

        public enum Centering {
            TOP_LEFT(-1,1), TOP_CENTER(0,1), TOP_RIGHT(1,1),
            MIDDLE_LEFT(-1,0), MIDDLE_CENTER(0,0), MIDDLE_RIGHT(1,0),
            BOTTOM_LEFT(-1,-1), BOTTOM_CENTER(0,-1), BOTTOM_RIGHT(1,-1);

            private final int horiz;
            private final int vert;

            Centering(int horiz, int vert) { this.horiz = horiz; this.vert = vert; }
            public boolean isTop() { return vert > 0; }
            public boolean isMiddle() { return vert == 0; }
            public boolean isBottom() { return vert < 0; }
            public boolean isLeft() { return horiz < 0; }
            public boolean isCenter() { return horiz == 0; }
            public boolean isRight() { return horiz > 1; }

            public Centering makeTop() { return this.of(this.horiz, 1); }
            public Centering makeMiddle() { return this.of(this.horiz, 0); }
            public Centering makeBottom() { return this.of(this.horiz, -1); }

            public Centering makeLeft() { return this.of(-1, this.vert); }
            public Centering makeCenter() { return this.of(0, this.vert); }
            public Centering makeRight() { return this.of(1, this.vert); }

            private Centering of(int horiz, int vert) {
                for(Centering c : Centering.values())
                {
                    if(c.horiz == horiz && c.vert == vert)
                        return c;
                }
                return this;
            }

        }

        private Centering centering = Centering.MIDDLE_CENTER;
        public Centering centering() { return this.centering; }
        private int color = 0xFFFFFF;
        public int color() { return this.color; }

        private TextFormatting() {}

        public static TextFormatting create() { return new TextFormatting(); }

        public TextFormatting topEdge() { this.centering = this.centering.makeTop(); return this; }
        public TextFormatting middle() { this.centering = this.centering.makeMiddle(); return this; }
        public TextFormatting bottomEdge() { this.centering = this.centering.makeBottom(); return this; }

        public TextFormatting leftEdge() { this.centering = this.centering.makeLeft(); return this; }
        public TextFormatting centered() { this.centering = this.centering.makeCenter(); return this; }
        public TextFormatting rightEdge() { this.centering = this.centering.makeRight(); return this; }

        public TextFormatting color(int color) { this.color = color; return this; }

    }

    public static TextRenderer getFont() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.textRenderer;
    }

    public static Text fitString(String text, int width) { return fitString(text, width, "..."); }

    public static Text fitString(String text, int width, Style style) { return fitString(text, width, "...", style); }

    public static Text fitString(String text, int width, String edge) { return fitString(Text.literal(text), width, edge); }

    public static Text fitString(Text component, int width) { return fitString(component.getString(), width, "...", component.getStyle()); }

    public static Text fitString(Text component, int width, String edge) { return fitString(component.getString(), width, edge, component.getStyle()); }

    public static Text fitString(Text component, int width, Style style) { return fitString(component.getString(), width, "...", style); }

    public static Text fitString(Text component, int width, String edge, Style style) { return fitString(component.getString(), width, edge, style); }

    public static Text fitString(String text, int width, String edge, Style style) {
        TextRenderer font = getFont();
        if(font.getWidth(Text.literal(text).setStyle(style)) <= width)
            return Text.literal(text).setStyle(style);
        while(font.getWidth(Text.literal(text + edge).setStyle(style)) > width && text.length() > 0)
            text = text.substring(0, text.length() - 1);
        return Text.literal(text + edge).setStyle(style);
    }

    public static void drawCenteredText(DrawContext gui, String string, int centerX, int yPos, int color) { drawCenteredText(gui, Text.literal(string), centerX, yPos, color); }
    public static void drawCenteredText(DrawContext gui, Text component, int centerX, int yPos, int color) {
        TextRenderer font = getFont();
        int width = font.getWidth(component);
        gui.drawText(font, component, centerX - (width/2), yPos, color, false);
    }

    public static void drawRightEdgeText(DrawContext gui, String string, int rightPos, int yPos, int color) { drawRightEdgeText(gui, Text.literal(string), rightPos, yPos, color); }
    public static void drawRightEdgeText(DrawContext gui, Text component, int rightPos, int yPos, int color) {
        TextRenderer font = getFont();
        int width = font.getWidth(component);
        gui.drawText(font, component, rightPos, yPos - width, color, false);
    }

    public static void drawCenteredMultilineText(DrawContext gui, String string, int leftPos, int width, int topPos, int color) { drawCenteredMultilineText(gui, Text.literal(string), leftPos, width, topPos, color); }
    public static void drawCenteredMultilineText(DrawContext gui, Text component, int leftPos, int width, int topPos, int color) {
        TextRenderer font = getFont();
        List<OrderedText> lines = font.wrapLines(component, width);
        int centerPos = leftPos + (width / 2);
        for(int i = 0; i < lines.size(); ++i)
        {
            OrderedText line = lines.get(i);
            int lineWidth = font.getWidth(line);
            gui.drawText(font, line, centerPos - (lineWidth/2), topPos + font.fontHeight * i, color, false);
        }
    }

    public static void drawVerticallyCenteredMultilineText(DrawContext gui, String string, int leftPos, int width, int topPos, int height, int color) { drawVerticallyCenteredMultilineText(gui, Text.literal(string), leftPos, width, topPos, height, color); }
    public static void drawVerticallyCenteredMultilineText(DrawContext gui, Text component, int leftPos, int width, int topPos, int height, int color) {
        TextRenderer font = getFont();
        List<OrderedText> lines = font.wrapLines(component, width);
        int centerPos = leftPos + (width / 2);
        int startHeight = topPos + (height / 2) - ((font.fontHeight * lines.size())/2);
        for(int i = 0; i < lines.size(); ++i)
        {
            OrderedText line = lines.get(i);
            int lineWidth = font.getWidth(line);
            gui.drawText(font, line, centerPos - (lineWidth/2), startHeight + font.fontHeight * i, color, false);
        }
    }

    public static MutableText changeStyle(Text component, UnaryOperator<Style> styleChanges) {
        if(component instanceof MutableText mc) {
            return mc.styled(styleChanges);
        }
        return Text.empty().append(component).setStyle(component.getStyle()).styled(styleChanges);
    }

}