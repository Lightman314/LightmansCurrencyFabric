package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.SyncedConfigFile;
import io.github.lightman314.lightmanscurrency.client.colors.TicketColor;
import io.github.lightman314.lightmanscurrency.client.data.*;
import io.github.lightman314.lightmanscurrency.client.gui.overlay.WalletDisplayOverlay;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.FreezerTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.ItemTraderBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.SlotMachineBlockEntityRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.entity.layers.WalletLayer;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.core.groups.BlockConvertible;
import io.github.lightman314.lightmanscurrency.common.items.CoinBlockItem;
import io.github.lightman314.lightmanscurrency.common.items.CoinItem;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.PacketChannels;
import io.github.lightman314.lightmanscurrency.network.client.LCClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class LightmansCurrencyClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.SERVER_TO_CLIENT, new LCClientPacketHandler());

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
		HandledScreens.register(ModMenus.SLOT_MACHINE, SlotMachineScreen::new);

		//Block Entity Renderers
		BlockEntityRendererFactories.register(ModBlockEntities.ITEM_TRADER, ItemTraderBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlockEntities.FREEZER_TRADER, FreezerTraderBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlockEntities.SLOT_MACHINE, SlotMachineBlockEntityRenderer::new);

		//Register Item Colors
		ColorProviderRegistry.ITEM.register(new TicketColor(), ModItems.TICKET, ModItems.TICKET_MASTER);

		//Register extra player layers
		EntityModelLayerRegistry.registerModelLayer(ModLayerDefinitions.WALLET, WalletLayer::createLayer);

		//Register Event Listeners
		this.registerEventListeners();
		ClientEventListeners.init();

		//Register Synced Config reset listener
		ClientPlayConnectionEvents.DISCONNECT.register((handler,client) -> SyncedConfigFile.onClientLeavesServer());

		WalletDisplayOverlay.setup();

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

		ClientPlayConnectionEvents.INIT.register(((handler, client) -> ItemEditWidget.initItemList()));
		ClientPlayConnectionEvents.INIT.register((handler,client) -> ConfigFile.loadClientFiles(ConfigFile.LoadPhase.GAME_START));

		ClientPlayConnectionEvents.DISCONNECT.register(ClientBankData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientEjectionData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientNotificationData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientTeamData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientTraderData::onClientLogout);
		ClientPlayConnectionEvents.DISCONNECT.register(ClientWalletData::onClientLogout);

		ItemTooltipCallback.EVENT.register(this::appendTooltips);

	}

	private void appendTooltips(ItemStack stack, TooltipContext context, List<Text> lines) {
		if(MoneyUtil.isCoin(stack) && !(stack.getItem() instanceof CoinItem || stack.getItem() instanceof CoinBlockItem))
			CoinItem.addCoinTooltips(stack, lines);
	}


}
