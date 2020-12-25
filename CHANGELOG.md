> ### DynamicSurroundings-1.16.4-4.0.1.2
**Requirements**
* Forge 1.16.4-35.1.10
* 100% client side; no server side deployment needed

**What's New**
* Started roughing in the /dsdump command.  The intent is to have the ability to dump out the various registries and runtime configurations to disk.  This is WIP.

**Fixes**
* Crash with the Undergarden dimension from the Undergarden mod
* Issue with primitive sound handling.  This means a better default sound behavior for blocks that do not have a specific configuration.
* Incremental work on supporting the new MC 1.16.4 blocks

> ### DynamicSurroundings-1.16.4-4.0.1.1
**Requirements**
* Forge 1.16.4-35.1.10
* 100% client side; no server side deployment needed

**Fixes**
* Crash when connecting to a remote server.
* Version update being indicated when you have the current version installed.

> ### DynamicSurroundings-1.16.4-4.0.1.0
**Requirements**
* Forge 1.16.4-35.1.10
* 100% client side; no server side deployment needed

Welcome to 1.16.4!  Unlike the 1.14.x and 1.15.x versions (Sound Control, Environs, and MobEffects) this JAR release combines all of them into a single JAR.  This is reflected in the Forge mod lists.

Here are some differences in this release vs. Dynamic Surroundings 1.12.2:

* Dynamic Surroundings 1.16.4 is licensed GPL-3 rather than MIT.  This does not affect your ability to add the mod to modpacks.
* Sound effects are built into Dynamic Surroundings:
  * The area around where a sound is played is scanned for reflective surfaces.  This will result in some amount of reverb being added.  This is apparent in large caves, or in houses with reflective surfaces.
  * Reflectivity of a block varies based on its material.  For example, a stone block is more reflective than wood planks, which in turn is more reflective than wool.  If you find your base having too much reverb add some sound padding/absorption material.
  * Sound occlusion is turned off by default (it needs some tuning).  Sound occlusion happens when a sound location is behind other blocks.  The more blocks the sound has to travel to the listener the more occluded the sound becomes.  Blocks like glass don't occlude much, whereas blocks like wool make for a good sound insulator.  Turn it on and try it out.
  * This complex sound processing occurs on a background worker thread, not on the rendering thread (main client side thread).  This means that impact of processing should be minimal.
  * Positioned sounds will be converted to mono on the fly if necessary.  For spacial sound processing to happen it needs to be in mono format. 
* There are no weather related features.  This is planned for the future.
* The Aurora is currently disabled in code.  I am having difficulty migrating the code to 1.16.4 and didn't want to hold up releasing.
* There are no in-game configuration menus.  Unlike prior versions Forge does not have this support.  I am looking for any libraries that provides this capability, so I can minimize the amount of GUI code I have to write.  (I dislike writing GUIs.)
* The Minecraft sound engine finally supports looping of streaming sounds.  This means that biome sounds that play in the background as well as continuous positioned sounds (waterfall) will not have stutters and the like.
* Dynamic Surroundings Nether background sound was removed.  Mojang did a pretty good job with the sound profile of the Nether and felt this was not needed.

The underlying configuration Jsons have changed.  There are several reasons for this:

* I am reworking how Dynamic Surroundings is configured, with the goal of making it flexible and easier to use.
* Be able to provide configs on disk that override any Jsons that are embedded in JARs or datapacks.
* Have more granular Json files rather than larger monolithic ones.
* Another mod could provide Dynamic Surroundings support for their work by adding addition information into their assets that Dynamic Surroundings can discover.

Here are some things you may encounter using this ALPHA:

* Minecraft 1.16.4 changed biomes, a lot.  I have noticed that some biome mods do not properly tag their biomes.  Dynamic Surroundings uses these tags to identify what sound features to give the biome.
* Particles could render strangely (water ripples, footsteps, etc.).  I think I have the proper rendering, but other mods may indirectly interfere.
* Json configs will load/process when you log in to a world.  This is to ensure that the client has the most up to date registries and datapacks from the server before applying its rules.  This may have some impact on login processing time.


-- OreCruncher
