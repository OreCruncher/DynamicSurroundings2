### SoundControl-1.15.2-1.0.0.0
**Requirements**
* Forge 1.15.2-31.2.44+
* 100% client side; no server side deployment needed

**Changes**
* Disable block occlusion by default.  To enable change "Enable Sound Occlusion Calculations" to true.

### SoundControl-1.14.4-0.1.1.0
**Requirements**
* Forge 1.14.4-28.1.96+
* 100% client side; no server side deployment needed

**Fixes**
* Server side crash when mod present on dedicated server

### SoundControl-1.14.4-0.1.0.0
**Requirements**
* Forge 1.14.4-28.1.96+
* 100% client side; no server side deployment needed

**What's New**
* Spanish translation (thanks ruchom4u!)

**Fixes**
* NPE playing startup sound when acoustic factory cannot be aquired
* Allow OGG file to be reselected if the same sound instance is replayed (Environs support)

**Changes**
* Additional function in JavaScript library
* ASM to increase streaming sound source count to 10
* F3 diagnostics will display detailed debug info only when mod debug tracing is enabled

### SoundControl-1.14.4-0.0.5.0
**Requirements**
* Forge 1.14.4-28.1.96+
* 100% client side; no server side deployment needed

**Fixes**
* OpenAL crash when attempting to mute sound when Minecraft's sound engine is not initialized

**Changes**
* Library work to support Mob Effects and Environs
* Config option to replace **client side** randomizers with a faster algorithm

### SoundControl-1.14.4-0.0.4.0
**Requirements**
* Forge 1.14.4-28.1.96+

**What's New**
* Config option to disable sound occlusion tracing.  In other words, you can now turn off sound muffling if the sound is positioned behind blocks.

**Fixes**
* NPE when changing mipmap levels when enhanced sounds not enabled

**Changes**
* Library work to support Mob Effects

### SoundControl-1.14.4-0.0.3.0
**Requirements**
* Forge 1.14.4-28.1.96+

**Fixes**
* Sounds would stop playing when a resource pack reload would occur (or F3+T)

### SoundControl-1.14.4-0.0.2.0
**Requirements**
* Forge 1.14.4-28.1.96+

**What's New**
* Convert stereo sounds to mono on the fly
* Internal changes to support Dynamic Surroundings: Mob Effects

### SoundControl-1.14.4-0.0.1.0
**Requirements**
* Forge 1.14.4-28.1.96+

**What's New**
* Initial release for 1.14.4
* Folds in the following features from Dynamic Surroundings: Sound Blocking, Culling, and Volume control for individual sounds
* Folds in the core of Sound Physics to provide enhanced sound experience in caves and enclosed spaces
