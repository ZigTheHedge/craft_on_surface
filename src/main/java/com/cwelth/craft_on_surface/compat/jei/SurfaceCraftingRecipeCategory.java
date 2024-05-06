package com.cwelth.craft_on_surface.compat.jei;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import com.cwelth.craft_on_surface.setup.Registries;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SurfaceCraftingRecipeCategory implements IRecipeCategory<SurfaceCraftingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(CraftOnSurface.MODID, "surface_crafting_recipe");
    public static final ResourceLocation TEXTURE = new ResourceLocation("craft_on_surface:textures/gui/jei/surface_crafting_gui.png");

    IDrawable background;
    IDrawable icon;

    public SurfaceCraftingRecipeCategory(IGuiHelper helper)
    {
        background = helper.createDrawable(TEXTURE, 4, 4, 168, 75);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registries.LAYERED_ICON.get()));
    }

    @Override
    public RecipeType<SurfaceCraftingRecipe> getRecipeType() {
        return JEICraftOnSurfacePlugin.SURFACE_CRAFTING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.craft_on_surface.surface_crafting_dummy_block");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SurfaceCraftingRecipe recipe, IFocusGroup iFocusGroup) {
        for(int i = 0; i < 8; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 13 + i * 18, 7).addIngredients(recipe.getMaterial(i));
            builder.addSlot(RecipeIngredientRole.INPUT, 13 + i * 18, 34).addIngredients(recipe.getMaterial(8+i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 139, 55).addItemStack(recipe.getResultItem());
    }
}
