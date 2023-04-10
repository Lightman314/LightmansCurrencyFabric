package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class EasyText {

    private EasyText() {}

    public static MutableText empty() { return Text.empty(); }
    public static MutableText literal(String literal) { return Text.literal(literal); }
    public static MutableText translatable(String translation, Object... children) { return Text.translatable(translation, children); }

}
