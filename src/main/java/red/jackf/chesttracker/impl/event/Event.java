package red.jackf.chesttracker.impl.event;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public interface Event<T> {
    ResourceLocation DEFAULT_PHASE = ResourceLocation.withDefaultNamespace("default");

    T invoker();

    void register(T listener);

    void register(ResourceLocation phase, T listener);
}
