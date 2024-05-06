package com.cwelth.craft_on_surface.block;

import com.cwelth.craft_on_surface.block.entity.SurfaceCraftingDummyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SurfaceCraftingDummyBlock extends BaseEntityBlock {
    public SurfaceCraftingDummyBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SurfaceCraftingDummyBlockEntity(blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof SurfaceCraftingDummyBlockEntity) {
                ((SurfaceCraftingDummyBlockEntity) blockEntity).drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            if(pHand == InteractionHand.MAIN_HAND)
            {
                SurfaceCraftingDummyBlockEntity be = (SurfaceCraftingDummyBlockEntity) pLevel.getBlockEntity(pPos);
                if (be != null) {
                    if(pPlayer.isShiftKeyDown())
                    {
                        ItemStack result = be.craft();
                        if(!result.isEmpty())
                        {
                            Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), result);
                            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
                        }
                    } else
                    {
                        ItemStack useItem = pPlayer.getItemInHand(pHand);
                        if (useItem.isEmpty()) {
                            pPlayer.setItemInHand(pHand, be.popStack());
                        } else {
                            if (!be.pushStack(useItem))
                                return InteractionResult.FAIL;
                        }
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }
}
