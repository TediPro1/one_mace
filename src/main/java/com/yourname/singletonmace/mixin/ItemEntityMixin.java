package com.yourname.singletonmace.mixin;

import com.yourname.singletonmace.MaceTrackerState;
import com.yourname.singletonmace.SingletonMace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStack();
    @Shadow private int itemAge;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Injects at the head of the tick method to check for despawning Maces.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void singletonmace_tick(CallbackInfo ci) {
        if (!this.getWorld().isClient && this.getStack().isOf(Items.MACE)) {
            // Default despawn time is 6000 ticks (5 minutes)
            if (this.itemAge >= 6000 - 1) { // Check one tick before actual despawn
                MinecraftServer server = this.getWorld().getServer();
                if (server != null) {
                    MaceTrackerState.setMaceExists(server, false);
                    SingletonMace.LOGGER.info("Mace despawned. Setting maceExists to false.");
                }
            }
        }
    }

    /**
     * Injects before the item entity is removed due to damage.
     * Checks if the item is a Mace and the damage source is destructive.
     */
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove(Lnet/minecraft/entity/Entity$RemovalReason;)V"), cancellable = true)
    private void singletonmace_damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getWorld().isClient && this.getStack().isOf(Items.MACE)) {
            // Check for destructive damage sources
            if (source.isFire() || source.isMagic() || source.isOutOfWorld() || source.isExplosive() || source.isProjectile()) {
                MinecraftServer server = this.getWorld().getServer();
                if (server != null) {
                    MaceTrackerState.setMaceExists(server, false);
                    SingletonMace.LOGGER.info("Mace destroyed by damage source (fire/magic/void/explosion/projectile). Setting maceExists to false.");
                }
            }
        }
    }
}
