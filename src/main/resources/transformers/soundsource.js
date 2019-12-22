var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');

var SOURCE_PLAY = ASM.mapMethod("func_216438_c");
var SOURCE_TICK = ASM.mapMethod("func_216434_i");
var SOURCE_STOP = ASM.mapMethod("func_216436_b");

function log(message)
{
    print("[SoundControl Transformer - SoundSource]: " + message);
}

function initializeCoreMod()
{
    return {
        "soundsource_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.audio.SoundSource"]; }
            },
            "transformer": function(classNode) {
                // Add a field to cache our data
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "sndctrl_context", "Ljava/lang/Object;", null, null));
                log("Added context field");

                var playCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "tick",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                insertMethod(classNode, SOURCE_PLAY, playCall);
                log("Hooked SoundSource.play()");

                // Don't reuse - framework doesn't like that
                var tickCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "tick",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                insertMethod(classNode, SOURCE_TICK, tickCall);
                log("Hooked SoundSource.tick()");

                var stopCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "stopSoundPlay",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                insertMethod(classNode, SOURCE_STOP, stopCall);
                log("Hooked SoundSource.stop()");

                return classNode;
            }
        }
    };
}

function insertMethod(classNode, targetMethod, methodImpl)
{
    var newInstructions = new InsnList();
    newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    newInstructions.add(methodImpl);
    var method = findMethod(classNode, targetMethod);
    method.instructions.insert(newInstructions);
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
