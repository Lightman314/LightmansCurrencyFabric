package io.github.lightman314.lightmanscurrency.common.menu.factory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class SimpleMenuFactory implements NamedScreenHandlerFactory{

	private final IMenuFactory factory;
	private final Text name;
	
	public SimpleMenuFactory(IMenuFactory factory) { this(factory, Text.empty()); }
	
	public SimpleMenuFactory(IMenuFactory factory, Text name) {
		this.factory = factory;
		this.name = name;
	}
	
	@Override
	public ScreenHandler createMenu(int windowID, PlayerInventory inventory, PlayerEntity player) { return this.factory.createMenu(windowID, inventory, player); }

	@Override
	public Text getDisplayName() { return this.name; }
	
	public interface IMenuFactory { ScreenHandler createMenu(int windowID, PlayerInventory inventory, PlayerEntity player); }

}
