package com.cwelth.craft_on_surface.recipe;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class SurfaceCraftingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> materials;
    private final String surfaceBlock;
    private final ItemStack output;

    public SurfaceCraftingRecipe(ResourceLocation id, NonNullList<Ingredient> materials, String surfaceBlock, ItemStack output) {
        this.id = id;
        this.materials = materials;
        this.surfaceBlock = surfaceBlock;
        this.output = output;
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
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    public ItemStack getResultItem() {
        return output.copy();
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
            JsonObject outputJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "output");
            ItemStack output = ShapedRecipe.itemStackFromJson(outputJson);
            String surface = GsonHelper.getAsString(pSerializedRecipe, "surface_block");
            JsonArray materialsJson = GsonHelper.getAsJsonArray(pSerializedRecipe, "materials");
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsJson.size(), Ingredient.EMPTY);
            for(int i = 0; i < materialsJson.size(); i++)
                materials.set(i, Ingredient.fromJson(materialsJson.get(i)));

            return new SurfaceCraftingRecipe(pRecipeId, materials, surface, output);
        }

        @Override
        public @Nullable SurfaceCraftingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack output = pBuffer.readItem();
            String surface = pBuffer.readUtf();
            int materialsCount = pBuffer.readInt();
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsCount, Ingredient.EMPTY);
            for(int i = 0; i < materialsCount; i++)
                materials.set(i, Ingredient.fromNetwork(pBuffer));
            return new SurfaceCraftingRecipe(pRecipeId, materials, surface, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SurfaceCraftingRecipe pRecipe) {
            pBuffer.writeItemStack(pRecipe.output, false);
            pBuffer.writeUtf(pRecipe.surfaceBlock);
            pBuffer.writeInt(pRecipe.materials.size());
            for(int i = 0; i < pRecipe.materials.size(); i++)
                pRecipe.materials.get(i).toNetwork(pBuffer);
        }
    }
}
