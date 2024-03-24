package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.callbacks.RenderInventoryCallback;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ChestCoinCollectButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.*;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.integration.trinketsapi.LCTrinketsAPI;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageOpenWalletMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageSetWalletVisible;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageWalletInteraction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class ClientEventListeners {

    public static final Identifier WALLET_SLOT_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/wallet_slot.png");

    public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, KeyBinding.INVENTORY_CATEGORY);

    public static void init() {

        try{ KeyBindingHelper.registerKeyBinding(KEY_WALLET);
        } catch(Throwable ignored) {}

        ClientTickEvents.END_CLIENT_TICK.register(ClientEventListeners::onClientTick);
        ScreenEvents.AFTER_INIT.register(ClientEventListeners::onInventoryScreenInit);
        RenderInventoryCallback.RENDER_BACKGROUND.register(ClientEventListeners::renderInventoryScreen);

        ModelLoadingRegistry.INSTANCE.registerModelProvider(ClientEventListeners::loadExtraModels);

    }

    private static void loadExtraModels(ResourceManager manager, Consumer<Identifier> consumer) {
        consumer.accept(SlotMachineBlock.LIGHT_MODEL_LOCATION);
    }

    //Manual detection of key pressing in the client tick
    private static void onClientTick(MinecraftClient client)
    {
        if(KEY_WALLET.wasPressed() && client.player != null && client.currentScreen == null)
        {
            ClientPlayerEntity player = client.player;
            new CMessageOpenWalletMenu(-1).sendToServer();
            ItemStack walletStack = WalletHandler.getWallet(player).getWallet();
            if(!walletStack.isEmpty())
            {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f + player.getWorld().random.nextFloat() * 0.5f, 0.75f));
                if(!WalletItem.isEmpty(walletStack))
                    client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.COINS_CLINKING, 1f, 0.4f));
            }
        }
    }

    private static void onInventoryScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight)
    {
        if(screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen)
        {
            HandledScreen<?> gui = (HandledScreen<?>) screen;

            //Add wallet slot related buttons
            List<ClickableWidget> screenWidgets = Screens.getButtons(screen);
            if(!LCTrinketsAPI.isValid(client.player))
            {
                screenWidgets.add(new WalletButton(gui));
                screenWidgets.add(new WalletVisibilityToggleButton(gui));
            }

            //Add notification button(s)
            screenWidgets.add(new NotificationButton(gui));
            screenWidgets.add(new TeamManagerButton(gui));
            screenWidgets.add(new TraderRecoveryButton(gui));

            //Register screen-specific events
            ScreenExtensions extension = ScreenExtensions.getExtensions(screen);
            extension.fabric_getAfterRenderEvent().register(ClientEventListeners::renderInventoryTooltips);
            extension.fabric_getAllowMouseClickEvent().register(ClientEventListeners::onInventoryClick);

        }
        if(screen instanceof GenericContainerScreen gui)
        {
            //Add Chest Button to the generic chest screens.
            List<ClickableWidget> screenWidgets = Screens.getButtons(screen);
            screenWidgets.add(new ChestCoinCollectButton(gui));
        }
    }

    public static void toggleWalletVisibility() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        WalletHandler handler = WalletHandler.getWallet(player);
        boolean nowVisibile = !handler.visible();
        handler.setVisible(nowVisibile);
        new CMessageSetWalletVisible(nowVisibile).sendToServer();
    }

    public static void renderInventoryScreen(Screen screen, DrawContext context, int mouseX, int mouseY, float tickDelta)
    {
        if(screen instanceof AbstractInventoryScreen<?> gui)
        {
            if(gui instanceof CreativeInventoryScreen creativeScreen) {
                if(!creativeScreen.isInventoryTabSelected())
                    return;
            }

            MinecraftClient client = Screens.getClient(screen);
            if(LCTrinketsAPI.isValid(client.player))
                return;
            WalletHandler walletHandler = WalletHandler.getWallet(client.player);

            ScreenPosition slotPosition = screen instanceof CreativeInventoryScreen ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get();
            context.setShaderColor(1f,1f,1f,1f);

            //Render slot background
            slotPosition = slotPosition.offset(ScreenUtil.getScreenCorner(gui));
            context.drawTexture(WALLET_SLOT_TEXTURE, slotPosition.x, slotPosition.y, 0,0,18,18);
            //Render slot item
            ItemStack wallet = walletHandler.getWallet();
            slotPosition = slotPosition.offset(1,1);
            if(wallet.isEmpty())
                ItemRenderUtil.drawSlotBackground(context, slotPosition.x, slotPosition.y, WalletSlot.BACKGROUND);
            else
                ItemRenderUtil.drawItemStack(context, null, wallet, slotPosition.x, slotPosition.y);

            //Render slot highlight
            if(isMouseOverWalletSlot(mouseX, mouseY, slotPosition.offset(-1,-1)))
                HandledScreen.drawSlotHighlight(context, slotPosition.x, slotPosition.y, 0);

        }
    }

    public static void renderInventoryTooltips(Screen screen, DrawContext context, int mouseX, int mouseY, float tickDelta)
    {
        if(screen instanceof AbstractInventoryScreen<?> gui)
        {
            if(screen instanceof CreativeInventoryScreen creativeScreen) {
                if(!creativeScreen.isInventoryTabSelected())
                    return;
            }

            MinecraftClient client = Screens.getClient(screen);
            if(!LCTrinketsAPI.isValid(client.player))
            {
                WalletHandler walletHandler = WalletHandler.getWallet(client.player);
                ItemStack wallet = walletHandler.getWallet();

                ScreenPosition slotPosition = screen instanceof CreativeInventoryScreen ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get();
                slotPosition = slotPosition.offset(ScreenUtil.getScreenCorner(gui));

                //Render slot tooltip
                if(isMouseOverWalletSlot(mouseX, mouseY, slotPosition.offset(-1,-1)))
                {
                    if(!wallet.isEmpty())
                        context.drawTooltip(client.textRenderer, ItemRenderUtil.getTooltipFromItem(wallet), mouseX, mouseY);
                }
            }

            //Render Notification & Team Manager Tooltips
            NotificationButton.tryRenderTooltip(context, mouseX, mouseY);
            TeamManagerButton.tryRenderTooltip(context, mouseX, mouseY);
            TraderRecoveryButton.tryRenderTooltip(context, mouseX, mouseY);

        }
    }

    private static boolean onInventoryClick(Screen screen, double mouseX, double mouseY, int button)
    {
        if(screen instanceof AbstractInventoryScreen<?> gui)
        {
            if(gui instanceof CreativeInventoryScreen creativeScreen) {
                if(!creativeScreen.isInventoryTabSelected())
                    return true;
            }

            if(LCTrinketsAPI.isValid(Screens.getClient(screen).player))
                return true;

            ScreenPosition slotPosition = screen instanceof CreativeInventoryScreen ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get();
            slotPosition = slotPosition.offset(ScreenUtil.getScreenCorner(gui));

            //Wallet Slot click detection
            if(isMouseOverWalletSlot(mouseX, mouseY, slotPosition) && !isMouseOverVisibilityButton(mouseX, mouseY, slotPosition))
            {
                ItemStack heldStack = gui.getScreenHandler().getCursorStack().copy();
                boolean shiftHeld = Screen.hasShiftDown() && !(gui instanceof CreativeInventoryScreen);
                if(gui instanceof CreativeInventoryScreen) //If creative, run interaction on the client as well, so that the wallet goes into their hand/
                    WalletHandler.WalletSlotInteraction(MinecraftClient.getInstance().player, -1, false, heldStack);
                new CMessageWalletInteraction(-1, shiftHeld, heldStack).sendToServer();
            }
            else if(Screen.hasShiftDown() && !(gui instanceof CreativeInventoryScreen))
            {
                Slot hoveredSlot = ScreenUtil.getFocusedSlot(gui);
                if(hoveredSlot != null)
                {
                    MinecraftClient client = Screens.getClient(screen);
                    PlayerEntity player = client.player;
                    if(player != null)
                    {
                        int slotIndex = hoveredSlot.inventory != player.getInventory() ? -1 : hoveredSlot.getIndex();
                        if(slotIndex < 0)
                            return true;
                        ItemStack slotItem = player.getInventory().getStack(slotIndex);
                        if(WalletSlot.isValidWallet(slotItem))
                        {
                            ItemStack heldStack = gui.getScreenHandler().getCursorStack().copy();
                            new CMessageWalletInteraction(slotIndex, true, heldStack).sendToServer();
                            //Cancel event
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    private static boolean isMouseOverWalletSlot(double mouseX, double mouseY, ScreenPosition slotPosition)
    {
        return mouseX >= slotPosition.x && mouseX < slotPosition.x + 18 && mouseY >= slotPosition.y && mouseY < slotPosition.y + 18;
    }

    private static boolean isMouseOverVisibilityButton(double mouseX, double mouseY, ScreenPosition slotPosition)
    {
        return mouseX >= slotPosition.x && mouseX < slotPosition.x + WalletVisibilityToggleButton.SIZE && mouseY >= slotPosition.y && mouseY < slotPosition.y + WalletVisibilityToggleButton.SIZE;
    }

}
