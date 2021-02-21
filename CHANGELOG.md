> ### DynamicSurroundings-1.16.4-4.0.3.11
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+, and JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Mod would not initialize on connect because world load event isn't received
* Derp in detecting super flat worlds

> ### DynamicSurroundings-1.16.4-4.0.3.10
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+, and JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Do not provide fog color for a biome if biome/fog processing is disabled
* Hopefully fixed Paper server connection, again.
* Speculative fix/logging for when the mod crashes during load due to shader compile issue.
* NPE in light level debug HUD

**Changes**
* Improved rendering performance of Aurora shader.  YMMV.
* Changing fog options no longer require a client restart
* Weeping and Twisted vines have a different acoustic.  Based on the weeping vine step sound.  It sounds a bit "ropier" and not like straw.  Let me know what you think.
* Additional context help in the Individual Sound Configuration dialog (CTRL+i while in game).
* Hide debugging HUDs behind flags.
* Re-enabled occlusion of WATERFALL by default
* Normalized the sound volume of waterfall effects
* No biome sounds in Savanna when raining
* Config default changes:
  * Culling interval to 20 from 10
  * Footstep volume scale 40 from 75

> ### DynamicSurroundings-1.16.4-4.0.3.9
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+, and JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Support super flat world properly
* Play footstep acoustic based on the facade attached to a block
  * Example, cable facade on an AE2 network cable
  * Works on any block that implements team.chisel.ctm.api.IFacade
* Added capability to select water ripple style via config
* OpenAL related errors on Debian
* Steam jets will go away once water source is removed
* Setting Friendly mob sound slider to OFF will no longer cause DS to sputter.
* Disable play button click sound in individual sound config menu

**Changes**
* Sounds in the RECORDS category will now have reverb and occlusion.  Separate option to enable/disable occlusion processing.
* Sounds in the WEATHER category will not be occluded by default.   Separate option to enable/disable occlusion processing.
* Sounds in the WATERFALL category will not be occluded by default.  Can be changed in the config.
* Add restart and range info to configuration tooltips
* More granular options to control footstep accenting.  Armor rustle sound option will be reset and have to be disabled if you prefer it disabled.
* Default sound culling interval is now 10 ticks (down from 20).  Maximum cull interval is 6000 ticks (5 minutes).
* Some translations for fr_fr from Mazdallier!  Thanks!

> ### DynamicSurroundings-1.16.4-4.0.3.8
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+, and JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Changes**
* Added sound occlusion toggle to the quick volume settings dialog (CTRL+v)

**Fixes**
* Sounds not playing when connecting to a Paper server.

> ### DynamicSurroundings-1.16.4-4.0.3.7
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+, and JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Work around JavaScript issue using Java 8 with Forge 36.
* Speculative fix for NPE when connecting to server.

> ### DynamicSurroundings-1.16.4-4.0.3.6
**Requirements**
* JAVA 8 w/Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+ w/JAVA 15
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**What's New**
* (WIP) Java 15 support.  This requires Forge 1.16.5+.  I use AdoptOpenJDK and OpenJ9 JVM.

**Fixes**
* Don't do ripple effect for drip particles underwater (Crying Obsidian and Wet Sponges)

**Changes**
* (WIP) Reworked config loading to support resource packs.  Not currently documented.
* Disable enhanced sound processing if Sound Filters is installed.

> ### DynamicSurroundings-1.16.4-4.0.3.5
**Requirements**
* JAVA 8
* Forge 1.16.4-35.1.10+
  * Compatible with 1.16.5-36.0.0+ as well
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Aurora should no longer jump position when player positions around Y=63

**Changes**
* Changed default activation control for Individual Sound Configuration to CTRL+i
* Enabled config menus for child mods that will open the main Dynamic Surroundings option menus.  Intent is to reduce confusion and keep things simple.
* Add option to disable water ripple effect normally generated from rain and water splash effects.  It's under Environs -> Effect Options.
* Adjusted sunrise threshold to be a bit earlier.
* Tweaked sound occlusion algorithm a bit.

> ### DynamicSurroundings-1.16.4-4.0.3.4
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Calculate sound volume from individual sound configs correctly.  Prior sound volumes were way too low.

> ### DynamicSurroundings-1.16.4-4.0.3.3
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**What's new**
* Individual Sound Configuration settings GUI.  Access in game using CTRL+s.  Keybind can be changed in control settings.
  * Game will pause if single player.  It will not pause when playing on a server, so be cautious.

**Fixes**
* Auroras will now render in the right place.  We won't talk about it. :)

**Changes**
* Use vanilla step sound for nether wart blocks rather than organic acoustic.  They won't sound squishy.
* Cull particles from rendering using frustum.  Essentially don't render particles that are not in view.

> ### DynamicSurroundings-1.16.4-4.0.3.2
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**What's New**
* Separate volume slider for waterfall sounds.
* Added in game menu to access volume scale settings for various categories
  * By default, access using CTRL+v; can be changed in options.
  * Will not pause the game.  Useful if you want to adjust settings and see impact in real time.

**Fixes**
* Scaling sounds > 100 using Individual Sound Configuration (max is 400, or 4x volume)
* A speculative fix for OptiFine crashing when rain particles are generated.

**Changes**
* Fences, fence gates, walls, glass panes, and chests no longer count as ceiling blocks when determining if the player is "indoors".

> ### DynamicSurroundings-1.16.4-4.0.3.1
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**Fixes**
* Malformed IArmorMaterial instances from mods triggering crash during startup
* Don't crash when a spurious IMC message gets sent to Dynamic Surroundings

> ### DynamicSurroundings-1.16.4-4.0.3.0
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**What's New**
* Rain splash generation on non-lava liquid will spawn ripples instead of splash.
* Acoustic versions of nether blocks.  Reused new block step sounds and accented with additional sounds.
* Auroras are now enabled.  Look for them in colder biomes, usually in the northern sky.  (May need some additional tuning.)
* Improved primitive sound handling for items and footsteps:
  * For unknown armor items the armor material equip sound is used for toolbar effects.
  * Armor accents are generated based on armor material equip sound if there isn't a more specific acoustic already defined.
  * Footstep sounds will be generated if there isn't a more specific acoustic already defined.
  * Acoustic generation is template based and attempts to give a better experience than a simple sound play.

**Fixes**
* Footprint particles on snow, soul sand, and farm blocks position properly.
* Waterlogged blocks particle effects:
  * Water ripple effects will now generate.
  * Lava/Water dripping into a waterlogged block will play splash and sound.
* Water ripples are now colored based on the fluid dictionary color.  Water will be colored based on biome water color.

**Changes**
* Changed default block occlusion to be less dense.
* Increased attenuation distance of small water fall sound to 16.

> ### DynamicSurroundings-1.16.4-4.0.2.0
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed
* Cloth Config API (Forge) is optional

**What's New**
* Added configuration menus.
  * You will need to add [Cloth Config API Forge](https://www.curseforge.com/minecraft/mc-mods/cloth-config-forge) to get config menus.  Make sure it's the Forge version.
  * If you forget you will get a dialog when you navigate to the config menu for Dynamic Surroundings.
  * Some modpacks already includes the Cloth API (Direwolf20 1.16 for example)
* Added option to disable footstep sounds (WIP).  If disabled:
  * Vanilla step sounds will be played as defined by the underlying block step sound type.
  * Armor/water splash sounds will still be played unless disabled.
  * Walking through items such as vines and reeds will still play effects.

**Fixes**
* Config had the wrong default setting for hiding player potion particles.  It should have been disabled.
* Sometimes persistent particle effects would not disappear when conditions changed.

**Changes**
* Sound stream conversion to mono if configured to do so.  Conversion will happen even if enhanced sound processing is disabled.  There is a config setting to independently enable/disable conversion.

> ### DynamicSurroundings-1.16.4-4.0.1.5
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed

**Fixes**
* Disabling mod update status in the config now works (dsurround-client.toml)
* Don't play step sounds when travelling up/down an elevator from Create mod

**Changes**
* More tweaks to block reflectivity and occlusion
  * Cobblestone and sandstone are less reflective
* Changed the wood log footstep sound.  Not as deep and the tempo is better.
* Decrease toolbar sounds for other players by 25% when played
* Decrease default toolbar master sound volume scale to 35% (from 50%)

> ### DynamicSurroundings-1.16.4-4.0.1.4
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed

**Fixes**
* Fixed calculations related to block occlusion processing
  * Now set to enabled as a default
  * To enable set "Enable Sound Occlusion Calculations" in sndctrl-client.toml to true

**Changes**
* Changed logic where spot sounds play around the player.  They should play a bit further away, lessening the sound impact of some bird sounds (like crows and raptors).

> ### DynamicSurroundings-1.16.4-4.0.1.3
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed

**Changes**
* Cleaned up biome fog processing
  * Use Minecraft's fog color calculations since it is now doing fancy things
  * Use Minecraft/datapack fog colors as a default rather than "white". 
  * Simplified fog visibility calculations.
* Activated support for Serene Seasons.
* Adjusted underground biome sound to activate at Y < 50 rather than 33.

> ### DynamicSurroundings-1.16.4-4.0.1.2
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed

**What's New**
* Started roughing in the /dsdump command.  The intent is to have the ability to dump out the various registries and runtime configurations to disk.  This is WIP.

**Fixes**
* Crash with the Undergarden dimension from the The Undergarden mod.
* Issue with primitive sound handling.  This means a better default sound behavior for blocks that do not have a specific configuration.
* Incremental work on supporting the new MC 1.16.4 blocks.

> ### DynamicSurroundings-1.16.4-4.0.1.1
**Requirements**
* Forge 1.16.4-35.1.10+
* 100% client side; no server side deployment needed

**Fixes**
* Crash when connecting to a remote server.
* Version update being indicated when you have the current version installed.

> ### DynamicSurroundings-1.16.4-4.0.1.0
**Requirements**
* Forge 1.16.4-35.1.10+
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
