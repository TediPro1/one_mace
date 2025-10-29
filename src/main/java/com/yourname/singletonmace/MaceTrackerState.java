package com.yourname.singletonmace;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;

public class MaceTrackerState implements PersistentStateManager.PersistentState {
    private static final Identifier ID = new Identifier("singletonmace", "mace_tracker");
    private boolean maceExists = false;

    public MaceTrackerState() {
        // Default constructor for PersistentState
    }

    // --- Persistence Methods ---

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("maceExists", maceExists);
        return nbt;
    }

    public static MaceTrackerState readNbt(NbtCompound nbt) {
        MaceTrackerState state = new MaceTrackerState();
        state.maceExists = nbt.getBoolean("maceExists");
        return state;
    }

    // --- Static Accessors ---

    public static MaceTrackerState get(MinecraftServer server) {
        // Get the overworld's PersistentStateManager
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // Get or create the state
        MaceTrackerState state = persistentStateManager.getOrCreate(
                MaceTrackerState::readNbt,
                MaceTrackerState::new,
                ID.toString()
        );
        return state;
    }

    public static boolean maceExists(MinecraftServer server) {
        if (server == null) {
            SingletonMace.LOGGER.warn("Attempted to check maceExists with null server. This might lead to inaccurate results.");
            // In cases where server is null (e.g., early mod initialization or client-side context),
            // we cannot reliably get the state. Assume false for safety.
            return false;
        }
        return get(server).maceExists;
    }

    public static void setMaceExists(MinecraftServer server, boolean exists) {
        if (server == null) {
            SingletonMace.LOGGER.error("Attempted to set maceExists with null server. State will not be updated.");
            return;
        }
        MaceTrackerState state = get(server);
        if (state.maceExists != exists) { // Only update and mark dirty if the state actually changes
            state.maceExists = exists;
            state.markDirty(); // Mark dirty to ensure the state is saved
            SingletonMace.LOGGER.info("Mace existence flag set to: " + exists);
        }
    }

    // --- Server Lifecycle Events (for initialization and cleanup) ---

    public static void registerServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Ensure the state is loaded when the server starts
            get(server);
            SingletonMace.LOGGER.info("MaceTrackerState initialized. Mace exists: " + get(server).maceExists);
        });

        // No need for SERVER_STOPPING or AFTER_SAVE events specifically for marking dirty
        // as get() and setMaceExists() already mark dirty when changes occur.
        // The PersistentStateManager handles saving dirty states automatically.
    }
}
