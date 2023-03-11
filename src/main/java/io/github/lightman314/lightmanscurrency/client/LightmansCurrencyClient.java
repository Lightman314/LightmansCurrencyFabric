package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.data.*;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.FreezerTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.core.groups.BlockConvertible;
import io.github.lightman314.lightmanscurrency.common.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.common.items.CoinItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.ticket.TicketSlot;
import io.github.lightman314.lightmanscurrency.common.menu.slots.trader.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.config.Config;
import io.github.lightman314.lightmanscurrency.config.network.SynchronizedConfigPacketHandler;
import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import io.github.lightman314.lightmanscurrency.network.client.LCClientPacketHandler;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

import java.util.List;

public class LightmansCurrencyClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.SERVER_TO_CLIENT, new LCClientPacketHandler());
		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.CONFIG_SYNC, new SynchronizedConfigPacketHandler());

		//Set certain blocks as cutout layer
		this.setRenderLayer(RenderLayer.getCutout(), ModBlocks.DISPLAY_CASE, ModBlocks.VENDING_MACHINE, ModBlocks.VENDING_MACHINE_LARGE, ModBlocks.ARMOR_DISPLAY);
		this.setRenderLayer(RenderLayer.getTranslucent(), ModBlocks.GEM_TERMINAL);

		//Register Screens
		HandledScreens.register(ModMenus.ATM, ATMScreen::new);
		HandledScreens.register(ModMenus.MINT, MintScreen::new);
		HandledScreens.register(ModMenus.TICKET_MACHINE, TicketMachineScreen::new);
		HandledScreens.register(ModMenus.TRADER_INTERFACE, TraderInterfaceScreen::new);
		HandledScreens.register(ModMenus.TRADER_RECOVERY, TraderRecoveryScreen::new);
		HandledScreens.register(ModMenus.TRADER, TraderScreen::new);
		HandledScreens.register(ModMenus.TRADER_BLOCK, TraderScreen::new);
		HandledScreens.register(ModMenus.TRADER_NETWORK_ALL, TraderScreen::new);
		HandledScreens.register(ModMenus.TRADER_STORAGE, TraderStorageScreen::new);
		HandledScreens.register(ModMenus.WALLET_BANK, WalletBankScreen::new);
		HandledScreens.register(ModMenus.WALLET, WalletScreen::new);

		//Block Entity Renderers
		BlockEntityRendererRegistry.register(ModBlockEntities.ITEM_TRADER, ItemTraderBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.FREEZER_TRADER, FreezerTraderBlockEntityRenderer::new);

		//Register Item Colors
		ColorProviderRegistry.ITEM.register(new TicketColor(), ModItems.TICKET, ModItems.TICKET_MASTER);

		//Register extra player layers
		EntityModelLayerRegistry.registerModelLayer(ModLayerDefinitions.WALLET, WalletLayer::createLayer);

		//Reload configs on server start
		ServerHook.addServerStartListener(server -> LCConfigClient.INSTANCE.reloadFromFile());

		//Register Event Listeners
		this.registerEventListeners();
		ClientEventListeners.init();

		//Register Config
		Config.register(LCConfigClient.INSTANCE);

	}

	private void setRenderLayer(RenderLayer layer, BlockConvertible... blocks) {
		for(BlockConvertible block : blocks)
		{
			for(Block b : block.asBlock())
			{
				BlockRenderLayerMap.INSTANCE.putBlocks(layer, b);
			}
		}
	}

	private void registerEventListeners() {

		WorldRenderEvents.START.register(ScreenUtil::onRenderTick);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(WalletLayer::registerLayer);

		ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(this::stitchTextures);

		ClientPlayConnectionEvents.INIT.register(((handler, client) -> ItemEditWidget.initItemList()));

		ClientPlayConnectionEvents.DISCONNECT.register(ClientBankData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientEjectionData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientNotificationData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientTeamData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientTraderData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientWalletData::onClientLogout);

		ItemTooltipCallback.EVENT.register(this::appendTooltips);

	}

	private void stitchTextures(SpriteAtlasTexture atlasTexture, ClientSpriteRegistryCallback.Registry registry) {

		//Register extra textures to the PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
		registry.register(CoinSlot.EMPTY_COIN_SLOT);
		registry.register(TicketSlot.EMPTY_TICKET_SLOT);
		registry.register(WalletSlot.EMPTY_WALLET_SLOT);
		registry.register(ItemRenderUtil.EMPTY_SLOT_BG);
		registry.register(UpgradeInputSlot.EMPTY_UPGRADE_SLOT);

	}

	private void appendTooltips(ItemStack stack, TooltipContext context, List<Text> lines) {
		if(MoneyUtil.isCoin(stack) && !(stack.getItem() instanceof CoinItem || stack.getItem()instanceof CoinBlockItem))
			CoinItem.addCoinTooltips(stack, lines);
	}

}
