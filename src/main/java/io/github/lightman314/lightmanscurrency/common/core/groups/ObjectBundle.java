package io.github.lightman314.lightmanscurrency.common.core.groups;

import java.util.*;
import java.util.function.BiConsumer;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;

public class ObjectBundle<T,L> {

	private boolean locked = false;
	public ObjectBundle<T,L> lock() { this.locked = true; return this; }
	
	private final Map<L,T> values = new HashMap<>();
	
	public void put(L key, T value) {
		if(this.locked)
		{
			LightmansCurrency.LogWarning("Attempted to put an object in the bundle after it's been locked.");
			return;
		}
		if(this.values.containsKey(key))
		{
			LightmansCurrency.LogWarning("Attempted to put a second object with key " + key.toString() + " into the registry bundle.");
			return;
		}
		this.values.put(key,value);
	}
	
	public T get(L key) { return this.values.getOrDefault(key, null); }
	
	public Collection<T> getAll() { return this.values.values(); }

	public Collection<T> getAllSorted(Comparator<L> sorter)
	{
		List<T> result = new ArrayList<>();
		this.values.keySet().stream().sorted(sorter).forEach(k -> result.add(this.get(k)));
		return result;
	}
	
	public void foreach(BiConsumer<L,T> consumer) { this.values.forEach(consumer); }
	
}
