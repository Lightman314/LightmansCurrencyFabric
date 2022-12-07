package io.github.lightman314.lightmanscurrency.common.upgrades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.*;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class UpgradeType {

	private static final Map<Identifier,UpgradeType> UPGRADE_TYPE_REGISTRY = new HashMap<>();
	
	public static final ItemCapacityUpgrade ITEM_CAPACITY = register(new Identifier(LightmansCurrency.MODID, "item_capacity"), new ItemCapacityUpgrade());
	
	public static final SpeedUpgrade SPEED = register(new Identifier(LightmansCurrency.MODID, "speed"), new SpeedUpgrade());
	
	public static final Simple NETWORK = register(new Identifier(LightmansCurrency.MODID, "trader_network"), new Simple(Text.translatable("tooltip.lightmanscurrency.upgrade.network")));
	
	public static final Simple HOPPER = register(new Identifier(LightmansCurrency.MODID, "hopper"), new Simple(Text.translatable("tooltip.lightmanscurrency.upgrade.hopper")));
	
	private Identifier type;
	
	protected abstract List<String> getDataTags();
	protected abstract Object defaultTagValue(String tag);
	public List<Text> getTooltip(UpgradeData data) { return new ArrayList<>(); }
	public final UpgradeData getDefaultData() { return new UpgradeData(this); }
	
	public UpgradeType setRegistryName(Identifier name) {
		this.type = name;
		return this;
	}

	public Identifier getRegistryName() {
		return this.type;
	}

	public Class<UpgradeType> getRegistryType() {
		return UpgradeType.class;
	}
	
	public static <T extends UpgradeType> T register(Identifier type, T upgradeType)
	{
		upgradeType.setRegistryName(type);
		UPGRADE_TYPE_REGISTRY.put(type, upgradeType);
		return upgradeType;
	}
	
	public interface IUpgradeable
	{
		public default boolean allowUpgrade(UpgradeItem item) { return this.allowUpgrade(item.getUpgradeType()); }
		public boolean allowUpgrade(UpgradeType type);
	}
	
	public interface IUpgradeItem
	{
		public UpgradeType getUpgradeType();
		public UpgradeData getDefaultUpgradeData();
		public default void onApplied(IUpgradeable target) { }
	}
	
	public static class UpgradeData
	{
		
		private final Map<String,Object> data = Maps.newHashMap();
		
		public Set<String> getKeys() { return data.keySet(); }
		
		public boolean hasKey(String tag)
		{
			return this.getKeys().contains(tag);
		}
		
		public UpgradeData(UpgradeType upgrade)
		{
			for(String tag : upgrade.getDataTags())
			{
				Object defaultValue = upgrade.defaultTagValue(tag);
				data.put(tag, defaultValue);
			}
		}
		
		public void setValue(String tag, Object value)
		{
			if(data.containsKey(tag))
				data.put(tag, value);
		}
		
		public Object getValue(String tag)
		{
			if(data.containsKey(tag))
				return data.get(tag);
			return null;
		}
		
		public int getIntValue(String tag)
		{
			Object value = getValue(tag);
			if(value instanceof Integer)
				return (Integer)value;
			return 0;
		}
		
		public float getFloatValue(String tag)
		{
			Object value = getValue(tag);
			if(value instanceof Float)
				return (Float)value;
			return 0f;
		}
		
		public String getStringValue(String tag)
		{
			Object value = getValue(tag);
			if(value instanceof String)
				return (String)value;
			return "";
		}
		
		public void read(NbtCompound compound)
		{
			compound.getKeys().forEach(key ->{
				if(this.hasKey(key))
				{
					if(compound.contains(key, NbtElement.INT_TYPE))
						this.setValue(key, compound.getInt(key));
					else if(compound.contains(key, NbtElement.FLOAT_TYPE))
						this.setValue(key, compound.getFloat(key));
					else if(compound.contains(key, NbtElement.STRING_TYPE))
						this.setValue(key, compound.getString(key));
				}
			});
		}
		
		public NbtCompound writeToNBT() { return writeToNBT(null); }
		
		public NbtCompound writeToNBT(UpgradeType source)
		{
			Map<String,Object> modifiedEntries = source == null ? this.data : getModifiedEntries(this,source);
			NbtCompound compound = new NbtCompound();
			modifiedEntries.forEach((key,value) ->{
				if(value instanceof Integer)
					compound.putInt(key, (Integer)value);
				else if(value instanceof Float)
					compound.putFloat(key, (Float)value);
				else if(value instanceof String)
					compound.putString(key, (String)value);
			});
			return compound;
		}
		
		public static Map<String,Object> getModifiedEntries(UpgradeData queryData, UpgradeType source)
		{
			Map<String,Object> modifiedEntries = Maps.newHashMap();
			source.getDefaultData().data.forEach((key, value) -> {
				if(queryData.data.containsKey(key) && !Objects.equal(queryData.data.get(key), value))
						modifiedEntries.put(key, value);
			});
			return modifiedEntries;
		}
		
		
		
	}
	
	public static boolean hasUpgrade(UpgradeType type, Inventory upgradeContainer) {
		for(int i = 0; i < upgradeContainer.size(); ++i)
		{
			ItemStack stack = upgradeContainer.getStack(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(upgradeItem.getUpgradeType() == type)
					return true;
			}
		}
		return false;
	}
	
	public static class Simple extends UpgradeType {

		private final List<Text> tooltips;
		public Simple(Text... tooltips) { this.tooltips = Lists.newArrayList(tooltips); }
		
		@Override
		protected List<String> getDataTags() { return new ArrayList<>(); }

		@Override
		protected Object defaultTagValue(String tag) { return null; }
		
		@Override
		public List<Text> getTooltip(UpgradeData data) { return this.tooltips; }
		
	}
	
}