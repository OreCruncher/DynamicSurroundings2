var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');

var SOUND_ENGINE_LOAD = ASM.mapMethod("func_148608_i");
var SOUND_SYSTEM_INITIALIZE = ASM.mapMethod("func_216404_a");
var SOUND_MANAGER = ASM.mapField("field_217937_g");

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
