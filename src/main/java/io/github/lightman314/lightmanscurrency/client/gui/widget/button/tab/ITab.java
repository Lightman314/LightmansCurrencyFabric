package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

public interface ITab
{
    @NotNull
    public IconData getIcon();
    public int getColor();
    public MutableText getTooltip();

}
