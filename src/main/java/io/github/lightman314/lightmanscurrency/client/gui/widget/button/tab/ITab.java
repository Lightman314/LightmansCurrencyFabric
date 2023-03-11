package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

public interface ITab
{
    @NotNull
    IconData getIcon();
    int getColor();
    MutableText getTooltip();

}
