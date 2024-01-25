package io.github.lightman314.lightmanscurrency.common.easy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class EasyText {

    private EasyText() {}

    public static MutableText empty() { return Text.empty(); }
    public static MutableText literal(String literal) { return Text.literal(literal); }
    public static MutableText translatable(String translation, Object... children) { return Text.translatable(translation, children); }

    public static void sendMessage(PlayerEntity player, Text message) { player.sendMessage(message); }

    public static void sendCommandFail(ServerCommandSource source, Text message) { source.sendError(message); }
    public static void sendCommandSuccess(ServerCommandSource source, Text message, boolean postToAdmins) { source.sendFeedback(() -> message, postToAdmins); }

}
