package com.cwelth.craft_on_surface.recipe;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SurfaceCraftingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> materials;
    private final String surfaceBlock;
    private final ItemStack outputItem;
    private final String outputBlock;
    private final ResultType resultType;

    public SurfaceCraftingRecipe(ResourceLocation id, NonNullList<Ingredient> materials, String surfaceBlock, ItemStack outputItem, String outputBlock, ResultType resultType) {
        this.id = id;
        this.materials = materials;
        this.surfaceBlock = surfaceBlock;
        this.outputItem = outputItem;
        this.outputBlock = outputBlock;
        this.resultType = resultType;
    }

    public Ingredient getMaterial(int slot)
    {
        if(slot >= materials.size())
            return Ingredient.EMPTY;
        return materials.get(slot);
    }

    @Override
    public boolean matches(SimpleContainer simpleContainer, Level level) {
        for(int i = 0; i < materials.size(); i++)
        {
            if(!materials.get(i).test(simpleContainer.getItem(i))) return false;
        }
        int countOccupiesSlots = 0;
        for(int i = 0; i < simpleContainer.getContainerSize(); i++)
            if(!simpleContainer.getItem(i).isEmpty()) countOccupiesSlots++;
        if(materials.size() != countOccupiesSlots) return false;
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer simpleContainer, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputItem.copy();
    }

    public ItemStack getResultItem() {
        return outputItem.copy();
    }

    public BlockState getResultBlock(BlockPlaceContext ctx) {
        if(ctx == null)
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlock)).defaultBlockState();
        else
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlock)).getStateForPlacement(ctx);
    }

    public ItemStack getResultBlock()
    {
        return new ItemStack(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputBlock)));
    }

    public String getSurfaceBlock()
    {
        return surfaceBlock;
    }

    public ResultType getResultType()
    {
        return resultType;
    }

    public boolean suitsForItemAndBlock(ItemStack firstItem, BlockState blockState, Level level, BlockPos pos)
    {
        boolean blockSuites = false;
        if(surfaceBlock.startsWith("$"))
        {
            // Special case. Valid values are $solid $nonsolid
            String lookupBlock = surfaceBlock.substring(1);
            if(lookupBlock.equalsIgnoreCase("solid"))
            {
                blockSuites = blockState.isRedstoneConductor(level, pos);
            } else if(lookupBlock.equalsIgnoreCase("nonsolid"))
            {
                blockSuites = !blockState.isRedstoneConductor(level, pos);
            } else
            {
                CraftOnSurface.LOGGER.warn("Error interpreting surface_block value (" + surfaceBlock + "). Valid values are: $solid, $nonsolid of recipe " + id);
            }
        } else if(surfaceBlock.startsWith("#"))
        {
            // Looking for block tags
            String lookupBlock = surfaceBlock.substring(1);
            TagKey<Block> blockTag = BlockTags.create(new ResourceLocation(lookupBlock));
            blockSuites = blockState.is(blockTag);
        } else
        {
            blockSuites = ForgeRegistries.BLOCKS.getKey(blockState.getBlock()).toString().equalsIgnoreCase(surfaceBlock);
        }
        return materials.get(0).test(firstItem) && blockSuites;
    }

    public Ingredient getBlockIngredient()
    {
        if(surfaceBlock.startsWith("#"))
        {
            // Looking for block tags
            String lookupBlock = surfaceBlock.substring(1);
            TagKey<Block> blockTag = BlockTags.create(new ResourceLocation(lookupBlock));
            Iterator tagIterator = BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag).iterator();
            List<ItemStack> itemStacks = new ArrayList<>();

            while(tagIterator.hasNext()) {
                Holder<Block> holder = (Holder)tagIterator.next();
                itemStacks.add(new ItemStack(holder.get().asItem()));
            }
            Ingredient toRet = Ingredient.of(itemStacks.stream());
            return toRet;
        } else
            return Ingredient.EMPTY;
    }


    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public enum ResultType {
        ITEM,
        BLOCK,
        BLOCK_BELOW
    }

    public static class Type implements RecipeType<SurfaceCraftingRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "surface_crafting_recipe";
    }

    public static class Serializer implements RecipeSerializer<SurfaceCraftingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(CraftOnSurface.MODID, "surface_crafting_recipe");

        @Override
        public SurfaceCraftingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ResultType type = ResultType.ITEM;
            try
            {
                String resultType = GsonHelper.getAsString(pSerializedRecipe, "result_type");
                type = ResultType.valueOf(resultType);
            } catch (JsonSyntaxException e)
            {

            }
            ItemStack outputItem = ItemStack.EMPTY;
            String outputBlock = "";
            if(type == ResultType.ITEM)
            {
                JsonObject outputJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "output");
                outputItem = ShapedRecipe.itemStackFromJson(outputJson);
            } else {
                outputBlock = GsonHelper.getAsString(pSerializedRecipe, "output");
            }
            String surface = GsonHelper.getAsString(pSerializedRecipe, "surface_block");
            JsonArray materialsJson = GsonHelper.getAsJsonArray(pSerializedRecipe, "materials");
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsJson.size(), Ingredient.EMPTY);
            for(int i = 0; i < materialsJson.size(); i++)
                materials.set(i, Ingredient.fromJson(materialsJson.get(i)));

            return new SurfaceCraftingRecipe(pRecipeId, materials, surface, outputItem, outputBlock, type);
        }

        @Override
        public @Nullable SurfaceCraftingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ResultType type = ResultType.valueOf(pBuffer.readUtf());
            ItemStack output = pBuffer.readItem();
            String surface = pBuffer.readUtf();
            String outputBlock = pBuffer.readUtf();
            int materialsCount = pBuffer.readInt();
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsCount, Ingredient.EMPTY);
            for(int i = 0; i < materialsCount; i++)
                materials.set(i, Ingredient.fromNetwork(pBuffer));
            return new SurfaceCraftingRecipe(pRecipeId, materials, surface, output, outputBlock, type);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SurfaceCraftingRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.resultType.toString());
            pBuffer.writeItemStack(pRecipe.outputItem, false);
            pBuffer.writeUtf(pRecipe.surfaceBlock);
            pBuffer.writeUtf(pRecipe.outputBlock);
            pBuffer.writeInt(pRecipe.materials.size());
            for(int i = 0; i < pRecipe.materials.size(); i++)
                pRecipe.materials.get(i).toNetwork(pBuffer);
        }
    }
}
