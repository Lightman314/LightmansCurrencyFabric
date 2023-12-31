package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;

@Environment(EnvType.CLIENT)
public abstract class TraderStorageClientTab<T extends TraderStorageTab> implements ITab {

    public final TraderStorageScreen screen;
    public final TraderStorageMenu menu;
    public final T commonTab;
    public final TextRenderer font;

    protected TraderStorageClientTab(TraderStorageScreen screen, T commonTab) {
        this.screen = screen;
        this.menu = this.screen.getScreenHandler();
        this.commonTab = commonTab;
        MinecraftClient mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
    }

    @Override
    public int getColor() { return 0xFFFFFF; }

    /**
     * Whether the tab button for this tab should be visible. Used to hide the advanced trade tab from the screen, to only be opened when needed.
     */
    public abstract boolean tabButtonVisible();

    /**
     * Whether this tab being open should prevent the inventory button from closing the screen. Use this when typing is used on this tab.
     */
    public abstract boolean blockInventoryClosing();

    /**
     * The trade index of the trade that the trade rule button should open.
     */
    public int getTradeRuleTradeIndex() { return -1; }

    /**
     * Called when the tab is opened. Use this to initialize buttons/widgets and reset variables
     */
    public abstract void onOpen();

    /**
     * Called every container tick
     */
    public void tick() { }

    /**
     * Renders background data before the rendering of buttons/widgets and item slots
     */
    public abstract void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks);

    /**
     * Renders tooltips after the rendering of buttons/widgets and item slots
     */
    public abstract void renderTooltips(DrawContext gui, int mouseX, int mouseY);

    /**
     * Called when the mouse is clicked before any other click interactions are processed.
     * Return true an action was taken and other click interactions should be ignored.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }

    /**
     * Called when the mouse is clicked before any other click interactions are processed.
     * Return true an action was taken and other click interactions should be ignored.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }

    /**
     * Processes a client -> client message from another tab immediately after the tab was changed.
     */
    public void receiveSelfMessage(NbtCompound message) { }

    /**
     * Processes a server -> client message response to an action made on the client.
     */
    public void receiveServerMessage(NbtCompound message) { }

    /**
     * Called when the tab is closed.
     */
    public void onClose() { }

}