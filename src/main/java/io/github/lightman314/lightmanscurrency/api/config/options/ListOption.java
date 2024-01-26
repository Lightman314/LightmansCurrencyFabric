package io.github.lightman314.lightmanscurrency.api.config.options;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ListOption<T> extends ConfigOption<List<T>> {


    protected ListOption(@NotNull Supplier<List<T>> defaultValue) { super(defaultValue); }

    public static <T> ConfigParser<List<T>> makeParser(ConfigParser<T> partialParser) { return new ListParser<>(partialParser); }

    @NotNull
    @Override
    protected ConfigParser<List<T>> getParser() { return makeParser(this.getPartialParser()); }

    protected abstract ConfigParser<T> getPartialParser();

    private static class ListParser<T> implements ConfigParser<List<T>>
    {
        private final ConfigParser<T> parser;
        private ListParser(@NotNull ConfigParser<T> parser) { this.parser = parser; }
        @NotNull
        @Override
        public List<T> tryParse(@NotNull String cleanLine) throws ConfigParsingException {
            if(cleanLine.length() == 0)
                throw new ConfigParsingException("Empty input received!");
            char c1 = cleanLine.charAt(0);
            if(c1 != '[')
                throw new ConfigParsingException("List does not start with '['!");
            char lastChar = cleanLine.charAt(cleanLine.length() - 1);
            if(lastChar != ']')
                throw new ConfigParsingException("List does not end with ']'!");

            List<String> sections = new ArrayList<>();
            StringBuilder temp = new StringBuilder();
            boolean inQuotes = false;
            boolean escaped = false;
            for(int i = 1; i < cleanLine.length() - 1; ++i)
            {
                char c = cleanLine.charAt(i);
                if(escaped)
                {
                    if(c == '\\' || c == '"')
                        temp.append(c);
                    else
                        temp.append('\\').append(c);
                    escaped = false;
                    continue;
                }
                if(c == '\\')
                {
                    escaped = true;
                    continue;
                }
                if(c == '"')
                {
                    inQuotes = !inQuotes;
                    continue;
                }
                if(c == ',')
                {
                    sections.add(temp.toString());
                    temp = new StringBuilder();
                    continue;
                }
                temp.append(c);
            }
            if(!temp.isEmpty())
                sections.add(temp.toString());
            if(sections.size() == 0)
                return new ArrayList<>();
            List<T> results = new ArrayList<>();
            for(int s = 0; s < sections.size(); ++s)
            {
                String section = sections.get(s);
                try { results.add(this.parser.tryParse(section));
                } catch (ConfigParsingException e) { LightmansCurrency.LogWarning("Failed to parse List Config entry #" + (s + 1), e); }
            }
            return results;
        }

        @NotNull
        @Override
        public String write(@NotNull List<T> value) {
            StringBuilder builder = new StringBuilder("[");
            boolean comma = false;
            for(T v : value)
            {
                if(comma)
                    builder.append(',');
                builder.append(this.parser.write(v));
                comma = true;
            }
            builder.append(']');
            return builder.toString();
        }
    }


}
