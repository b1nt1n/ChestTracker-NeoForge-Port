package red.jackf.chesttracker.impl;

import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.ModList;
import net.minecraft.world.level.block.Blocks;
import red.jackf.chesttracker.api.ChestTrackerPlugin;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.api.providers.*;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.defaults.*;
import red.jackf.chesttracker.impl.compat.mods.ShareEnderChestIntegration;
import red.jackf.chesttracker.impl.compat.servers.hypixel.HypixelProvider;
import red.jackf.chesttracker.impl.gui.util.CTTitleOverrideDuck;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultChestTrackerPlugin implements ChestTrackerPlugin {
    private final AtomicBoolean fromEnderChestCommand = new AtomicBoolean(false);

    @Override
    public void load() {
        GetCustomName.EVENT.register(EventPhases.FALLBACK_PHASE, (screen) -> {
            Component title = ((CTTitleOverrideDuck) screen).chesttracker$getOriginalTitle();

            if (containsTranslatable(title))
                return ResultHolder.pass();

            // if it's not translatable, it's very likely a custom name
            return ResultHolder.value(title);
        });

        ScreenBlacklist.add(
                // workstations with no item retention
                CartographyTableScreen.class,
                EnchantmentScreen.class,
                GrindstoneScreen.class,
                ItemCombinerScreen.class,
                LoomScreen.class,
                StonecutterScreen.class,
                BeaconScreen.class,

                // inventory (surv & creative)
                EffectRenderingInventoryScreen.class
        );

        ProviderUtils.registerProvider(DefaultProvider.INSTANCE);
        ProviderUtils.registerProvider(new HypixelProvider());

        // todo configurable
        Set<String> enderChestCommands = Set.of("ec", "ender", "enderchest", "echest");

        DefaultProviderCommandSent.EVENT.register((provider, command) -> fromEnderChestCommand.set(enderChestCommands.contains(command)));

        DefaultProviderScreenOpen.EVENT.register((provider, context) -> {
            if (fromEnderChestCommand.get()) {
                context.setMemoryLocation(MemoryLocation.override(CommonKeys.ENDER_CHEST_KEY, BlockPos.ZERO));
                return true;
            } else {
                return false;
            }
        });

        DefaultProviderScreenClose.EVENT.register(EventPhases.FALLBACK_PHASE, DefaultChestTrackerPlugin::defaultMemoryCreator);

        DefaultProviderScreenClose.EVENT.register(EventPhases.DEFAULT_PHASE, this::enderChestMemoryCreator);

        DefaultProviderMemoryLocation.EVENT.register(EventPhases.FALLBACK_PHASE, cbs -> {
            var current = ProviderUtils.getPlayersCurrentKey();
            if (current.isEmpty()) return ResultHolder.pass();

            if (!ProviderUtils.defaultShouldRemember(cbs)) return ResultHolder.pass();

            List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(cbs.level(), cbs.blockState(), cbs.pos());
            BlockPos rootPos = connected.get(0);

            return ResultHolder.value(MemoryLocation.inWorld(current.get(), rootPos));
        });

        DefaultProviderMemoryLocation.EVENT.register(EventPhases.DEFAULT_PHASE, cbs -> {
            if (cbs.blockState().getBlock() == Blocks.ENDER_CHEST || fromEnderChestCommand.get()) {
                return ResultHolder.value(MemoryLocation.override(CommonKeys.ENDER_CHEST_KEY, BlockPos.ZERO));
            }

            return ResultHolder.pass();
        });

        ShareEnderChestIntegration.setup();
    }

    // Visits without decomposing
    private static boolean containsTranslatable(Component component) {
        if (component.getContents() instanceof TranslatableContents) return true;
        for (Component sibling : component.getSiblings()) {
            if (containsTranslatable(sibling)) return true;
        }
        return false;
    }

    private static ResultHolder<DefaultProviderScreenClose.Result> defaultMemoryCreator(ServerProvider provider, ScreenCloseContext context) {
        var cbsOpt = InteractionTracker.INSTANCE.getLastBlockSource();
        var memoryLocationOpt = cbsOpt.flatMap(provider::getMemoryLocation);

        if (cbsOpt.isEmpty() || memoryLocationOpt.isEmpty() || memoryLocationOpt.get().isOverride()) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) return ResultHolder.pass();
            var currentKey = red.jackf.chesttracker.api.providers.ProviderUtils.getPlayersCurrentKey();
            if (currentKey.isEmpty()) return ResultHolder.pass();

            BlockPos rootPos = cbsOpt.map(red.jackf.chesttracker.api.ClientBlockSource::pos).orElseGet(player::blockPosition);

            return ResultHolder.value(MemoryBuilder.create(context.getItems())
                    .withCustomName(context.getCustomTitle().orElse(null))
                    .inContainer(net.minecraft.world.level.block.Blocks.CHEST)
                    .toResult(currentKey.get(), rootPos));
        }

        var cbs = cbsOpt.get();
        var memoryLocation = memoryLocationOpt.get();
        List<BlockPos> connectedBlocks = ConnectedBlocksGrabber.getConnected(cbs.level(), cbs.blockState(), cbs.pos());
        BlockPos rootPos = connectedBlocks.get(0);

        return ResultHolder.value(MemoryBuilder.create(context.getItems())
                .withCustomName(context.getCustomTitle().orElse(null))
                .inContainer(cbs.blockState().getBlock())
                .otherPositions(connectedBlocks.stream()
                        .filter(pos -> !pos.equals(rootPos))
                        .toList())
                .toResult(memoryLocation.memoryKey(), rootPos));
    }

    private ResultHolder<DefaultProviderScreenClose.Result> enderChestMemoryCreator(ServerProvider provider, ScreenCloseContext context) {
        if (!this.fromEnderChestCommand.getAndSet(false)) {
            var cbs = InteractionTracker.INSTANCE.getLastBlockSource();
            if (cbs.isEmpty()) return ResultHolder.pass();

            if (!cbs.get().blockState().is(Blocks.ENDER_CHEST)) return ResultHolder.pass();
        }

        return ResultHolder.value(MemoryBuilder.create(context.getItemsWithEmpty())
                .inContainer(Blocks.ENDER_CHEST)
                .toResult(CommonKeys.ENDER_CHEST_KEY, BlockPos.ZERO));
    }
}
