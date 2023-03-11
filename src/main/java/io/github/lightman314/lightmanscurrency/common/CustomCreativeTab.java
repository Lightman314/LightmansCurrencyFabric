package io.github.lightman314.lightmanscurrency.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.util.collection.DefaultedList;

public class CustomCreativeTab extends ItemGroup {

	private final ItemSorter itemSorter;

	private final List<Enchantment> enchantments = new ArrayList<>();
	Supplier<ItemConvertible> iconItem;


	public CustomCreativeTab(String label, Supplier<ItemConvertible> iconItem)
	{
		super(expandAndGetIndex(), label);
		this.iconItem = iconItem;
		this.itemSorter = new ItemSorter();
	}

	private static int expandAndGetIndex() {
		((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
		return ItemGroup.GROUPS.length - 1;
	}

	@Override
	public ItemStack createIcon()
	{
		if(this.iconItem != null)
			return new ItemStack(this.iconItem.get());
		return ItemStack.EMPTY;
	}

	public void addEnchantments(Enchantment... enchantments) {
		this.enchantments.addAll(List.of(enchantments));
	}

	@Override
	public void appendStacks(DefaultedList<ItemStack> items) {
		
		super.appendStacks(items);

		// Sort the item list using the ItemSorter instance
		items.sort(this.itemSorter);

		//Add custom enchantments
		for(Enchantment e : this.enchantments)
		{
			for(int level = 1; level <= e.getMaxLevel(); level++) {
				items.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(e, level)));
			}
		}
		
	}
	
	public void addToSortingList(List<ItemConvertible> extras)
	{
		this.itemSorter.addToSortingList(extras);
	}
	
	/**
	 * Initializes the sorting list of the item group. Should be called in the FMLCommonSetupEvent.
	 */
	public void initSortingList(List<ItemConvertible> defaultList)
	{
		this.itemSorter.initSortingList(defaultList);
	}
	
	private static class ItemSorter implements Comparator<ItemStack>
	{
		
		public ItemSorter()
		{
			
		}
		
		private ArrayList<Item> sortList = null;
		
		private ArrayList<Item> convertList(List<ItemConvertible> sourceList)
		{
			ArrayList<Item> list = new ArrayList<>();
			for (ItemConvertible itemConvertible : sourceList) {
				list.add(itemConvertible.asItem());
			}
			return list;
		}
		
		public void initSortingList(List<ItemConvertible> sortList)
		{
			if(this.sortList == null)
			{
				this.sortList = this.convertList(sortList);
			}
			else
			{
				List<Item> copyList = this.sortList;
				this.sortList = this.convertList(sortList);
				this.sortList.addAll(copyList);
			}
		}
		
		public void addToSortingList(List<ItemConvertible> extras)
		{
			if(this.sortList == null)
			{
				//LightmansCurrency.LogWarning("Sorting list has not been initialized. Adding temporarily, until the official init arrives.");
				this.sortList = this.convertList(extras);
				return;
			}
			for (ItemConvertible extra : extras) {
				this.sortList.add(extra.asItem());
			}
			LightmansCurrency.LogDebug("Added " + extras.size() + " items to the creative tab sorting list.");
		}
		
		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			
			Item item1 = o1.getItem();
			Item item2 = o2.getItem();
			
			if(sortList == null)
			{
				LightmansCurrency.LogWarning("No sortlist defined for this CurrencyGroup.");
				return 0;
			}
			
			//If item1 is on the sort list and item2 isn't, sort item1 before item2
			if(sortList.contains(item1) && !sortList.contains(item2))
			{
				return -1;
			}
			
			//If item2 is on the sort list and item1 isn't, sort item1 before item2
			if(!sortList.contains(item1) && sortList.contains(item2))
			{
				return 1;
			}
			
			//If both items are on the sort list, sort by index
			if(sortListContains(item1) && sortListContains(item2))
			{
				int index1 = indexOf(item1);
				int index2 = indexOf(item2);
				//CurrencyMod.LOGGER.info("Sorting items at index " + index1 + " & " + index2);
				return Integer.compare(index1, index2);
			}
			
			//No other sort method found, do nothing.
			return 0;
			
		}
		
		private boolean sortListContains(Item item)
		{
			return indexOf(item) >= 0;
		}
		
		private int indexOf(Item item)
		{
			for(int i = 0; i < this.sortList.size(); i++)
			{
				if(item == this.sortList.get(i))
					return i;
			}
			return -1;
		}
		
	}
	
}