package io.github.lightman314.lightmanscurrency.client.renderer.entity.layers;

import io.github.lightman314.lightmanscurrency.client.ModLayerDefinitions;
import io.github.lightman314.lightmanscurrency.client.model.ModelWallet;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WalletLayer<T extends PlayerEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T,M> {

    private ModelWallet<T> model;

    public WalletLayer(FeatureRendererContext<T,M> renderer)
    {
        super(renderer);
        this.model = new ModelWallet<T>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(ModLayerDefinitions.WALLET));
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumerProvider bufferSource, int light, T entity, float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

        WalletHandler handler = WalletHandler.getWallet(entity);
        if(!handler.visible())
            return;

        ItemStack wallet = handler.getWallet();
        if(wallet.getItem() instanceof WalletItem walletItem)
        {
            this.model.animateModel(entity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.getContextModel().copyStateTo(this.model);
            VertexConsumer vertexConsumer = ItemRenderer.getItemGlintConsumer(bufferSource, this.model.getLayer(walletItem.getModelTexture()), false, wallet.hasGlint());
            this.model.render(poseStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);

        }

    }

    public static TexturedModelData createLayer() {
        Dilation cube = Dilation.NONE;
        ModelData mesh = BipedEntityModel.getModelData(cube, 0.0f);
        ModelPartData part = mesh.getRoot();
        part.addChild("wallet", ModelPartBuilder.create().uv(0, 0).cuboid(4f, 11.5f, -2f, 2f, 4f, 4f, cube),
                ModelTransform.of(0f, 0f, 0f, 0f, 0f, 0f));
        return TexturedModelData.of(mesh, 32, 16);
    }

    public static void registerLayer(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer, LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper, EntityRendererFactory.Context context)
    {
        if(entityType == EntityType.PLAYER)
        {
            try{
                registrationHelper.register(new WalletLayer<>((FeatureRendererContext<PlayerEntity, BipedEntityModel<PlayerEntity>>) entityRenderer));
            } catch(Throwable ignored) { }
        }
    }

}
