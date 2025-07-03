# Runtime Datagen

A cursed mod that allows you to automatically run and reload datagen from inside your world.

The mod's functionality is only enabled when you are in a development environment.

## Installation

- Install the mod manually from [GitHub Releases](https://github.com/Mim1q/RuntimeDatagen/releases)

## Usage

This mod makes a few assumptions about the environment:
- your mod must have its generated resources in `src/main/generated/` or a similar path, relative to your `gradlew` file;
- you must have the `fabric-api.datagen.output-dir` property set in your Minecraft Client configuration
  (This may just  be copied from the Data Generation configuration).
- you have reloaded your sources in case you've changed anything 

After that is done, **press the O key** while in-world (configurable under Key Bind settings), and let the mod do its job :)

## Notes

In case this mod gets flagged as malicious due to its usage of reflection and process spawning, feel free to verify the 
code in [the main class](src/main/java/dev/mim1q/runtimedatagen/RuntimeDatagenClient.java), compile it yourself or 
incorporate it into your own project (CC0 license).
