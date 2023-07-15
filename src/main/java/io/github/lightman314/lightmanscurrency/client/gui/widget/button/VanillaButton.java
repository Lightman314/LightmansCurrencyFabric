package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class VanillaButton extends ButtonWidget {

    public VanillaButton(int x, int y, int width, int height, Text message, PressAction onPress) { super(x,y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER); }
}
