package com.yourname.singletonmace.mixin;

import com.yourname.singletonmace.MaceTrackerState;
import com.yourname.singletonmace.SingletonMace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /**
     * Injects before an item takes damage.
     * If the item is a Mace and the damage would cause it to break, set maceExists to false.
     */
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V", shift = At.Shift.BEFORE), cancellable = true)
    private <T extends LivingEntity> void singletonmace_damage(int amount, net.minecraft.util.Random random, T entity, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isOf(Items.MACE)) {
            if (self.getDamage() + amount >= self.getMaxDamage()) {
                // Mace is about to break
                MinecraftServer server = entity.getServer();
                if (server != null) {
                    MaceTrackerState.setMaceExists(server, false);
                    SingletonMace.LOGGER.info("Mace broke. Setting maceExists to false.");
                }
            }
        }
    }
}
