package com.cwelth.craft_on_surface.compat.jei;

import com.cwelth.craft_on_surface.CraftOnSurface;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEICraftOnSurfacePlugin implements IModPlugin {
    public static RecipeType<SurfaceCraftingRecipe> SURFACE_CRAFTING_RECIPE_TYPE = new RecipeType<>(SurfaceCraftingRecipeCategory.UID, SurfaceCraftingRecipe.class);


    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CraftOnSurface.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SurfaceCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<SurfaceCraftingRecipe> SURFACE_CRAFTING_RECIPES = rm.getAllRecipesFor(SurfaceCraftingRecipe.Type.INSTANCE);
        registration.addRecipes(SURFACE_CRAFTING_RECIPE_TYPE, SURFACE_CRAFTING_RECIPES);
    }
}
