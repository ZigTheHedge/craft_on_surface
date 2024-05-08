package com.cwelth.craft_on_surface.recipe;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ItemsInLiquidRecipe  implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> materials;
    private final String inputLiquid;
    private final ItemStack outputItem;
    private final String outputLiquid;
    private final ResultType resultType;
    private final boolean liquidDisappears;

    public ItemsInLiquidRecipe(ResourceLocation id, NonNullList<Ingredient> materials, String inputLiquid, ItemStack outputItem, String outputLiquid, ResultType resultType, boolean liquidDisappears) {
        this.id = id;
        this.materials = materials;
        this.inputLiquid = inputLiquid;
        this.outputItem = outputItem;
        this.outputLiquid = outputLiquid;
        this.resultType = resultType;
        this.liquidDisappears = liquidDisappears;
    }

    @Override
    public boolean matches(SimpleContainer simpleContainer, Level level) {
        boolean[] matched = new boolean[materials.size()];
        for(int i = 0; i < materials.size(); i++)
            matched[i] = false;

        int filledSlots = 0;
        for(int i = 0; i < simpleContainer.getContainerSize(); i++)
        {
            if(simpleContainer.getItem(i).isEmpty()) continue;
            filledSlots++;
            for(int j = 0; j < materials.size(); j++)
            {
                if(matched[j]) continue;
                if(materials.get(j).test(simpleContainer.getItem(i)))
                {
                    matched[j] = true;
                    break;
                }
            }
        }

        boolean finalDecision = true;
        for(int i = 0; i < materials.size(); i++)
            if (!matched[i]) {
                finalDecision = false;
                break;
            }
        if(filledSlots != matched.length) finalDecision = false;

        return finalDecision;
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

    public boolean shouldLiquidDisappear()
    {
        return liquidDisappears;
    }

    public boolean suitsForLiquid(FluidState liquidBlock)
    {
        return ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputLiquid)).isSame(liquidBlock.getType());
        //return ForgeRegistries.BLOCKS.getKey(liquidBlock.getBlock()).toString().equalsIgnoreCase(inputLiquid);
    }

    public boolean suitsForRecipe(ItemStack item)
    {
        for(Ingredient ingredient: materials)
            if(ingredient.test(item)) return true;
        return false;
    }

    public Fluid getResultFluid() {
        return ForgeRegistries.FLUIDS.getValue(new ResourceLocation(getOutputLiquid()));
        /*
        Block toRet = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(outputLiquid));
        if(toRet instanceof LiquidBlock)
            return toRet.defaultBlockState();
        else
            return Blocks.AIR.defaultBlockState();

         */
    }

    public String getInputLiquid()
    {
        return inputLiquid;
    }

    public String getOutputLiquid()
    {
        return outputLiquid;
    }

    public ResultType getResultType() {
        return resultType;
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

    public Ingredient getMaterial(int slot) {
        if(slot >= materials.size())
            return Ingredient.EMPTY;
        return materials.get(slot);
    }

    public enum ResultType {
        ITEM,
        LIQUID;

    }

    public static class Type implements RecipeType<ItemsInLiquidRecipe> {
        private Type() {}
        public static final ItemsInLiquidRecipe.Type INSTANCE = new ItemsInLiquidRecipe.Type();
        public static final String ID = "items_in_liquid";
    }

    public static class Serializer implements RecipeSerializer<ItemsInLiquidRecipe> {
        public static final ItemsInLiquidRecipe.Serializer INSTANCE = new ItemsInLiquidRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(CraftOnSurface.MODID, "items_in_liquid");

        @Override
        public ItemsInLiquidRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ResultType resultType = ResultType.valueOf(GsonHelper.getAsString(pSerializedRecipe, "result_type"));
            ItemStack output = ItemStack.EMPTY;
            String outputLiquid = "";
            boolean liquidDisappearing = false;
            if(resultType == ResultType.ITEM)
            {
                JsonObject outputJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "output_item");
                output = ShapedRecipe.itemStackFromJson(outputJson);
                liquidDisappearing = GsonHelper.getAsBoolean(pSerializedRecipe, "consume_liquid");
            } else
                outputLiquid = GsonHelper.getAsString(pSerializedRecipe, "output_liquid_block");

            String inputLiquid = GsonHelper.getAsString(pSerializedRecipe, "input_liquid_block");
            JsonArray materialsJson = GsonHelper.getAsJsonArray(pSerializedRecipe, "materials");
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsJson.size(), Ingredient.EMPTY);
            for(int i = 0; i < materialsJson.size(); i++)
                materials.set(i, Ingredient.fromJson(materialsJson.get(i)));

            return new ItemsInLiquidRecipe(pRecipeId, materials, inputLiquid, output, outputLiquid, resultType, liquidDisappearing);
        }

        @Override
        public @Nullable ItemsInLiquidRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack output = pBuffer.readItem();
            String inputLiquid = pBuffer.readUtf();
            String outputLiquid = pBuffer.readUtf();
            boolean liquidDisappearing = pBuffer.readBoolean();
            ResultType resultType = ResultType.valueOf(pBuffer.readUtf());

            int materialsCount = pBuffer.readInt();
            NonNullList<Ingredient> materials = NonNullList.withSize(materialsCount, Ingredient.EMPTY);
            for(int i = 0; i < materialsCount; i++)
                materials.set(i, Ingredient.fromNetwork(pBuffer));
            return new ItemsInLiquidRecipe(pRecipeId, materials, inputLiquid, output, outputLiquid, resultType, liquidDisappearing);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ItemsInLiquidRecipe pRecipe) {
            pBuffer.writeItemStack(pRecipe.outputItem, false);
            pBuffer.writeUtf(pRecipe.inputLiquid);
            pBuffer.writeUtf(pRecipe.outputLiquid);
            pBuffer.writeBoolean(pRecipe.liquidDisappears);
            pBuffer.writeUtf(pRecipe.resultType.toString());

            pBuffer.writeInt(pRecipe.materials.size());
            for(int i = 0; i < pRecipe.materials.size(); i++)
                pRecipe.materials.get(i).toNetwork(pBuffer);
        }
    }
}
