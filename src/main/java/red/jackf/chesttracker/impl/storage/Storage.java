package red.jackf.chesttracker.impl.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.storage.backend.Backend;

import java.util.Collection;
import java.util.Optional;

public class Storage {
    private static final Logger LOGGER = ChestTracker.getLogger("Storage");
    private static Backend backend;

    public static void setBackend(Backend backend) {
        Storage.backend = backend;
    }

    public static void setup() {
        ChestTrackerConfig.INSTANCE.instance().storage.storageBackend.load();
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    public static Optional<Metadata> loadMetadata(String id) {
        Optional<MemoryBankImpl> existing = MemoryBankAccessImpl.INSTANCE.getLoadedInternal();
        if (existing.isPresent() && id.equals(existing.get().getId()))
            return Optional.of(existing.get().getMetadata().deepCopy());
        LOGGER.debug("Loading {} metadata using {}", id, backend.getClass().getSimpleName());
        return backend.loadMetadata(id);
    }

    public static Collection<String> getAllIds() {
        return backend.getAllIds();
    }

    public static boolean exists(String id) {
        return backend.exists(id);
    }

    public static void delete(String id) {
        backend.delete(id);
    }

    public static Component getBackendLabel(String memoryBankId) {
        return backend.getDescriptionLabel(memoryBankId);
    }

    public static Optional<MemoryBankImpl> load(String id) {
        Optional<MemoryBankImpl> existing = MemoryBankAccessImpl.INSTANCE.getLoadedInternal();
        if (existing.isPresent() && id.equals(existing.get().getId())) return existing;

        HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        LOGGER.debug("Loading {} using {}", id, backend.getClass().getSimpleName());
        var loaded = backend.load(id, registries);
        if (loaded == null) return Optional.empty();
        loaded.setId(id);
        return Optional.of(loaded);
    }

    public static void save(MemoryBankImpl bank) {
        if (bank == null) {
            LOGGER.warn("Tried to save null Memory Bank");
            return;
        }

        HolderLookup.Provider registries = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : null;
        bank.getMetadata().updateModified();
        backend.save(bank, registries);
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof PauseScreen) {
                MemoryBankAccessImpl.INSTANCE.save();
            }
        }
    }
}
