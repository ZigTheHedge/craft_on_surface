package com.cwelth.craft_on_surface.setup;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.block.entity.renderer.SurfaceCraftingDummyBlockRenderer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CraftOnSurface.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlersModClient {
    @SubscribeEvent
    public static void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {

        }
    }

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(Registries.SURFACE_CRAFTING_DUMMY_BE.get(), SurfaceCraftingDummyBlockRenderer::new);
    }
}
