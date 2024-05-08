package com.cwelth.craft_on_surface.compat.jei;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.recipe.ItemsInLiquidRecipe;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import com.cwelth.craft_on_surface.setup.Registries;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemsInLiquidRecipeCategory implements IRecipeCategory<ItemsInLiquidRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(CraftOnSurface.MODID, "items_in_liquid");
    public static final ResourceLocation TEXTURE = new ResourceLocation("craft_on_surface:textures/gui/jei/items_in_liquid_gui.png");

    IDrawable background;
    IDrawable icon;

    public ItemsInLiquidRecipeCategory(IGuiHelper helper)
    {
        background = helper.createDrawable(TEXTURE, 4, 4, 168, 75);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registries.LAYERED_ICON.get()));
    }

    @Override
    public RecipeType<ItemsInLiquidRecipe> getRecipeType() {
        return JEICraftOnSurfacePlugin.ITEMS_IN_LIQUID_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.craft_on_surface.items_in_liquid_block");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(ItemsInLiquidRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        boolean consumeLiquid = recipe.shouldLiquidDisappear();
        if(consumeLiquid)
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("jei.items_in_liquid_crafting.fluid_consumed"), 84, 64, 0xFFFFFFFF);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemsInLiquidRecipe recipe, IFocusGroup iFocusGroup) {
        for(int i = 0; i < 6; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 18 + i * 18, 16).addIngredients(recipe.getMaterial(i));
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(recipe.getInputLiquid()));
        ItemStack hiddenCatalyst = new ItemStack(fluid.getBucket());
        builder.addSlot(RecipeIngredientRole.CATALYST, 131, 41).addFluidStack(fluid, 1000);
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStack(hiddenCatalyst);
        if(recipe.getResultType() == ItemsInLiquidRecipe.ResultType.ITEM)
        {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 41).addItemStack(recipe.getResultItem());
            //Add "Fluid consumed"
        } else {
            Fluid fluidOutput = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(recipe.getOutputLiquid()));
            ItemStack hiddenResult = new ItemStack(fluidOutput.getBucket());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 41).addFluidStack(fluidOutput, 1000);
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(hiddenResult);
        }

    }
}
