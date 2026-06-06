package red.jackf.whereisit.api.search;

import red.jackf.chesttracker.impl.event.Event;
import red.jackf.chesttracker.impl.event.EventFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface NestedItemsGrabber {
    Event<NestedItemsGrabber> EVENT = EventFactory.createArrayBacked(NestedItemsGrabber.class, listeners -> source -> {
        List<ItemStack> result = new ArrayList<>();
        ItemContainerContents contents = source.get(DataComponents.CONTAINER);
        if (contents != null) {
            contents.nonEmptyItems().forEach(result::add);
        }
        for (NestedItemsGrabber listener : listeners) {
            listener.grab(source).forEach(result::add);
        }
        return result.stream();
    });

    static Stream<ItemStack> get(ItemStack source) {
        return EVENT.invoker().grab(source);
    }

    Stream<ItemStack> grab(ItemStack source);
}
