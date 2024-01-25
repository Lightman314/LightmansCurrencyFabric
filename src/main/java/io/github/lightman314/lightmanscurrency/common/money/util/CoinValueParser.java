package io.github.lightman314.lightmanscurrency.common.money.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoinValueParser {

    public static CoinValue parse(StringReader reader, boolean allowEmpty) throws CommandSyntaxException {
        CoinValue value = new CoinValue();
        StringReader inputReader = new StringReader(readStringUntil(reader, ' '));
        while(inputReader.canRead())
        {
            String s1 = readStringUntil(inputReader, '-',',');
            if(NumberUtil.IsInteger(s1))
            {
                int count = NumberUtil.GetIntegerValue(s1, 1);
                String s2 = readStringUntil(inputReader,',');
                TryParseCoin(value, inputReader, s2, count);
            }
            else
            {
                TryParseCoin(value, inputReader, s1, 1);
            }
        }
        if(!allowEmpty && !value.hasAny())
            throw NoValueException(reader);
        return value;
    }

    public static String writeParsable(@NotNull CoinValue value)
    {
        StringBuilder builder = new StringBuilder();
        boolean comma = false;
        for(CoinValue.CoinValuePair pair : value.getEntries())
        {
            if(comma)
                builder.append(',');
            else
                comma = true;
            builder.append(pair.amount).append('-').append(Registries.ITEM.getId(pair.coin));
        }
        return builder.toString();
    }

    private static String readStringUntil(StringReader reader, char... t) throws CommandSyntaxException {
        List<Character> terminators = new ArrayList<>();
        for(char c : t)
            terminators.add(c);
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (reader.canRead()) {
            final char c = reader.read();
            if (escaped) {
                if (terminators.contains(c) || c == '\\') {
                    result.append(c);
                    escaped = false;
                } else {
                    reader.setCursor(reader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, String.valueOf(c));
                }
            } else if (c == '\\') {
                escaped = true;
            } else if (terminators.contains(c)) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        //If end is reached, assume end
        return result.toString();
    }

    private static void TryParseCoin(CoinValue result, StringReader reader, String coinIDString, int count) throws CommandSyntaxException
    {
        if(Identifier.isValid(coinIDString))
        {
            Identifier coinID = new Identifier(coinIDString);
            Item coin = Registries.ITEM.get(coinID);
            if(!MoneyUtil.isCoin(coin, false))
                throw NotACoinException(coinIDString, reader);
            result.addValue(coin, count);
        }
        else
            throw NotACoinException(coinIDString, reader);
    }

    public static CommandSyntaxException NoValueException(StringReader reader) {
        return new CommandSyntaxException(EXCEPTION_TYPE, EasyText.translatable("command.argument.coinvalue.novalue"), reader.getString(), reader.getCursor());
    }

    public static CommandSyntaxException NotACoinException(String item, StringReader reader) {
        return new CommandSyntaxException(EXCEPTION_TYPE, EasyText.translatable("command.argument.coinvalue.notacoin", item), reader.getString(), reader.getCursor());
    }

    private static final CommandExceptionType EXCEPTION_TYPE = new CommandExceptionType() {
        @Override
        public int hashCode() { return super.hashCode(); }
    };

}