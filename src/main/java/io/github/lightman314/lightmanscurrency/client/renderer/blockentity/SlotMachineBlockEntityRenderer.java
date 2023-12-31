package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

public class SlotMachineBlockEntityRenderer implements BlockEntityRenderer<SlotMachineTraderBlockEntity> {

    public SlotMachineBlockEntityRenderer(BlockEntityRendererFactory.Context ignored) { }

    @Override
    public void render(SlotMachineTraderBlockEntity tileEntity, float tickDelta, MatrixStack poseStack, VertexConsumerProvider bufferSource, int light, int overlay) {

        //Render the door
        if(tileEntity.getCachedState().getBlock() instanceof SlotMachineBlock block)
        {

            //LightmansCurrency.LogDebug("Light level is " + lightLevel);

            Identifier lightModel = block.getLightModel();
            if(lightModel == null)
                return;

            poseStack.push();

            Direction facing = block.getFacing(tileEntity.getCachedState());
            Vector3f corner = IRotatableBlock.getOffsetVect(facing);
            Vector3f right = IRotatableBlock.getRightVect(facing);
            Vector3f forward = IRotatableBlock.getForwardVect(facing);
            Vector3f offset = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 0.5f), MathUtil.VectorMult(forward, 0.5f), new Vector3f(0f,0.5f,0f));

            poseStack.translate(offset.x, offset.y, offset.z);
            poseStack.multiply(MathUtil.getRotationDegrees(facing.getHorizontal() * -90f));

            MinecraftClient mc = MinecraftClient.getInstance();
            BakedModel model = BakedModelManagerHelper.getModel(mc.getBakedModelManager(), lightModel);
            ItemRenderer itemRenderer = mc.getItemRenderer();
            itemRenderer.renderItem(new ItemStack(block), ModelTransformationMode.FIXED, false, poseStack, bufferSource, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);

            poseStack.pop();

        }

    }
}
