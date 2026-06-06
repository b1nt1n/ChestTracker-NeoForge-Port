# Chest Tracker - Unofficial NeoForge Port

**Client-side item storage and searching for Minecraft 1.21.1 on NeoForge.**

Chest Tracker helps you remember where you stored your items. Open containers as you play, then search your saved storage data later from a client-side GUI. No server-side installation is required.

> **Unofficial port**
>
> This is an unofficial NeoForge 1.21.1 port of [Chest Tracker](https://modrinth.com/mod/chest-tracker) by JackFred. It is not made, maintained, or endorsed by the original author. Permission to publish this port was obtained from the original author. If you use Fabric or Quilt, use the original project instead.

## Short Summary

Use this for the Modrinth short description field:

```text
Unofficial NeoForge port of Chest Tracker: client-side item storage, searching, and container highlighting for Minecraft 1.21.1.
```

## What It Does

Chest Tracker stores remembered container contents locally on your client. This means it can work in singleplayer, multiplayer, and Realms-style environments without requiring the server to install the mod.

You can use it to:

- Find which chest, barrel, shulker, or other remembered container has an item.
- Search saved storage from the in-game Chest Tracker GUI.
- Highlight matching containers in the world.
- Keep separate local storage data through memory banks.
- Display remembered or custom container names.

## Features

- Client-side storage memory.
- Main searchable storage browser.
- Item search from the Chest Tracker GUI.
- World highlights for matching containers.
- Remembered/custom container names.
- Local memory banks.
- Works without a server-side mod.

## Usage

1. Open containers while playing so Chest Tracker can remember their contents.
2. Press the **Grave** key, usually `` ` ``, to open the Chest Tracker GUI.
3. Click an item in the GUI to search for it in remembered containers.
4. Use the search bar and filters to narrow down stored items.

## Requirements

| Requirement | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.x |
| Dependency | YetAnotherConfigLib for NeoForge 1.21.1 |

Fabric API is **not** required for this port.

## Differences From The Original Fabric Version

This port focuses on the core Chest Tracker experience for NeoForge. Some optional Fabric-side integrations from the original project are not included here.

Known differences:

- Targets **NeoForge 1.21.1** only.
- No Fabric API dependency.
- No ModMenu integration.
- Litematica integration is not included.
- Jade/WTHIT integration is not included.
- Shulker Box Tooltip integration is not included.
- Searchables integration is disabled in this build.

## Compatibility Notes

This mod is client-side. It should not need to be installed on servers.

Because this is an unofficial port, compatibility with large modpacks may vary. If you find a crash or broken behavior, include your latest log and mod list when reporting it.

## Credits

- Original mod: [Chest Tracker](https://modrinth.com/mod/chest-tracker)
- Original author: JackFred
- Original source: [GitHub](https://github.com/JackFred2/ChestTracker)

All original project ownership and credit belong to the original author. This project is only an unofficial NeoForge compatibility port.
