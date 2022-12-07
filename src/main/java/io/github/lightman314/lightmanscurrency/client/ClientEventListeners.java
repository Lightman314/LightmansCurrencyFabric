package io.github.lightman314.lightmanscurrency.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.*;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageOpenWalletMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageSetWalletVisible;
import io.github.lightman314.lightmanscurrency.network.server.messages.walletslot.CMessageWalletInteraction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClientEventListeners {

    public static final Identifier WALLET_SLOT_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/wallet_slot.png");

    public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, KeyBinding.INVENTORY_CATEGORY);

    public static void init() {

        try{ KeyBindingHelper.registerKeyBinding(KEY_WALLET);
        } catch(Throwable ignored) {}

        ClientTickEvents.END_CLIENT_TICK.register(ClientEventListeners::onClientTick);
        ScreenEvents.AFTER_INIT.register(ClientEventListeners::onInventoryScreenInit);

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
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f + player.world.random.nextFloat() * 0.5f, 0.75f));
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
            screenWidgets.add(new WalletButton(gui));
            screenWidgets.add(new WalletVisibilityToggleButton(gui));

            //Add notification button(s)
            screenWidgets.add(new NotificationButton(gui));
            screenWidgets.add(new TeamManagerButton(gui));
            screenWidgets.add(new TraderRecoveryButton(gui));


            //Register screen-specific events
            ScreenExtensions extension = ScreenExtensions.getExtensions(screen);
            extension.fabric_getAfterRenderEvent().register(ClientEventListeners::renderInventoryScreen);
            extension.fabric_getAllowMouseClickEvent().register(ClientEventListeners::onInventoryClick);

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

    public static void renderInventoryScreen(Screen screen, MatrixStack pose, int mouseX, int mouseY, float tickDelta)
    {
        if(screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen)
        {
            if(screen instanceof CreativeInventoryScreen creativeScreen) {
                if(creativeScreen.getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                    return;
            }
            HandledScreen<?> gui = (HandledScreen<?>)screen;

            MinecraftClient client = Screens.getClient(screen);
            WalletHandler walletHandler = WalletHandler.getWallet(client.player);

            ScreenPosition slotPosition = screen instanceof CreativeInventoryScreen ? LCConfigClient.INSTANCE.walletSlotCreative.get() : LCConfigClient.INSTANCE.walletSlot.get();
            RenderSystem.setShaderTexture(0, WALLET_SLOT_TEXTURE);
            RenderSystem.setShaderColor(1f,1f,1f,1f);

            //Render slot background
            slotPosition = slotPosition.withOffset(ScreenUtil.getScreenCorner(gui));
            screen.drawTexture(pose, slotPosition.x, slotPosition.y, 0,0,18,18);
            //Render slot item
            ItemStack wallet = walletHandler.getWallet();
            slotPosition = slotPosition.withOffset(1,1);
            if(wallet.isEmpty())
                ItemRenderUtil.drawSlotBackground(pose, slotPosition.x, slotPosition.y, WalletSlot.BACKGROUND);
            else
                ItemRenderUtil.drawItemStack(screen, null, wallet, slotPosition.x, slotPosition.y);
            //Render slot highlight
            if(isMouseOverWalletSlot(mouseX, mouseY, slotPosition.withOffset(-1,-1)))
            {
                HandledScreen.drawSlotHighlight(pose, slotPosition.x, slotPosition.y, screen.getZOffset());

                //Render Inventory Tooltips
                if(!gui.getScreenHandler().getCursorStack().isEmpty())
                    return;

                if(!wallet.isEmpty())
                    screen.renderTooltip(pose, ItemRenderUtil.getTooltipFromItem(wallet), mouseX, mouseY);
            }

            //Render Notification & Team Manager Tooltips
            if(!gui.getScreenHandler().getCursorStack().isEmpty())
                return;

            NotificationButton.tryRenderTooltip(pose, mouseX, mouseY);
            TeamManagerButton.tryRenderTooltip(pose, mouseX, mouseY);
            TraderRecoveryButton.tryRenderTooltip(pose, mouseX, mouseY);
        }
    }

    private static boolean onInventoryClick(Screen screen, double mouseX, double mouseY, int button)
    {
        if(screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen)
        {
            HandledScreen<?> gui = (HandledScreen<?>) screen;
            if(gui instanceof CreativeInventoryScreen creativeScreen) {
                if(creativeScreen.getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                    return true;
            }

            ScreenPosition slotPosition = screen instanceof CreativeInventoryScreen ? LCConfigClient.INSTANCE.walletSlotCreative.get() : LCConfigClient.INSTANCE.walletSlot.get();
            slotPosition = slotPosition.withOffset(ScreenUtil.getScreenCorner(gui));

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
