package com.cwelth.craft_on_surface.block.entity;

import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import com.cwelth.craft_on_surface.setup.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SurfaceCraftingDummyBlockEntity extends BlockEntity {
    public static final int MAX_SIZE = 16;
    public final ItemStackHandler itemStackHandler = new ItemStackHandler(MAX_SIZE + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide())
            {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public static final int OUTPUT_SLOT = MAX_SIZE;

    public SurfaceCraftingDummyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Registries.SURFACE_CRAFTING_DUMMY_BE.get(), pPos, pBlockState);
    }



    private int findNearestFreeSlot()
    {
        for(int i = 0; i < MAX_SIZE; i++)
            if(itemStackHandler.getStackInSlot(i).isEmpty()) return i;
        return -1;
    }

    public boolean pushStack(ItemStack item)
    {
        int freeSlot = findNearestFreeSlot();
        if(freeSlot == -1) return false;
        itemStackHandler.setStackInSlot(freeSlot, item.copyWithCount(1));
        item.shrink(1);
        return true;
    }

    public ItemStack popStack()
    {
        int nonfreeSlot = findNearestFreeSlot();
        if(nonfreeSlot == -1) nonfreeSlot = MAX_SIZE;
        if(nonfreeSlot == 0) return ItemStack.EMPTY;
        nonfreeSlot--;
        ItemStack toRet = itemStackHandler.getStackInSlot(nonfreeSlot);
        itemStackHandler.setStackInSlot(nonfreeSlot, ItemStack.EMPTY);
        nonfreeSlot = findNearestFreeSlot();
        if(nonfreeSlot == 0) getLevel().setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 11);
        return toRet;
    }

    public void drops()
    {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0; i < inventory.getContainerSize(); i++)
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public ItemStack craft(Player player, BlockHitResult hitResult)
    {
        SimpleContainer inventory = new SimpleContainer(this.itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemStackHandler.getStackInSlot(i));
        }
        Optional<SurfaceCraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(SurfaceCraftingRecipe.Type.INSTANCE, inventory, level);
        if(recipe.isPresent())
        {
            SurfaceCraftingRecipe foundRecipe = recipe.get();
            for(int i = 0; i < itemStackHandler.getSlots(); i++) {
                if(itemStackHandler.getStackInSlot(i).isDamageableItem())
                {
                    ItemStack damageableItem = itemStackHandler.getStackInSlot(i);
                    /*
                    int damage = damageableItem.getDamageValue() + 1;
                    int maxDamage = damageableItem.getMaxDamage();
                    if(damage >= maxDamage)
                        damageableItem.shrink(1);
                    else
                        damageableItem.setDamageValue(damage);

                     */
                    damageableItem.hurt(1, player.getRandom(), (ServerPlayer) player);
                    itemStackHandler.setStackInSlot(i, damageableItem);

                } else
                    itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
            level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 11);
            SurfaceCraftingRecipe.ResultType resultType = foundRecipe.getResultType();
            if(resultType == SurfaceCraftingRecipe.ResultType.ITEM)
                return foundRecipe.getResultItem(getLevel().registryAccess());
            else if(resultType == SurfaceCraftingRecipe.ResultType.BLOCK)
            {
                level.setBlock(getBlockPos(), foundRecipe.getResultBlock(
                        new BlockPlaceContext(player, InteractionHand.MAIN_HAND, foundRecipe.getResultBlock(), hitResult)), 11);
            } else {
                level.setBlock(getBlockPos().below(), foundRecipe.getResultBlock(
                        new BlockPlaceContext(player, InteractionHand.MAIN_HAND, foundRecipe.getResultBlock(), hitResult)), 11);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemStackHandler.serializeNBT());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
