package com.cwelth.craft_on_surface.block.entity.renderer;

import com.cwelth.craft_on_surface.block.entity.SurfaceCraftingDummyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class SurfaceCraftingDummyBlockRenderer implements BlockEntityRenderer<SurfaceCraftingDummyBlockEntity> {
    private final BlockEntityRendererProvider.Context ctx;

    public SurfaceCraftingDummyBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(SurfaceCraftingDummyBlockEntity surfaceCraftingDummyBlockEntity, float pPartialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = ctx.getItemRenderer();

        for(int i = 0; i < SurfaceCraftingDummyBlockEntity.MAX_SIZE; i++)
        {
            if(surfaceCraftingDummyBlockEntity.itemStackHandler.getStackInSlot(i).isEmpty()) return;

            poseStack.pushPose();
            poseStack.translate(0.5f, (float) i / 16.0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 45.0f));
            //poseStack.translate(0.5f, (float) i / 16.0f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));

            itemRenderer.renderStatic(surfaceCraftingDummyBlockEntity.itemStackHandler.getStackInSlot(i), ItemDisplayContext.FIXED,
                    getLightLevel(surfaceCraftingDummyBlockEntity.getLevel(), surfaceCraftingDummyBlockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, surfaceCraftingDummyBlockEntity.getLevel(), 1);
            poseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
