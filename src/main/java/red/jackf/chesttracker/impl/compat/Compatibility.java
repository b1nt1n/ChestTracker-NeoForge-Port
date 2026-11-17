package red.jackf.chesttracker.impl.compat;

public interface Compatibility {
    boolean SEARCHABLES = net.neoforged.fml.ModList.get().isLoaded("searchables");
}
