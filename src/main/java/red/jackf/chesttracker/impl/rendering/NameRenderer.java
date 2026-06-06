package red.jackf.chesttracker.impl.rendering;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryKey;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.whereisit.client.api.RenderUtils;

import java.util.Map;
import java.util.Set;

public class NameRenderer {
    public static void setup() {
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            if (ChestTrackerConfig.INSTANCE.instance().debug.disableContainerNames) return;

            MemoryBankAccessImpl.INSTANCE.getLoadedInternal().ifPresent(bank -> {
                if (bank.getMetadata().getCompatibilitySettings().nameRenderMode == NameRenderMode.DISABLED) return;
                bank.getKey(ProviderUtils.getPlayersCurrentKey()).ifPresent(key ->
                        NameRenderer.renderNamesForKey(event.getCamera(), bank, key, Minecraft.getInstance().hitResult));
            });
        }
    }

    private static void renderNamesForKey(Camera camera, MemoryBankImpl bank, MemoryKey key, @Nullable HitResult hitResult) {
        @Nullable Memory focused = null;

        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() != HitResult.Type.MISS) {
            var targetedMemory = key.get(blockHitResult.getBlockPos());
            if (targetedMemory.isPresent() && targetedMemory.get().hasCustomName()) {
                focused = targetedMemory.get();
            }
        }

        if (bank.getMetadata().getCompatibilitySettings().nameRenderMode == NameRenderMode.FULL) {
            Map<BlockPos, Memory> named = key.getNamedMemories();
            final int maxRangeSq = ChestTrackerConfig.INSTANCE.instance().rendering.nameRange * ChestTrackerConfig.INSTANCE.instance().rendering.nameRange;
            Set<BlockPos> alreadyRendering = RenderUtils.getCurrentlyRenderedWithNames();
            for (var entry : named.entrySet()) {
                if (entry.getValue() == focused) continue;
                if (alreadyRendering.contains(entry.getKey())) continue;
                if (entry.getKey().distToCenterSqr(camera.getPosition()) < maxRangeSq) {
                    Component name = entry.getValue().renderName();
                    if (name == null) continue;
                    RenderUtils.scheduleLabelRender(entry.getValue().getCenterPosition().add(0, 1, 0), name);
                }
            }
        }

        if (focused != null) {
            Component name = focused.renderName();
            if (name != null) {
                RenderUtils.scheduleLabelRender(focused.getCenterPosition().add(0, 1, 0), name, true);
            }
        }
    }
}
