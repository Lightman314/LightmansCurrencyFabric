package io.github.lightman314.lightmanscurrency.client.gui.widget.button.notifications;

import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MarkAsSeenButton extends ButtonWidget {

    public static final int HEIGHT = 11;

    public MarkAsSeenButton(int rightPos, int yPos, Text text, PressAction onPress) {
        super(rightPos - getWidth(text), yPos, getWidth(text), HEIGHT, text, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    private static int getWidth(Text text) { return TextRenderUtil.getFont().getWidth(text) + 4; }

}