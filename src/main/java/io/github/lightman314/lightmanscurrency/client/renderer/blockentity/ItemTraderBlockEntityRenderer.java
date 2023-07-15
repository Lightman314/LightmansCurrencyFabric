package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity> {

    public ItemTraderBlockEntityRenderer(BlockEntityRendererFactory.Context dispatcher) { }

    @Override
    public void render(ItemTraderBlockEntity blockEntity, float partialTicks, MatrixStack pose, VertexConsumerProvider buffer, int lightLevel, int id)
    {
        renderItems(blockEntity, partialTicks, pose, buffer, lightLevel, id);
    }

    public static List<ItemStack> GetRenderItems(ItemTradeData trade) {
        List<ItemStack> result = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = trade.getSellItem(i);
            if(!item.isEmpty())
                result.add(item);
        }
        return result;
    }
    public static void renderItems(ItemTraderBlockEntity blockEntity, float partialTicks, MatrixStack pose, VertexConsumerProvider buffer, int lightLevel, int id)
    {
        try{
            ItemTraderData trader = blockEntity.getTraderData();
            if(trader == null)
                return;
            for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
            {

                ItemTradeData trade = trader.getTrade(tradeSlot);
                List<ItemStack> renderItems = GetRenderItems(trade);
                if(renderItems.size() > 0)
                {

                    ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

                    //Get positions
                    List<Vector3f> positions = blockEntity.GetStackRenderPos(tradeSlot, renderItems.size() > 1);

                    //Get rotation
                    List<Quaternionf> rotation = blockEntity.GetStackRenderRot(tradeSlot, partialTicks);

                    //Get scale
                    float scale = blockEntity.GetStackRenderScale(tradeSlot);

                    int stock = trader.getTradeStock(tradeSlot);
                    for(int pos = 0; pos < LCConfig.CLIENT.itemRenderLimit.get() && pos < positions.size() && pos < stock; pos++)
                    {

                        pose.push();

                        Vector3f position = positions.get(pos);

                        //Translate, rotate, and scale the matrix stack
                        pose.translate(position.x(), position.y(), position.z());
                        for(Quaternionf rot : rotation)
                        {
                            pose.multiply(rot);
                        }
                        pose.scale(scale, scale, scale);

                        //Render the item
                        if(renderItems.size() > 1)
                        {
                            //Render first item
                            pose.push();

                            //Don't base translation off of scale, as we've already been scaled down.
                            pose.translate(0.25, 0.25, 0d);
                            pose.scale(0.5f, 0.5f, 0.5f);

                            itemRenderer.renderItem(renderItems.get(0), ModelTransformationMode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, blockEntity.getWorld(), id);

                            pose.pop();

                            //Render second item
                            pose.push();

                            //Slightly offset in the Z to prevent z-fighting if there's an overlap
                            pose.translate(-0.25, -0.25, 0.001d);
                            pose.scale(0.5f, 0.5f, 0.5f);

                            itemRenderer.renderItem(renderItems.get(1), ModelTransformationMode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, blockEntity.getWorld(), id);

                            pose.pop();
                        }
                        else
                            itemRenderer.renderItem(renderItems.get(0), ModelTransformationMode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, blockEntity.getWorld(), id);

                        pose.pop();

                    }

                }
            }
        } catch(Throwable ignored) { }
    }


}