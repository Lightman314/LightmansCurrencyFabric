package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinData;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CoinValueInput extends ClickableWidget implements ScrollBarWidget.IScrollable {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");

    public static final int HEIGHT = 69;
    public static final int DISPLAY_WIDTH = 176;

    public static final int MAX_BUTTON_COUNT = 6;

    private static final int SEGMENT_WIDTH = 20;
    private static final int SEGMENT_SPACING = 5;
    private static final int SEGMENT_TOTAL = SEGMENT_WIDTH + SEGMENT_SPACING;

    private final CoinValue.ValueType inputType;

    private final TextRenderer font;
    private final Consumer<CoinValue> onValueChanged;
    private final Consumer<ClickableWidget> addWidget;

    ButtonWidget buttonLeft;
    ButtonWidget buttonRight;

    private CoinValue coinValue;
    ButtonWidget toggleFree;
    private List<ButtonWidget> increaseButtons;
    private List<ButtonWidget> decreaseButtons;
    private Text title;
    public void setTitle(Text title) {this.title = title; }

    String lastInput = "";
    TextFieldWidget valueInput;
    String prefix;
    String postfix;

    public boolean allowFreeToggle = true;

    public boolean drawBG = true;

    public boolean locked = false;

    int scroll = 0;
    List<CoinData> coinData = new ArrayList<>();

    @Deprecated
    public CoinValueInput(int y, Text title, CoinValue startingValue, @NotNull ICoinValueInput parent) {

        super((parent.getWidth() - DISPLAY_WIDTH) /  2, y, DISPLAY_WIDTH, HEIGHT, title);

        this.inputType = LCConfig.SERVER.coinValueInputType.get();
        this.title = title;

        this.coinValue = startingValue.copy();

        this.font = parent.getFont();
        this.onValueChanged = v -> parent.OnCoinValueChanged(this);
        this.addWidget = parent::addCustomWidget;


        if(this.inputType == CoinValue.ValueType.VALUE)
            this.setPrefixAndPostfix();

    }

    public CoinValueInput(int x, int y, Text title, CoinValue startingValue, TextRenderer font, Consumer<CoinValue> onValueChanged, Consumer<ClickableWidget> addWidget) {
        super(x, y, DISPLAY_WIDTH, HEIGHT, title);

        this.inputType = LCConfig.SERVER.coinValueInputType.get();
        this.title = title;

        this.font = font;
        this.onValueChanged = onValueChanged;
        this.addWidget = addWidget;

        this.coinValue = startingValue.copy();

        if(this.inputType == CoinValue.ValueType.VALUE)
            this.setPrefixAndPostfix();
        else
            this.getCoinData();

    }

    private void setPrefixAndPostfix()
    {
        String format = LCConfig.SERVER.valueFormat.get();
        //Have to replace the {value} with a non-illegal character in order to split the string
        String[] splitFormat = format.replace("{value}", "`").split("`",2);
        if(splitFormat.length < 2)
        {
            //Determine which is the prefix, and which is the postfix
            if(format.startsWith("{value}"))
            {
                prefix = "";
                postfix = splitFormat[0];
            }
            else
            {
                prefix = splitFormat[0];
                postfix = "";
            }
        }
        else
        {
            prefix = splitFormat[0];
            postfix = splitFormat[1];
        }

    }

    private void getCoinData() {
        this.coinData = MoneyUtil.getAllData(MoneyUtil.MAIN_CHAIN);
    }

    public void init()
    {
        this.toggleFree = new PlainButton(this.getX() + this.width - 14, this.getY() + 4, 10, 10, this::ToggleFree, GUI_TEXTURE, 40, HEIGHT);
        this.addWidget.accept(this.toggleFree);
        this.increaseButtons = new ArrayList<>();
        this.decreaseButtons = new ArrayList<>();
        //Initialize default button setup
        if(this.inputType == CoinValue.ValueType.DEFAULT)
        {
            int buttonCount = this.coinData.size();
            if(buttonCount > MAX_BUTTON_COUNT)
            {
                buttonCount = MAX_BUTTON_COUNT;
                this.buttonLeft = new PlainButton(this.getX() + 4, this.getY() + 29, 10, 20, b -> this.scrollLeft(), GUI_TEXTURE, 50, HEIGHT);
                this.buttonLeft.visible = false;
                this.addWidget.accept(this.buttonLeft);
                this.buttonRight = new PlainButton(this.getX() + this.width - 14, this.getY() + 29, 10, 20, b -> this.scrollRight(), GUI_TEXTURE, 60, HEIGHT);
                this.addWidget.accept(this.buttonRight);
            }
            int startX = this.getStartX();
            for(int x = 0; x < buttonCount; x++)
            {
                ButtonWidget newButton = new PlainButton(startX + (x * SEGMENT_TOTAL), this.getY() + 15, 20, 10, this::IncreaseButtonHit, GUI_TEXTURE, 0, HEIGHT);
                this.addWidget.accept(newButton);
                newButton.active = true;
                increaseButtons.add(newButton);
                newButton = new PlainButton(startX + (x * SEGMENT_TOTAL), this.getY() + 53, 20, 10, this::DecreaseButtonHit, GUI_TEXTURE, 20, HEIGHT);
                this.addWidget.accept(newButton);
                newButton.active = false;
                decreaseButtons.add(newButton);
            }
        }
        else
        {
            //Value input
            int prefixWidth = this.font.getWidth(this.prefix);
            if(prefixWidth > 0)
                prefixWidth += 2;
            int postfixWidth = this.font.getWidth(this.postfix);
            if(postfixWidth > 0)
                postfixWidth += 2;
            this.valueInput = new TextFieldWidget(this.font, this.getX() + 10 + prefixWidth, this.getY() + 20, DISPLAY_WIDTH - 20 - prefixWidth - postfixWidth, 20, Text.empty());
            this.addWidget.accept(this.valueInput);
        }
        this.tick();
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        //Match the buttons visibility to our visibility.
        this.toggleFree.visible = this.allowFreeToggle && this.visible;
        this.increaseButtons.forEach(button -> button.visible = this.visible);
        this.decreaseButtons.forEach(button -> button.visible = this.visible);
        if(this.valueInput != null)
            this.valueInput.visible = this.visible;
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderButton(DrawContext gui, int mouseX, int mouseY, float delta) {

        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        gui.setShaderColor(1f,  1f,  1f, 1f);

        if(this.drawBG)
        {
            //Render the background
            gui.drawTexture(GUI_TEXTURE, this.getX(), this.getY(), 0, 0, DISPLAY_WIDTH, HEIGHT);
        }

        if(this.inputType == CoinValue.ValueType.DEFAULT)
        {

            this.validateScroll();

            int buttonCount = Math.min(coinData.size(), MAX_BUTTON_COUNT);

            //Draw the coins initial & sprite
            int startX = this.getStartX();
            for(int x = 0; x < buttonCount; x++)
            {
                //Draw sprite
                ItemRenderUtil.drawItemStack(gui, this.font, new ItemStack(this.coinData.get(x + this.scroll).coinItem), startX + (x * SEGMENT_TOTAL) + 2, this.getY() + 26);
                //Draw string
                String countString = String.valueOf(this.coinValue.getEntry(this.coinData.get(x + this.scroll).coinItem));
                int width = this.font.getWidth(countString);
                gui.drawText(this.font, countString, startX + (x * SEGMENT_TOTAL) + 10 - (width / 2), this.getY() + 43, 0x404040, false);

            }
        }
        //Draw background for the text input field variant
        else if(this.inputType == CoinValue.ValueType.VALUE)
        {

            //Draw the prefix and postfix
            gui.drawText(this.font, this.prefix, this.getX() + 10, this.getY() + 26, 0xFFFFFF, false);
            int postfixWidth = this.font.getWidth(this.postfix);
            gui.drawText(this.font, this.postfix, this.getX() + DISPLAY_WIDTH - 10 - postfixWidth, this.getY() + 26, 0xFFFFFF, false);

        }

        //Render the current price in the top-right corner
        int priceWidth = this.font.getWidth(this.coinValue.getString());
        int freeButtonOffset = this.allowFreeToggle ? 15 : 5;
        gui.drawText(this.font, this.coinValue.getComponent(), this.getX() + this.width - freeButtonOffset - priceWidth, this.getY() + 5, 0x404040, false);

        //Render the title
        int titleWidth = this.width - 7 - freeButtonOffset - priceWidth;
        gui.drawText(this.font, TextRenderUtil.fitString(this.title, titleWidth), this.getX() + 8, this.getY() + 5, 0x404040, false);

        gui.setShaderColor(1f,1f,1f,1f);

    }

    private int getStartX() {
        int buttonCount = Math.min(this.coinData.size(), MAX_BUTTON_COUNT);
        int space = this.width - (buttonCount * SEGMENT_TOTAL) + SEGMENT_SPACING;
        return this.getX() + space / 2;
    }

    public void tick()
    {
        //Set the decrease buttons as inactive if their value is 0;
        this.toggleFree.active = !this.locked;
        if(this.inputType == CoinValue.ValueType.DEFAULT)
        {
            List<Item> coinItems = MoneyUtil.getAllCoins();
            for(int i = 0; i < decreaseButtons.size(); i++)
            {
                if(i + this.scroll >= coinItems.size())
                    decreaseButtons.get(i).active = false;
                else
                    decreaseButtons.get(i).active = this.coinValue.getEntry(coinItems.get(i + this.scroll)) > 0 && !this.locked;
            }
            for (ButtonWidget increaseButton : this.increaseButtons)
                increaseButton.active = !this.coinValue.isFree() && !this.locked;

            if(this.buttonLeft != null)
                this.buttonLeft.visible = this.scroll > 0;
            if(this.buttonRight != null)
                this.buttonRight.visible = this.scroll < this.getMaxScroll();
        }
        else if(this.valueInput != null)
        {
            this.valueInput.tick();
            this.valueInput.active = !this.coinValue.isFree() && !this.locked;
            if(!this.coinValue.isFree())
            {
                TextInputUtil.whitelistFloat(this.valueInput);
                if(!this.lastInput.contentEquals(this.valueInput.getText()))
                {
                    this.lastInput = this.valueInput.getText();
                    this.coinValue = MoneyUtil.displayValueToCoinValue(this.getDisplayValue());
                    this.onValueChanged.accept(this.coinValue);
                }
            }
            else
            {
                this.valueInput.setText("");
                this.lastInput = this.valueInput.getText();
            }
        }

    }

    public void IncreaseButtonHit(ButtonWidget button)
    {
        if(!this.increaseButtons.contains(button))
            return;

        int coinIndex = this.increaseButtons.indexOf(button);
        if(coinIndex < 0)
            return;

        coinIndex += this.scroll;

        if(coinIndex >= 0 && coinIndex < this.coinData.size())
        {
            Item coin = this.coinData.get(coinIndex).coinItem;
            int addAmount = 1;
            if(Screen.hasShiftDown())
                addAmount = getLargeIncreaseAmount(coin);
            if(Screen.hasControlDown())
                addAmount *= 10;
            this.coinValue.addValue(coin, addAmount);
            this.onValueChanged.accept(this.coinValue);
        }
        else
            LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the increasing button.");
    }

    public void DecreaseButtonHit(ButtonWidget button)
    {
        if(!this.decreaseButtons.contains(button))
            return;

        int coinIndex = this.decreaseButtons.indexOf(button);
        if(coinIndex < 0)
            return;

        coinIndex += this.scroll;

        if(coinIndex >= 0 && coinIndex < this.coinData.size())
        {
            Item coin = this.coinData.get(coinIndex).coinItem;
            int removeAmount = 1;
            if(Screen.hasShiftDown())
                removeAmount = getLargeIncreaseAmount(coin);
            if(Screen.hasControlDown())
                removeAmount *= 10;
            //LightmansCurrency.LOGGER.info("Removing " + (Screen.hasShiftDown() ? 5 : 1) + " coins of type '" + MoneyUtil.getAllCoins().get(coinIndex).getRegistryName().toString() + "' from the input value.");
            this.coinValue.removeValue(coin, removeAmount);
            this.onValueChanged.accept(this.coinValue);
        }
        else
            LightmansCurrency.LogError("Invalid index (" + coinIndex + ") found for the decreasing button.");
    }

    private int getLargeIncreaseAmount(Item coinItem)
    {
        Pair<Item,Integer> upwardConversion = MoneyUtil.getUpwardConversion(coinItem);
        if(upwardConversion != null)
            return getLargeAmount(upwardConversion);
        else
        {
            Pair<Item,Integer> downwardConversion = MoneyUtil.getDownwardConversion(coinItem);
            if(downwardConversion != null)
                return getLargeAmount(downwardConversion);
            //No conversion found for this coin. Assume 10;
            return 10;
        }
    }

    private int getLargeAmount(Pair<Item,Integer> conversion)
    {
        if(conversion.getSecond() >= 64)
            return 16;
        if(conversion.getSecond() > 10)
            return 10;
        if(conversion.getSecond() > 5)
            return 5;
        return 2;
    }

    private void ToggleFree(ButtonWidget button)
    {
        this.coinValue.setFree(!this.coinValue.isFree());
        this.onValueChanged.accept(this.coinValue);
    }

    public CoinValue getCoinValue()
    {
        return this.coinValue;
    }

    public double getDisplayValue()
    {
        if(this.valueInput != null)
        {
            return TextInputUtil.getDoubleValue(this.valueInput);
        }
        return this.coinValue.getDisplayValue();
    }

    public void setCoinValue(CoinValue newValue)
    {
        this.coinValue = newValue.copy();
        if(this.inputType == CoinValue.ValueType.VALUE)
            this.valueInput.setText(LCConfig.SERVER.formatValueOnly(newValue.getDisplayValue()));
    }

    @Deprecated
    public interface ICoinValueInput
    {
        <T extends Element & Drawable & Selectable> T addCustomWidget(T button);
        int getWidth();
        TextRenderer getFont();
        void OnCoinValueChanged(CoinValueInput input);
    }

    private void scrollLeft() {
        this.scroll--;
        this.validateScroll();
    }

    private void scrollRight() {
        this.scroll++;
        this.validateScroll();
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder narrator) { }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }

    @Override
    public int currentScroll() {
        return this.scroll;
    }

    @Override
    public void setScroll(int newScroll) {
        this.scroll = Math.max(newScroll, this.getMaxScroll());
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.coinData.size() - MAX_BUTTON_COUNT);
    }

    private void validateScroll() { this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll()); }

}