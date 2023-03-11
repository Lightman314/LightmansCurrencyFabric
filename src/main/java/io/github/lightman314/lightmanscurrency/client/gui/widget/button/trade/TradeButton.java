package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.AlertData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


public class TradeButton extends ButtonWidget {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/trade.png");
    public static final PressAction NULL_PRESS = button -> {};

    public static  final int ARROW_WIDTH = 22;
    public static  final int ARROW_HEIGHT = 18;

    public static  final int TEMPLATE_WIDTH = 212;

    public static final int BUTTON_HEIGHT = 18;

    private final Supplier<TradeData> tradeSource;
    public TradeData getTrade() { return this.tradeSource.get(); }
    private Supplier<TradeContext> contextSource;
    public TradeContext getContext() { return this.contextSource.get(); }
    public boolean displayOnly = false;

    public TradeButton(Supplier<TradeContext> contextSource, Supplier<TradeData> tradeSource, PressAction onPress) {
        super(0, 0, 0, BUTTON_HEIGHT, EasyText.empty(), onPress);
        this.tradeSource = tradeSource;
        this.contextSource = contextSource;
        this.recalculateSize();
    }

    private void recalculateSize()
    {
        TradeData trade = this.getTrade();
        if(trade != null)
        {
            TradeContext context = this.getContext();
            this.width = trade.tradeButtonWidth(context);
        }
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void renderButton(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {

        TradeData trade = this.getTrade();
        if(trade == null)
            return;

        TradeContext context = this.getContext();

        this.recalculateSize();

        this.renderBackground(pose, context.isStorageMode || this.displayOnly ? false : this.hovered);

        try {
            trade.renderAdditional(this, pose, mouseX, mouseY, context);
        } catch(Exception e) { LightmansCurrency.LogError("Error on additional Trade Button rendering.", e); }

        if(trade.hasArrow(context))
            this.renderArrow(pose, trade.arrowPosition(context), context.isStorageMode || this.displayOnly ? false : this.hovered);

        this.renderAlert(pose, trade.alertPosition(context), trade.getAlertData(context));

        this.renderDisplays(pose, trade, context);

    }

    private void renderBackground(MatrixStack pose, boolean isHovered)
    {
        if(this.width < 8)
        {
            LightmansCurrency.LogError("Cannot render a trade button that is less than 8 pixels wide!");
            return;
        }
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        if(this.active)
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        else
            RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);

        int vOffset = isHovered ? BUTTON_HEIGHT : 0;

        //Render the left
        this.drawTexture(pose, this.x, this.y, 0, vOffset, 4, BUTTON_HEIGHT);
        //Render the middle
        int xOff = 4;
        while(xOff < this.width - 4)
        {
            int xRend = Math.min(this.width - 4 - xOff, TEMPLATE_WIDTH - 8);
            this.drawTexture(pose, this.x + xOff, this.y, 4, vOffset, xRend, BUTTON_HEIGHT);
            xOff += xRend;
        }
        //Render the right
        this.drawTexture(pose, this.x + this.width - 4, this.y, TEMPLATE_WIDTH - 4, vOffset, 4, BUTTON_HEIGHT);
    }

    private void renderArrow(MatrixStack pose, Pair<Integer,Integer> position, boolean isHovered)
    {

        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        if(this.active)
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        else
            RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);

        int vOffset = isHovered ? ARROW_HEIGHT : 0;

        this.drawTexture(pose, this.x + position.getFirst(), this.y + position.getSecond(), TEMPLATE_WIDTH, vOffset, ARROW_WIDTH, ARROW_HEIGHT);

    }

    private void renderAlert(MatrixStack pose, Pair<Integer,Integer> position, List<AlertData> alerts)
    {

        if(alerts == null || alerts.size() <= 0)
            return;
        alerts.sort(AlertData::compare);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        alerts.get(0).setShaderColor(this.active ? 1f : 0.5f);

        this.drawTexture(pose, this.x + position.getFirst(), this.y + position.getSecond(), TEMPLATE_WIDTH + ARROW_WIDTH, 0, ARROW_WIDTH, ARROW_HEIGHT);

    }

    public void renderDisplays(MatrixStack pose, TradeData trade, TradeContext context)
    {
        for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(trade, context))
            display.getFirst().render(this, pose, this.x, this.y, display.getSecond());
        for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(trade, context))
            display.getFirst().render(this, pose, this.x, this.y, display.getSecond());
    }

    public void renderTooltips(MatrixStack pose, int mouseX, int mouseY)
    {
        if(!this.visible || !this.isMouseOver(mouseX, mouseY))
            return;

        TradeData trade = tradeSource.get();
        if(trade == null)
            return;

        TradeContext context = this.getContext();

        List<Text> tooltips = new ArrayList<>();

        this.tryAddTooltip(tooltips, trade.getAdditionalTooltips(context, mouseX - this.x, mouseY - this.y));

        for(Pair<DisplayEntry,DisplayData> display : getInputDisplayData(trade, context))
        {
            if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
                this.tryAddTooltip(tooltips, display.getFirst().getTooltip());
        }

        for(Pair<DisplayEntry,DisplayData> display : getOutputDisplayData(trade, context))
        {
            if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
            {
                this.tryAddTooltip(tooltips, display.getFirst().getTooltip());
            }
        }

        if(this.isMouseOverAlert(mouseX, mouseY, trade, context) && trade.hasAlert(context))
            this.tryAddAlertTooltips(tooltips, trade.getAlertData(context));

        DrawTooltip(pose, mouseX, mouseY, tooltips);

    }

    private void tryAddTooltip(List<Text> tooltips, @Nullable List<Text> add)
    {
        if(add == null)
            return;
        tooltips.addAll(add);
    }

    private void tryAddAlertTooltips(List<Text> tooltips, @Nullable List<AlertData> alerts)
    {
        if(alerts == null)
            return;
        alerts.sort(AlertData::compare);
        for(AlertData alert : alerts)
            tooltips.add(alert.getFormattedMessage());
    }

    public void onInteractionClick(int mouseX, int mouseY, int button, InteractionConsumer consumer)
    {
        if(!this.visible || !this.isMouseOver(mouseX, mouseY))
            return;

        TradeData trade = tradeSource.get();
        if(trade == null)
            return;

        TradeContext context = this.getContext();

        List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, context);
        for(int i = 0; i < inputDisplays.size(); ++i)
        {
            Pair<DisplayEntry,DisplayData> display = inputDisplays.get(i);
            if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
            {
                consumer.onTradeButtonInputInteraction(context.getTrader(), trade, i, button);
                return;
            }
        }

        List<Pair<DisplayEntry,DisplayData>> outputDisplays = getOutputDisplayData(trade, context);
        for(int i = 0; i < outputDisplays.size(); ++i)
        {
            Pair<DisplayEntry,DisplayData> display = outputDisplays.get(i);
            if(display.getFirst().isMouseOver(this.x, this.y, display.getSecond(), mouseX, mouseY))
            {
                consumer.onTradeButtonOutputInteraction(context.getTrader(), trade, i, button);
                return;
            }
        }

        //Only run the default interaction code if you didn't hit an input or output display
        consumer.onTradeButtonInteraction(context.getTrader(), trade, mouseX - this.x, mouseY - this.y, button);

    }

    private static void DrawTooltip(MatrixStack pose, int mouseX, int mouseY, List<Text> tooltips)
    {
        if(tooltips == null || tooltips.size() <= 0)
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc != null)
            mc.currentScreen.renderTooltip(pose, tooltips, mouseX, mouseY);

    }

    public int isMouseOverInput(int mouseX, int mouseY)
    {
        TradeData trade = this.getTrade();
        if(trade == null)
            return -1;
        List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, this.getContext());
        for(int i = 0; i < inputDisplays.size(); ++i)
        {
            if(inputDisplays.get(i).getFirst().isMouseOver(this.x, this.y, inputDisplays.get(i).getSecond(), mouseX, mouseY))
                return i;
        }
        return -1;
    }

    public int isMouseOverOutput(int mouseX, int mouseY)
    {
        TradeData trade = this.getTrade();
        if(trade == null)
            return -1;
        List<Pair<DisplayEntry,DisplayData>> inputDisplays = getInputDisplayData(trade, this.getContext());
        for(int i = 0; i < inputDisplays.size(); ++i)
        {
            if(inputDisplays.get(i).getFirst().isMouseOver(this.x, this.y, inputDisplays.get(i).getSecond(), mouseX, mouseY))
                return i;
        }
        return -1;
    }

    public boolean isMouseOverAlert(int mouseX, int mouseY, TradeData trade, TradeContext context)
    {
        Pair<Integer,Integer> position = trade.alertPosition(context);
        int left = this.x + position.getFirst();
        int top = this.y + position.getSecond();
        return mouseX >= left && mouseX < left + ARROW_WIDTH && mouseY >= top && mouseY < top + ARROW_HEIGHT;
    }

    public static List<Pair<DisplayEntry,DisplayData>> getInputDisplayData(TradeData trade, TradeContext context)
    {
        List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
        List<DisplayEntry> entries = trade.getInputDisplays(context);
        List<DisplayData> display = trade.inputDisplayArea(context).divide(entries.size());
        for(int i = 0; i < entries.size() && i < display.size(); ++i)
            results.add(Pair.of(entries.get(i), display.get(i)));
        return results;
    }

    public static List<Pair<DisplayEntry,DisplayData>> getOutputDisplayData(TradeData trade, TradeContext context)
    {
        List<Pair<DisplayEntry,DisplayData>> results = new ArrayList<>();
        List<DisplayEntry> entries = trade.getOutputDisplays(context);
        List<DisplayData> display = trade.outputDisplayArea(context).divide(entries.size());
        for(int i = 0; i < entries.size() && i < display.size(); ++i)
            results.add(Pair.of(entries.get(i), display.get(i)));
        return results;
    }

    public DisplayEntry getInputDisplay(int index) {
        TradeData trade = tradeSource.get();
        if(trade == null)
            return DisplayEntry.EMPTY;
        List<DisplayEntry> inputDisplays = trade.getInputDisplays(this.getContext());
        if(index < 0 || index >= inputDisplays.size())
            return DisplayEntry.EMPTY;
        return inputDisplays.get(index);
    }

    private int inputDisplayCount() {
        TradeData trade = tradeSource.get();
        if(trade == null)
            return 0;
        return trade.getInputDisplays(this.getContext()).size();
    }

    public Pair<Integer,Integer> getInputDisplayPosition(int index) {
        TradeData trade = tradeSource.get();
        if(trade == null)
            return Pair.of(0, 0);

        int xPos = 0;
        int yPos = 0;
        int count = this.inputDisplayCount();
        if(index < 0 || index >= count)
            return Pair.of(0, 0);

        return Pair.of(this.x + xPos, this.y + yPos);

    }

    @Override
    protected boolean isValidClickButton(int button) {
        if(this.getContext().isStorageMode || this.displayOnly)
            return false;
        return super.isValidClickButton(button);
    }





}