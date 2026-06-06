# Chest Tracker - Unofficial NeoForge Port

Client-side item storage and searching for Minecraft 1.21.1 on NeoForge.

This is an unofficial NeoForge 1.21.1 port of [Chest Tracker](https://modrinth.com/mod/chest-tracker) by JackFred. It is not made, maintained, or endorsed by the original author. Permission to publish this port was obtained from the original author.

## What It Does

Chest Tracker remembers container contents locally on your client, so you can search for stored items later without requiring a server-side mod.

Features included in this port:

- Client-side storage memory.
- Main searchable storage browser.
- Item search from the Chest Tracker GUI.
- World highlights for matching containers.
- Remembered/custom container names.
- Local memory banks.
- NeoForge 1.21.1 support.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- YetAnotherConfigLib for NeoForge 1.21.1
- Java 21

## Differences From The Original Fabric Version

This port focuses on the core Chest Tracker experience for NeoForge. Some optional Fabric-side integrations from the original project are not included here.

Known differences:

- Targets NeoForge 1.21.1 only.
- Fabric API is not required.
- ModMenu integration is not included.
- Litematica integration is not included.
- Jade/WTHIT integration is not included.
- Shulker Box Tooltip integration is not included.
- Searchables integration is disabled in this build.

## Building

```powershell
.\gradlew.bat build
```

The built jar will be in:

```text
build/libs/chesttracker-2.6.7+1.21.1-neoforge.jar
```

## Credits

- Original mod: [Chest Tracker](https://modrinth.com/mod/chest-tracker)
- Original author: JackFred
- Original source: [GitHub](https://github.com/JackFred2/ChestTracker)

All original project ownership and credit belong to the original author. This repository is only an unofficial NeoForge compatibility port.
