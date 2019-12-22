var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

var SOUND_ENGINE_LOAD = ASM.mapMethod("func_148608_i");
var SOUND_SYSTEM_INITIALIZE = ASM.mapMethod("func_216404_a");
var SOUND_MANAGER = ASM.mapField("field_217937_g");
var GET_CLAMPED_VOLUME = ASM.mapMethod("func_188770_e");
var PLAY_SOUND = ASM.mapMethod("func_148611_c");
var RUN_SOUND_EXECUTOR = ASM.mapMethod("func_217888_a");

function log(message)
{
    print("[SoundControl Transformer - SoundEngine]: " + message);
}

function initializeCoreMod()
{
    return {
        "soundsystem_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.audio.SoundEngine"]; }
            },
            "transformer": function(classNode) {

                var initCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/SoundUtils",
                    "initialize",
                    "(Lnet/minecraft/client/audio/SoundSystem;)V",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/audio/SoundEngine", SOUND_MANAGER, "Lnet/minecraft/client/audio/SoundSystem;"));
                newInstructions.add(initCall);

                var targetMethod = findMethod(classNode, SOUND_ENGINE_LOAD);
                ASM.insertInsnList(targetMethod, ASM.MethodType.VIRTUAL, "net/minecraft/client/audio/SoundSystem", SOUND_SYSTEM_INITIALIZE, "()V", newInstructions, ASM.InsertMode.INSERT_AFTER);
                log("Hooked SoundEngine.load()");


                var clamped = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundVolumeEvaluator",
                    "getClampedVolume",
                    "(Lnet/minecraft/client/audio/ISound;)F",
                    ASM.MethodType.STATIC
                );

                newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(clamped);
                newInstructions.add(new InsnNode(Opcodes.FRETURN));

                var targetMethod = findMethod(classNode, GET_CLAMPED_VOLUME);
                targetMethod.instructions.insert(newInstructions);
                log("Hooked SoundEngine.getClampedVolume()");

                var playSound = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "onSoundPlay",
                    "(Lnet/minecraft/client/audio/ISound;Lnet/minecraft/client/audio/ChannelManager$Entry;)V",
                    ASM.MethodType.STATIC
                );

                newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 14));
                newInstructions.add(playSound);

                var targetMethod = findMethod(classNode, PLAY_SOUND);
                ASM.insertInsnList(targetMethod, ASM.MethodType.VIRTUAL, "net/minecraft/client/audio/ChannelManager$Entry", RUN_SOUND_EXECUTOR, "(Ljava/util/function/Consumer;)V", newInstructions, ASM.InsertMode.INSERT_AFTER);
                log("Hooked SoundEngine.play()");

                return classNode;
            }
        }
    };
}

function findMethod(classNode, methodName)
{
    for each (var method in classNode.methods)
    {
        if (method.name == methodName)
            return method;
    }
    log("Method not found: " + methodName);
    return null;
}
