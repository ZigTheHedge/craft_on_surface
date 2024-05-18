package com.cwelth.craft_on_surface.setup;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.block.entity.SurfaceCraftingDummyBlockEntity;
import com.cwelth.craft_on_surface.recipe.ItemsInLiquidRecipe;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class EventHandlersForge {
    public static List<SurfaceCraftingRecipe> surfaceCraftingRecipes = null;
    public static List<ItemsInLiquidRecipe> itemsInLiquidRecipes = null;
    private static final String keyName = CraftOnSurface.MODID + ":markedForInWorldCrafting";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getFace() == Direction.UP)
        {
            ItemStack usedItem = event.getItemStack();
            Level level = event.getLevel();
            if(usedItem.getItem() != Items.AIR && event.getHand() == InteractionHand.MAIN_HAND && !event.getEntity().isShiftKeyDown() && level.getBlockState(event.getPos().above()).isAir())
            {
                if(level.getBlockEntity(event.getPos()) == null)
                {
                                    /* SimpleContainer testContainer = new SimpleContainer(1);
                testContainer.setItem(0, usedItem); */
                    if(surfaceCraftingRecipes == null)
                    {
                        CraftOnSurface.LOGGER.debug("First time calling surfaceCraftingRecipes. Caching.");
                        surfaceCraftingRecipes = level.getRecipeManager().getAllRecipesFor(SurfaceCraftingRecipe.Type.INSTANCE);
                        CraftOnSurface.LOGGER.debug(surfaceCraftingRecipes.size() + " recipes loaded.");
                    }
                    BlockState blockState = event.getLevel().getBlockState(event.getPos());
                    if(blockState.is(Registries.SURFACE_CRAFTING_DUMMY_BLOCK.get())) return;
                    //CraftOnSurface.LOGGER.debug("Clicked " + blockState.getBlock() + " with " + usedItem.getItem());

                    for(SurfaceCraftingRecipe recipe: surfaceCraftingRecipes)
                    {
                        if(recipe.suitsForItemAndBlock(usedItem, blockState, level, event.getPos()))
                        {
                            //CraftOnSurface.LOGGER.debug("Found valid recipe: " + recipe.getId());
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
    }

    @SubscribeEvent
    public void onPlayerTossItem(ItemTossEvent event)
    {
        Player player = event.getPlayer();
        if(player instanceof ServerPlayer)
        {
            ItemEntity trackedItem =  event.getEntity();
            ItemStack itemStack = trackedItem.getItem();
            Level level = player.level();
            if(itemsInLiquidRecipes == null)
            {
                CraftOnSurface.LOGGER.debug("First time calling itemsInLiquidRecipes. Caching.");
                itemsInLiquidRecipes = level.getRecipeManager().getAllRecipesFor(ItemsInLiquidRecipe.Type.INSTANCE);
                CraftOnSurface.LOGGER.debug(itemsInLiquidRecipes.size() + " recipes loaded.");
            }

            for(ItemsInLiquidRecipe recipe: itemsInLiquidRecipes)
            {
                if(recipe.suitsForRecipe(itemStack))
                {
                    //CraftOnSurface.LOGGER.debug("Found valid item for recipe: " + recipe.getId());
                    CompoundTag tag = new CompoundTag();
                    tag = trackedItem.saveWithoutId(tag);
                    CompoundTag forgeData;
                    if(tag.contains("ForgeData"))
                        forgeData = tag.getCompound("ForgeData");
                    else
                        forgeData = new CompoundTag();

                    if(forgeData.contains(keyName))
                        continue;
                    forgeData.putBoolean(keyName, true);
                    tag.put("ForgeData", forgeData);
                    trackedItem.load(tag);
                    trackedItem.setInvulnerable(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldTickCrafting(TickEvent.LevelTickEvent event)
    {
        if(event.level.isClientSide()) return;
        ServerLevel level = (ServerLevel) event.level;
        HashMap<BlockPos, ArrayList<ItemEntity>> foundPiles = new HashMap<>();
        for(Entity entity: level.getAllEntities())
        {
            if(entity instanceof ItemEntity)
            {
                CompoundTag tag = new CompoundTag();
                tag = entity.saveWithoutId(tag);
                if(tag.contains("ForgeData")) {
                    CompoundTag forgeData = tag.getCompound("ForgeData");
                    if(forgeData.contains(keyName))
                    {
                        BlockPos pos = entity.getOnPos();
                        if(!foundPiles.containsKey(pos))
                        {
                            foundPiles.put(pos, new ArrayList<>());
                        }
                        ArrayList<ItemEntity> stackList = foundPiles.get(pos);
                        stackList.add((ItemEntity) entity);
                        foundPiles.put(pos, stackList);
                    }
                }
            }
        }

        for(Map.Entry<BlockPos, ArrayList<ItemEntity>> entry : foundPiles.entrySet())
        {
            SimpleContainer inventory = new SimpleContainer(entry.getValue().size());
            BlockPos pos = entry.getKey();
            BlockPos posAbove = pos.above();
            for(int i = 0; i < entry.getValue().size(); i++)
            {
                inventory.setItem(i, entry.getValue().get(i).getItem());
            }
            Optional<ItemsInLiquidRecipe> recipe = level.getRecipeManager()
                    .getRecipeFor(ItemsInLiquidRecipe.Type.INSTANCE, inventory, level);
            if(recipe.isPresent())
            {
                ItemsInLiquidRecipe foundRecipe = recipe.get();
                boolean suitableLiquidFound = foundRecipe.suitsForLiquid(level.getFluidState(pos));
                boolean suitableLiquidFoundAbove = foundRecipe.suitsForLiquid(level.getFluidState(posAbove));
                if(suitableLiquidFound || suitableLiquidFoundAbove)
                {
                    //CraftOnSurface.LOGGER.debug("Found valid recipe: " + foundRecipe.getId());
                    // Recipe matched. Crafting.
                    if(foundRecipe.getResultType() == ItemsInLiquidRecipe.ResultType.ITEM)
                    {
                        ItemStack resultItem = foundRecipe.getResultItem();
                        for(int i = 0; i < entry.getValue().size(); i++)
                        {
                            entry.getValue().get(i).remove(Entity.RemovalReason.DISCARDED);
                        }
                        if(foundRecipe.shouldLiquidDisappear()) {
                            if(suitableLiquidFound) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                            else level.setBlock(posAbove, Blocks.AIR.defaultBlockState(), 11);
                        }
                        if(suitableLiquidFound) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), resultItem);
                        else Containers.dropItemStack(level, posAbove.getX(), posAbove.getY(), posAbove.getZ(), resultItem);
                    } else
                    {
                        Fluid resultFluid = foundRecipe.getResultFluid();
                        for(int i = 0; i < entry.getValue().size(); i++)
                        {
                            entry.getValue().get(i).remove(Entity.RemovalReason.DISCARDED);
                        }
                        if(suitableLiquidFound) level.setBlock(pos, resultFluid.defaultFluidState().createLegacyBlock(), 11);
                        else level.setBlock(posAbove, resultFluid.defaultFluidState().createLegacyBlock(), 11);
                    }
                    break;
                } else {
                    //CraftOnSurface.LOGGER.debug("Found valid recipe, but fluid doesn't match: " + foundRecipe.getId() + " required: " + foundRecipe.getInputLiquid() + ", found: " + level.getFluidState(pos) + ", pos: " + pos);
                }
            }
        }


    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDatapackReload(AddReloadListenerEvent event)
    {
        event.addListener(new SurfaceCraftingReloadListener());
    }

    @SubscribeEvent
    public void onAnvilHit(EntityLeaveLevelEvent event)
    {

        //TODO: Add Anvil crafting
        /*
        event.getLevel();
        if(event.getEntity() instanceof FallingBlockEntity)
        {
            FallingBlockEntity fallingEntity = (FallingBlockEntity) event.getEntity();
            BlockState fallingBlockState = fallingEntity.getBlockState();
            if(fallingBlockState.is(Blocks.ANVIL) || fallingBlockState.is(Blocks.CHIPPED_ANVIL) || fallingBlockState.is(Blocks.DAMAGED_ANVIL))
            {
                event.getLevel();
                BlockPos pos = fallingEntity.getOnPos();
                Level level = event.getLevel();
                AABB area = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

                event.getLevel();
            }
        }
         */
    }
}
