package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class TooltipItem extends Item{

	public static final Style DEFAULT_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
	
	private final Supplier<List<Text>> tooltips;
	
	public TooltipItem(Settings properties, Supplier<List<Text>> tooltips) { super(properties); this.tooltips = tooltips; }
	
	public static List<Text> getTooltipLines(String tooltipTranslation) { return getTooltipLines(tooltipTranslation, DEFAULT_STYLE); }
	public static List<Text> getTooltipLines(String tooltipTranslation, @Nullable Style format) {
		List<Text> result = new ArrayList<>();
		int i = 0;
		
		while(true)
		{
			MutableText nextLine = getTooltipLine(tooltipTranslation, ++i);
			if(nextLine == null)
				return result;
			if(format != null)
				nextLine.fillStyle(format);
			result.add(nextLine);
		}
	}
	
	private static MutableText getTooltipLine(String tooltipTranslation, int page) {
		String tt = (tooltipTranslation.endsWith(".") ? tooltipTranslation : tooltipTranslation + ".") + String.valueOf(page);
		MutableText result = Text.translatable(tt);
		//Returns null if the translated text is the translation key.
		if(result.getString().contentEquals(tt))
			return null;
		return result;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World level, List<Text> tooltip, TooltipContext flagIn)
	{
		addTooltip(tooltip, this.tooltips);
		super.appendTooltip(stack, level, tooltip, flagIn);
	}
	
	public static void addTooltip(List<Text> tooltip, Supplier<List<Text>> tooltipSource) {
		List<Text> addableTooltips = tooltipSource.get();
		if(addableTooltips == null || addableTooltips.size() <= 0)
			return;
		if(Screen.hasShiftDown())
			tooltip.addAll(tooltipSource.get());
		else
			tooltip.add(Text.translatable("tooltip.lightmanscurrency.tooltip").fillStyle(DEFAULT_STYLE));
	}
	
	public static void addTooltipAlways(List<Text> tooltip, Supplier<List<Text>> tooltipSource) {
		tooltip.addAll(tooltipSource.get());
	}
	
	@SuppressWarnings("unchecked")
	public static Supplier<List<Text>> combine(Supplier<List<Text>>... tooltipSources) {
		return () -> {
			List<Text> result = new ArrayList<>();
			for(Supplier<List<Text>> source : tooltipSources)
			{
				List<Text> val = source.get();
				if(val != null)
					result.addAll(val);
			}
			return result;
		};
	}
	
}