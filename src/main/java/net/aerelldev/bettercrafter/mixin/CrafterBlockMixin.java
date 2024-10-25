package net.aerelldev.bettercrafter.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;

import static net.minecraft.block.CrafterBlock.*;

@Mixin(CrafterBlock.class)
abstract class CrafterBlockMixin {

    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    private void neighborUpdateInject(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof CrafterBlockEntity crafterBlockEntity) {
            CraftingRecipeInput craftingRecipeInput = crafterBlockEntity.createRecipeInput();
            Optional<RecipeEntry<CraftingRecipe>> optional = getCraftingRecipe(Objects.requireNonNull(world.getServer()).getOverworld(), craftingRecipeInput);
            if(optional.isEmpty()) {
                if(world.getBlockState(pos).get(TRIGGERED)) {
                    world.setBlockState(pos, state.with(TRIGGERED, false));
                    crafterBlockEntity.setTriggered(false);
                }
                ci.cancel();
            }else{
                for (ItemStack itemStack : crafterBlockEntity.getHeldStacks()) {
                    if (itemStack.getCount() == 1) {
                        if(world.getBlockState(pos).get(TRIGGERED)) {
                            world.setBlockState(pos, state.with(TRIGGERED, false));
                            crafterBlockEntity.setTriggered(false);
                        }
                        ci.cancel();
                        break;
                    }
                }
            }
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void craftInject(BlockState state, ServerWorld world, BlockPos pos, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof CrafterBlockEntity crafterBlockEntity) {
            for (ItemStack itemStack : crafterBlockEntity.getHeldStacks()) {
                if (itemStack.getCount() == 1) {
                    ci.cancel();
                    break;
                }
            }
        }
    }
}