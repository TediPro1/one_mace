package com.yourname.singletonmace.mixin;

import com.yourname.singletonmace.MaceTrackerState;
import com.yourname.singletonmace.SingletonMace;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    /**
     * Injects into the `insertStack` method of PlayerInventory.
     * This is a general hook that can catch items being added to the inventory,
     * including from creative mode, loot tables, or other sources.
     * If the item being inserted is a Mace and one already exists, it prevents the insertion.
     */
    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void singletonmace_insertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(Items.MACE)) {
            PlayerInventory inventory = (PlayerInventory) (Object) this;
            MinecraftServer server = inventory.player.getServer();

            if (server != null && MaceTrackerState.maceExists(server)) {
                SingletonMace.LOGGER.info("Prevented insertion of Mace into player inventory: one already exists.");
                inventory.player.sendMessage(Text.literal("A Mace already exists on the server!"), true);
                cir.setReturnValue(false); // Prevent insertion
            } else if (server != null && !MaceTrackerState.maceExists(server)) {
                // If no Mace exists, allow insertion and set the flag
                MaceTrackerState.setMaceExists(server, true);
                SingletonMace.LOGGER.info("Mace inserted into player inventory. Setting maceExists to true.");
            }
        }
    }

    /**
     * Injects into the `insertStack` method with slot parameter.
     * This is another overload of insertStack that might be called.
     */
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void singletonmace_insertStack_slot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(Items.MACE)) {
            PlayerInventory inventory = (PlayerInventory) (Object) this;
            MinecraftServer server = inventory.player.getServer();

            if (server != null && MaceTrackerState.maceExists(server)) {
                SingletonMace.LOGGER.info("Prevented insertion of Mace into player inventory (slotted): one already exists.");
                inventory.player.sendMessage(Text.literal("A Mace already exists on the server!"), true);
                cir.setReturnValue(false); // Prevent insertion
            } else if (server != null && !MaceTrackerState.maceExists(server)) {
                // If no Mace exists, allow insertion and set the flag
                MaceTrackerState.setMaceExists(server, true);
                SingletonMace.LOGGER.info("Mace inserted into player inventory (slotted). Setting maceExists to true.");
            }
        }
    }
}
