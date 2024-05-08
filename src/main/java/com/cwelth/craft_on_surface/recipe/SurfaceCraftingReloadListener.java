package com.cwelth.craft_on_surface.recipe;

import com.cwelth.craft_on_surface.setup.EventHandlersForge;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class SurfaceCraftingReloadListener extends SimplePreparableReloadListener<Void> {
    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return null;
    }

    @Override
    protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        EventHandlersForge.surfaceCraftingRecipes = null;
        EventHandlersForge.itemsInLiquidRecipes = null;
    }
}
