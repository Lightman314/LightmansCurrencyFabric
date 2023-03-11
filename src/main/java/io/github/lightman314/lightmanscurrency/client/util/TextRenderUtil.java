package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
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

    public static Text fitString(String text, int width, String edge) { return fitString(EasyText.literal(text), width, edge); }

    public static Text fitString(Text component, int width) { return fitString(component.getString(), width, "...", component.getStyle()); }

    public static Text fitString(Text component, int width, String edge) { return fitString(component.getString(), width, edge, component.getStyle()); }

    public static Text fitString(Text component, int width, Style style) { return fitString(component.getString(), width, "...", style); }

    public static Text fitString(Text component, int width, String edge, Style style) { return fitString(component.getString(), width, edge, style); }

    public static Text fitString(String text, int width, String edge, Style style) {
        TextRenderer font = getFont();
        if(font.getWidth(EasyText.literal(text).setStyle(style)) <= width)
            return EasyText.literal(text).setStyle(style);
        while(font.getWidth(EasyText.literal(text + edge).setStyle(style)) > width && text.length() > 0)
            text = text.substring(0, text.length() - 1);
        return EasyText.literal(text + edge).setStyle(style);
    }

    public static void drawCenteredText(MatrixStack pose, String string, int centerX, int yPos, int color) { drawCenteredText(pose, EasyText.literal(string), centerX, yPos, color); }
    public static void drawCenteredText(MatrixStack pose, Text component, int centerX, int yPos, int color) {
        TextRenderer font = getFont();
        int width = font.getWidth(component);
        font.draw(pose, component, centerX - (width/2), yPos, color);
    }

    public static void drawRightEdgeText(MatrixStack pose, String string, int rightPos, int yPos, int color) { drawRightEdgeText(pose, EasyText.literal(string), rightPos, yPos, color); }
    public static void drawRightEdgeText(MatrixStack pose, Text component, int rightPos, int yPos, int color) {
        TextRenderer font = getFont();
        int width = font.getWidth(component);
        font.draw(pose, component, rightPos, yPos - width, color);
    }

    public static void drawCenteredMultilineText(MatrixStack pose, String string, int leftPos, int width, int topPos, int color) { drawCenteredMultilineText(pose, EasyText.literal(string), leftPos, width, topPos, color); }
    public static void drawCenteredMultilineText(MatrixStack pose, Text component, int leftPos, int width, int topPos, int color) {
        TextRenderer font = getFont();
        List<OrderedText> lines = font.wrapLines(component, width);
        float centerPos = (float)leftPos + ((float)width / 2f);
        for(int i = 0; i < lines.size(); ++i)
        {
            OrderedText line = lines.get(i);
            int lineWidth = font.getWidth(line);
            font.draw(pose, line, centerPos - ((float)lineWidth/2f), topPos + font.fontHeight * i, color);
        }
    }

    public static void drawVerticallyCenteredMultilineText(MatrixStack pose, String string, int leftPos, int width, int topPos, int height, int color) { drawVerticallyCenteredMultilineText(pose, EasyText.literal(string), leftPos, width, topPos, height, color); }
    public static void drawVerticallyCenteredMultilineText(MatrixStack pose, Text component, int leftPos, int width, int topPos, int height, int color) {
        TextRenderer font = getFont();
        List<OrderedText> lines = font.wrapLines(component, width);
        float centerPos = (float)leftPos + ((float)width / 2f);
        float startHeight = (float)topPos + ((float)height / 2f) - ((float)(font.fontHeight * lines.size())/2f);
        for(int i = 0; i < lines.size(); ++i)
        {
            OrderedText line = lines.get(i);
            int lineWidth = font.getWidth(line);
            font.draw(pose, line, centerPos - ((float)lineWidth/2f), startHeight + font.fontHeight * i, color);
        }
    }

    public static MutableText changeStyle(Text component, UnaryOperator<Style> styleChanges) {
        if(component instanceof MutableText) {
            MutableText mc = (MutableText)component;
            return mc.styled(styleChanges);
        }
        return EasyText.empty().append(component).setStyle(component.getStyle()).styled(styleChanges);
    }

}