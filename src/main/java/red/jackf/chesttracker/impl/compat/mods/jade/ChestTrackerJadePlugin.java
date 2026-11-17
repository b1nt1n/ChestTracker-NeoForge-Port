package red.jackf.chesttracker.impl.compat.mods.jade;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;

import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ChestTrackerJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadeClientContentsPreview.INSTANCE, Block.class);
        registration.markAsClientFeature(JadeClientContentsPreview.ID);
    }
}
