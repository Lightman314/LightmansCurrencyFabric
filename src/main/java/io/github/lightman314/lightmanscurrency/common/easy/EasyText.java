package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;

public class EasyText {

    public static MutableText empty() { return new LiteralText(""); }
    public static MutableText literal(String text) { return new LiteralText(text); }
    public static MutableText translatable(String translation) { return new TranslatableText(translation); }
    public static MutableText translatable(String translation, Object... children) { return new TranslatableText(translation, children); }

    public static void sendMessage(PlayerEntity player, Text message) { player.sendMessage(message, false); }

}
