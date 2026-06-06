package red.jackf.chesttracker.impl.providers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.InteractionTracker;
import red.jackf.chesttracker.impl.util.CachedClientBlockSource;

import java.util.Optional;

public class InteractionTrackerImpl implements InteractionTracker {
    public static final InteractionTrackerImpl INSTANCE = new InteractionTrackerImpl();
    private @Nullable ClientBlockSource lastSource = null;

    public static void setup() {
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    @Override
    public Optional<ClientLevel> getPlayerLevel() {
        if (Minecraft.getInstance().level == null) return Optional.empty();
        return Optional.of(Minecraft.getInstance().level);
    }

    @Override
    public Optional<ClientBlockSource> getLastBlockSource() {
        return Optional.ofNullable(lastSource);
    }

    public void clear() {
        this.lastSource = null;
    }

    public void setLastBlockSource(ClientBlockSource source) {
        this.lastSource = source;
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Level level = event.getLevel();
            if (event.getHand() == InteractionHand.MAIN_HAND && level instanceof ClientLevel clientLevel) {
                INSTANCE.setLastBlockSource(new CachedClientBlockSource(clientLevel, event.getPos()));
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            INSTANCE.clear();
        }
    }
}
