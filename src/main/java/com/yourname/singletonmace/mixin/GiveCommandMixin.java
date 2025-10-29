package com.yourname.singletonmace.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.yourname.singletonmace.MaceTrackerState;
import com.yourname.singletonmace.SingletonMace;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.GiveCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    /**
     * Injects before the execution of the give command.
     * This allows us to prevent giving a Mace if one already exists on the server.
     */
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void singletonmace_execute(CommandContext<ServerCommandSource> context, ItemStringReader.ItemResult item, int amount, CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = item.createStack(amount, false);
        if (stack.isOf(Items.MACE)) {
            if (MaceTrackerState.maceExists(context.getSource().getServer())) {
                SingletonMace.LOGGER.info("Prevented /give of Mace: one already exists.");
                context.getSource().sendError(Text.literal("A Mace already exists on the server!"));
                cir.setReturnValue(0); // Return 0 to indicate command failure
            } else {
                // If no Mace exists, allow the command and set the flag
                MaceTrackerState.setMaceExists(context.getSource().getServer(), true);
                SingletonMace.LOGGER.info("Mace given via command. Setting maceExists to true.");
            }
        }
    }
}
