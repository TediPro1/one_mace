package com.yourname.singletonmace.mixin;

import com.yourname.singletonmace.MaceTrackerState;
import com.yourname.singletonmace.SingletonMace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {

    @Shadow @Final private PlayerEntity player;
    @Shadow @Final private CraftingResultInventory input;

    /**
     * Injects before the item is taken from the crafting result slot.
     * This allows us to prevent the crafting of a Mace if one already exists on the server.
     */
    @Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onCraft(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;I)V"), cancellable = true)
    private void singletonmace_onTakeItem(ItemStack stack, CallbackInfo ci) {
        if (stack.isOf(Items.MACE)) {
            if (MaceTrackerState.maceExists(player.getServer())) {
                // If a Mace already exists, prevent crafting
                SingletonMace.LOGGER.info("Prevented crafting of Mace: one already exists.");
                // Set the crafting result to empty to prevent the item from being taken
                input.setStack(0, ItemStack.EMPTY);
                // Optionally, send a message to the player
                player.sendMessage(net.minecraft.text.Text.literal("A Mace already exists on the server!"), true);
                ci.cancel();
            } else {
                // If no Mace exists, allow crafting and set the flag
                MaceTrackerState.setMaceExists(player.getServer(), true);
                SingletonMace.LOGGER.info("Mace crafted. Setting maceExists to true.");
            }
        }
    }
}
