package com.cwelth.craft_on_surface.setup;

import com.cwelth.craft_on_surface.block.SurfaceCraftingDummyBlock;
import com.cwelth.craft_on_surface.block.entity.SurfaceCraftingDummyBlockEntity;
import com.cwelth.craft_on_surface.recipe.SurfaceCraftingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.cwelth.craft_on_surface.CraftOnSurface.MODID;

public class Registries {

    // Registries
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

    // Helper methods
    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toRet = BLOCKS.register(name, block);
        registerBlockItem(name, toRet);
        return toRet;
    }

    // Objects
    public static final RegistryObject<Block> SURFACE_CRAFTING_DUMMY_BLOCK = BLOCKS.register(
            "surface_crafting_dummy_block",
            () -> new SurfaceCraftingDummyBlock(BlockBehaviour.Properties.of().noCollission()
                    .noLootTable().noParticlesOnBreak().strength(-1.0F, 3600000.0F)
                    .sound(SoundType.BAMBOO)
            )
    );

    public static final RegistryObject<BlockEntityType<SurfaceCraftingDummyBlockEntity>> SURFACE_CRAFTING_DUMMY_BE = BLOCK_ENTITY_TYPES.register(
            "surface_crafting_dummy_be", () -> BlockEntityType.Builder.of(SurfaceCraftingDummyBlockEntity::new,
                    SURFACE_CRAFTING_DUMMY_BLOCK.get()).build(null)
    );

    public static final RegistryObject<CreativeModeTab> COS_TAB = CREATIVE_MODE_TABS.register("creative_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Registries.LAYERED_ICON.get()))
                    .title(Component.translatable("creativetab.creative_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(Registries.TOOL_HANDLE.get());
                        output.accept(Registries.TOOL_BINDING.get());
                    })
                    .build());

    public static final RegistryObject<Item> TOOL_HANDLE = ITEMS.register("tool_handle", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOOL_BINDING = ITEMS.register("tool_binding", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LAYERED_ICON = ITEMS.register("layered_icon", () -> new Item(new Item.Properties()));

    // Recipes

    public static final RegistryObject<RecipeSerializer<SurfaceCraftingRecipe>> SURFACE_CRAFTING_RECIPE_SERIALIZER =
            SERIALIZERS.register("surface_crafting_recipe", () -> SurfaceCraftingRecipe.Serializer.INSTANCE);


    public static void setup() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        SERIALIZERS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
