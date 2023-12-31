package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemEditWidget extends ClickableWidget implements IScrollable{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/item_edit.png");

    private static final List<Function<ItemGroup,Boolean>> ITEM_GROUP_BLACKLIST = new ArrayList<>();

    public static void BlacklistCreativeTabs(ItemGroup... tabs) {
        for(ItemGroup tab : tabs)
            BlacklistCreativeTab(t -> tab == t);
    }

    @SafeVarargs
    public static void BlacklistCreativeTabs(RegistryKey<ItemGroup>... tabs) {
        for(RegistryKey<ItemGroup> tab : tabs)
            BlacklistCreativeTab(t -> Registries.ITEM_GROUP.get(tab) == t);
    }

    public static void BlacklistCreativeTab(Function<ItemGroup,Boolean> tabMatcher) {
        if(!ITEM_GROUP_BLACKLIST.contains(tabMatcher))
            ITEM_GROUP_BLACKLIST.add(tabMatcher);
    }

    public static boolean IsCreativeTabAllowed(ItemGroup tab) {
        for(Function<ItemGroup,Boolean> test : ITEM_GROUP_BLACKLIST)
        {
            if(test.apply(tab))
                return false;
        }
        return true;
    }
    private int scroll = 0;
    private int stackCount = 1;

    private final int columns;
    private final int rows;

    public int searchOffX;
    public int searchOffY;

    public int stackSizeOffX;
    public int stackSizeOffY;

    private static final List<ItemStack> allItems = new ArrayList<>();

    //private List<ItemStack> filteredResultItems;
    private List<ItemStack> searchResultItems;

    private String searchString;

    TextFieldWidget searchInput;
    ScrollListener stackScrollListener;
    private final IItemEditListener listener;

    private final TextRenderer font;

    public ItemEditWidget(int x, int y, int columns, int rows, IItemEditListener listener) {
        super(x, y, columns * 18, rows * 18, Text.empty());
        this.listener = listener;

        this.columns = columns;
        this.rows = rows;

        this.searchOffX = this.width - 90;
        this.searchOffY = -13;

        this.stackSizeOffX = this.width + 13;
        this.stackSizeOffY = 0;

        MinecraftClient mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;

        //Set the search to the default value to initialize the inventory
        this.modifySearch("");

    }

    public static void initItemList() {

        //Don't confirm that the list has already been initialized, as the registered items/mods may have changed because Fabric.
        allItems.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if(player == null)
            return;

        ItemGroups.updateDisplayContext(player.networkHandler.getEnabledFeatures(), client.options.getOperatorItemsTab().getValue(), player.clientWorld.getRegistryManager());

        //Go through all the item groups to avoid allowing sales of hidden items
        for(ItemGroup group : Registries.ITEM_GROUP.stream().toList())
        {
            if(IsCreativeTabAllowed(group))
            {
                //Get all the items in this group
                Collection<ItemStack> items = group.getDisplayStacks();

                //Add them to the list after confirming we don't already have it in the list
                for(ItemStack stack : items)
                {

                    if(!itemListAlreadyContains(stack))
                        allItems.add(stack);

                    if(stack.getItem() == Items.ENCHANTED_BOOK)
                    {
                        //LightmansCurrency.LogInfo("Attempting to add lower levels of an enchanted book.");
                        Map<Enchantment,Integer> enchantments = EnchantmentHelper.get(stack);
                        enchantments.forEach((enchantment, level) ->{
                            for(int newLevel = level - 1; newLevel > 0; newLevel--)
                            {
                                ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantmentHelper.set(ImmutableMap.of(enchantment, newLevel), newBook);
                                if(!itemListAlreadyContains(newBook))
                                    allItems.add(newBook);
                            }
                        });
                    }

                }
            }
        }
    }

    private static boolean itemListAlreadyContains(ItemStack stack)
    {
        for(ItemStack s : allItems)
        {
            if(InventoryUtil.ItemMatches(s, stack))
                return true;
        }
        return false;
    }

    private List<ItemStack> getFilteredItems()
    {
        List<ItemStack> results = Lists.newArrayList();
        ItemTradeData trade = this.listener.getTrade();
        ItemTradeRestriction restriction = trade == null ? ItemTradeRestriction.NONE : this.listener.getTrade().getRestriction();
        for (ItemStack allItem : allItems) {
            if (restriction.allowItemSelectItem(allItem))
                results.add(allItem);
        }
        return results;
    }

    public int getMaxScroll()
    {
        return Math.max(((this.searchResultItems.size() - 1) / this.columns) - this.rows + 1, 0);
    }

    public void refreshPage()
    {

        if(this.scroll < 0)
            this.scroll = 0;
        if(this.scroll > this.getMaxScroll())
            this.scroll = this.getMaxScroll();

        //LightmansCurrency.LogInfo("Refreshing page " + this.page + ". Max Page: " + maxPage());

        int startIndex = this.scroll * this.columns;
        //Define the display inventories contents
        for(int i = 0; i < this.rows * this.columns; i++)
        {
            int thisIndex = startIndex + i;
            if(thisIndex < this.searchResultItems.size()) //Set to search result item
            {
                ItemStack stack = this.searchResultItems.get(thisIndex).copy();
                stack.setCount(MathUtil.clamp(this.stackCount, 1, stack.getMaxCount()));
            }
        }
    }

    public void refreshSearch() { this.modifySearch(this.searchString); }

    public void modifySearch(String newSearch)
    {
        this.searchString = newSearch.toLowerCase();

        //Repopulate the searchResultItems list
        if(this.searchString.length() > 0)
        {
            this.searchResultItems = new ArrayList<>();
            List<ItemStack> validItems = this.listener.restrictItemEditItems() ? this.getFilteredItems() : allItems;
            for(ItemStack stack : validItems)
            {
                //Search the display name
                if(stack.getName().getString().toLowerCase().contains(this.searchString))
                {
                    this.searchResultItems.add(stack);
                }
                //Search the registry name
                else if(Registries.ITEM.getId(stack.getItem()).toString().contains(this.searchString))
                {
                    this.searchResultItems.add(stack);
                }
                //Search the enchantments?
                else
                {
                    AtomicReference<Boolean> enchantmentMatch = new AtomicReference<>(false);
                    Map<Enchantment,Integer> enchantments = EnchantmentHelper.get(stack);
                    enchantments.forEach((enchantment, level) ->{
                        if(Registries.ENCHANTMENT.getId(enchantment).toString().contains(this.searchString))
                            enchantmentMatch.set(true);
                        else if(enchantment.getName(level).getString().toLowerCase().contains(this.searchString))
                            enchantmentMatch.set(true);
                    });
                    if(enchantmentMatch.get())
                        this.searchResultItems.add(stack);
                }
            }
        }
        else //No search string, so the result is just the allItems list
        {
            this.searchResultItems = this.listener.restrictItemEditItems() ? this.getFilteredItems() : allItems;
        }

        //Run refresh page code to validate the page # and repopulate the display inventory
        this.refreshPage();

    }

    public void init(Function<TextFieldWidget,TextFieldWidget> addWidget, Function<ScrollListener,ScrollListener> addListener) {

        this.searchInput = addWidget.apply(new TextFieldWidget(this.font, this.getX() + this.searchOffX + 2, this.getY() + this.searchOffY + 2, 79, 9, Text.translatable("gui.lightmanscurrency.item_edit.search")));
        this.searchInput.setDrawsBackground(false);
        this.searchInput.setMaxLength(32);
        this.searchInput.setEditableColor(0xFFFFFF);

        this.stackScrollListener = addListener.apply(new ScrollListener(this.getX() + this.stackSizeOffX, this.getY() + this.stackSizeOffY, 18, 18, this::stackCountScroll));

    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.searchInput.visible = this.visible;
        this.stackScrollListener.active = this.visible;

        super.render(gui, mouseX, mouseY, partialTicks);

    }

    @Override
    protected void renderButton(DrawContext gui, int mouseX, int mouseY, float delta) {
        if(!this.searchInput.getText().toLowerCase().contentEquals(this.searchString))
            this.modifySearch(this.searchInput.getText());

        int index = this.scroll * this.columns;
        for(int y = 0; y < this.rows && index < this.searchResultItems.size(); ++y)
        {
            int yPos = this.getY() + y * 18;
            for(int x = 0; x < this.columns && index < this.searchResultItems.size(); ++x)
            {
                //Get the slot position
                int xPos = this.getX() + x * 18;
                //Render the slot background
                gui.setShaderColor(1f, 1f, 1f, 1f);
                gui.drawTexture(GUI_TEXTURE, xPos, yPos, 0, 0, 18, 18);
                //Render the slots item
                ItemRenderUtil.drawItemStack(gui, this.font, this.getQuantityFixedStack(this.searchResultItems.get(index)), xPos + 1, yPos + 1);
                index++;
            }
        }

        //Render the search field
        gui.setShaderColor(1f, 1f, 1f, 1f);
        gui.drawTexture(GUI_TEXTURE, this.getX() + this.searchOffX, this.getY() + this.searchOffY, 18, 0, 90, 12);

        //Render the quantity scroll area
        gui.drawTexture(GUI_TEXTURE, this.getX() + this.stackSizeOffX, this.getY() + this.stackSizeOffY, 108, 0, 18, 18);

    }

    public void tick() { this.searchInput.tick(); }

    private ItemStack getQuantityFixedStack(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(stack.getMaxCount(), this.stackCount));
        return copy;
    }

    public void renderTooltips(DrawContext gui, TextRenderer font, int mouseX, int mouseY) {
        if(!this.visible)
            return;
        int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
        if(hoveredSlot >= 0)
        {
            hoveredSlot += this.scroll * this.columns;
            if(hoveredSlot < this.searchResultItems.size())
            {
                gui.drawTooltip(font, ItemRenderUtil.getTooltipFromItem(this.searchResultItems.get(hoveredSlot)), mouseX, mouseY);
            }
        }
        if(this.isMouseOverStackSizeScroll(mouseX,mouseY))
            gui.drawTooltip(font, Text.translatable("tooltip.lightmanscurrency.item_edit.scroll"), mouseX, mouseY);
    }

    private boolean isMouseOverStackSizeScroll(int mouseX, int mouseY) {
        return mouseX >= this.getX() + this.stackSizeOffX && mouseX < this.getX() + this.stackSizeOffX + 18 && mouseY >= this.getY() + this.stackSizeOffY && mouseY < this.getY() + this.stackSizeOffY + 18;
    }

    private int isMouseOverSlot(double mouseX, double mouseY) {

        int foundColumn = -1;
        int foundRow = -1;

        for(int x = 0; x < this.columns && foundColumn < 0; ++x)
        {
            if(mouseX >= this.getX() + x * 18 && mouseX < this.getX() + (x * 18) + 18)
                foundColumn = x;
        }
        for(int y = 0; y < this.rows && foundRow < 0; ++y)
        {
            if(mouseY >= this.getY() + y * 18 && mouseY < this.getY() + (y * 18) + 18)
                foundRow = y;
        }
        if(foundColumn < 0 || foundRow < 0)
            return -1;
        return (foundRow * this.columns) + foundColumn;
    }

    public interface IItemEditListener {
        ItemTradeData getTrade();
        boolean restrictItemEditItems();
        void onItemClicked(ItemStack item);
    }


    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
        if(hoveredSlot >= 0)
        {
            hoveredSlot += this.scroll * this.columns;
            if(hoveredSlot < this.searchResultItems.size())
            {
                ItemStack stack = this.getQuantityFixedStack(this.searchResultItems.get(hoveredSlot));
                this.listener.onItemClicked(stack);
                return true;
            }
        }
        return false;
    }

    public boolean stackCountScroll(double mouseX, double mouseY, double delta) {
        if(delta > 0)
        {
            if(this.stackCount < 64)
                this.stackCount++;
        }
        else if(delta < 0)
        {
            if(this.stackCount > 1)
                this.stackCount--;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(delta < 0)
        {
            if(this.scroll < this.getMaxScroll())
                this.scroll++;
            else
                return false;
        }
        else if(delta > 0)
        {
            if(this.scroll > 0)
                this.scroll--;
            else
                return false;
        }
        return true;
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        this.refreshPage();
    }

}