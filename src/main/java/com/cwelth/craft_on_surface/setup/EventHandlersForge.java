package com.cwelth.craft_on_surface.setup;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.block.entity.SurfaceCraftingDummyBlockEntity;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingReloadListener;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class EventHandlersForge {
    public static List<SurfaceCraftingRecipe> surfaceCraftingRecipes = null;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getLevel().isClientSide()) return;
        if(event.getFace() == Direction.UP)
        {
            ItemStack usedItem = event.getItemStack();
            if(usedItem.getItem() != Items.AIR && event.getHand() == InteractionHand.MAIN_HAND && !event.getEntity().isShiftKeyDown())
            {
                Level level = event.getLevel();
                SimpleContainer testContainer = new SimpleContainer(1);
                testContainer.setItem(0, usedItem);
                if(surfaceCraftingRecipes == null)
                {
                    CraftOnSurface.LOGGER.debug("First time calling surfaceCraftingRecipes. Caching.");
                    surfaceCraftingRecipes = level.getRecipeManager().getAllRecipesFor(SurfaceCraftingRecipe.Type.INSTANCE);
                    CraftOnSurface.LOGGER.debug(surfaceCraftingRecipes.size() + " recipes loaded.");
                }
                BlockState blockState = event.getLevel().getBlockState(event.getPos());
                if(blockState.is(Registries.SURFACE_CRAFTING_DUMMY_BLOCK.get())) return;
                CraftOnSurface.LOGGER.debug("Clicked " + blockState.getBlock() + " with " + usedItem.getItem());

                for(SurfaceCraftingRecipe recipe: surfaceCraftingRecipes)
                {
                    if(recipe.suitsForItemAndBlock(usedItem, blockState, level, event.getPos()))
                    {
                        CraftOnSurface.LOGGER.debug("Found valid recipe: " + recipe.getId());
                        level.setBlock(event.getPos().above(), Registries.SURFACE_CRAFTING_DUMMY_BLOCK.get().defaultBlockState(), 11);
                        SurfaceCraftingDummyBlockEntity be = (SurfaceCraftingDummyBlockEntity)level.getBlockEntity(event.getPos().above());
                        if(be != null)
                        {
                            be.pushStack(usedItem);
                            event.setCanceled(true);
                            break;
                        }
                    }
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void OnDatapackReload(AddReloadListenerEvent event)
    {
        event.addListener(new SurfaceCraftingReloadListener());
    }
}
