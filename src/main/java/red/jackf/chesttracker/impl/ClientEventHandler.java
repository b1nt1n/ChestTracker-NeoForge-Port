package red.jackf.chesttracker.impl;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;

public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen == null && client.getOverlay() == null) {
            while (ChestTracker.OPEN_GUI != null && ChestTracker.OPEN_GUI.consumeClick()) {
                ChestTracker.openInGame(client, null);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) return;
        MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> bank.getMetadata().incrementLoadedTime());
    }
}
