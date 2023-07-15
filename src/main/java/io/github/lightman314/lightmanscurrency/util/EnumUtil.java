package io.github.lightman314.lightmanscurrency.util;

public class EnumUtil {

    public static <T extends Enum<?>> T enumFromString(String string, T[] allValues, T defaultValue)
    {
        for(T val : allValues)
        {
            if(val.toString().contentEquals(string))
                return val;
        }
        return defaultValue;
    }

    public static <T extends Enum<?>> int sortEnum(T e1, T e2) { return Integer.compare(e1.ordinal(), e2.ordinal()); }

}