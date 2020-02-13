var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');

var SOURCE_PLAY = ASM.mapMethod("func_216438_c");
var SOURCE_TICK = ASM.mapMethod("func_216434_i");
var SOURCE_STOP = ASM.mapMethod("func_216436_b");
var SOURCE_AUDIO_STREAM = ASM.mapMethod("func_216433_a");
var FIELD_AUDIO_STREAM = ASM.mapField("field_216444_e");
var SOURCE_PLAY_BUFFER = ASM.mapMethod("func_216429_a");
var BUFFER_SIZE = ASM.mapMethod("func_216417_a");

var FORMAT = "[soundsource.js] {}";

function initializeCoreMod()
{
    return {
        "sndctrl_soundsource_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.audio.SoundSource"]; }
            },
            "transformer": function(classNode) {
                // Add a field to cache our data
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "sndctrl_context", "Ljava/lang/Object;", null, null));
                ASM.log("INFO", FORMAT, ["Added context field"]);

                var playCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "tick",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                var result = insertMethod(classNode, SOURCE_PLAY, playCall);
                if (result)
                    ASM.log("INFO", FORMAT, ["Hooked SoundSource.play()"]);
                else
                    ASM.log("ERROR", FORMAT, ["Will not be able to tick sound on initial creation"]);

                // Don't reuse - framework doesn't like that
                var tickCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "tick",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                result = insertMethod(classNode, SOURCE_TICK, tickCall);
                if (result)
                    ASM.log("INFO", FORMAT, ["Hooked SoundSource.tick()"]);
                 else
                    ASM.log("ERROR", FORMAT, ["Will not be able to tick sound"]);

                var stopCall = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "stopSoundPlay",
                    "(Lnet/minecraft/client/audio/SoundSource;)V",
                    ASM.MethodType.STATIC
                );

                result = insertMethod(classNode, SOURCE_STOP, stopCall);
                if (result)
                    ASM.log("INFO", FORMAT, ["Hooked SoundSource.stop()"]);
                else
                    ASM.log("ERROR", FORMAT, ["Will not be able to cleanup sound when it stops"]);

                var audioFormat = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/handlers/SoundFXProcessor",
                    "playBuffer",
                    "(Lnet/minecraft/client/audio/SoundSource;Lnet/minecraft/client/audio/AudioStreamBuffer;)Lnet/minecraft/client/audio/AudioStreamBuffer;",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(audioFormat);
                newInstructions.add(new VarInsnNode(Opcodes.ASTORE, 1));

                var method = findMethod(classNode, SOURCE_PLAY_BUFFER);
                if (method !== null) {
                    method.instructions.insert(newInstructions);
                    ASM.log("INFO", FORMAT, ["Hooked SoundSource.playBuffer()"]);
                } else {
                    ASM.log("WARN", FORMAT, ["Will not be able to transform sound streams to mono on the fly"]);
                }

                newInstructions = new InsnList();
                newInstructions.add(new LdcInsnNode(16));
                newInstructions.add(new InsnNode(Opcodes.IMUL));

                method = findMethod(classNode, BUFFER_SIZE);
                if (method !== null) {
                    var theReturn = ASM.findFirstInstruction(method, Opcodes.IRETURN);
                    if (theReturn !== null) {
                        method.instructions.insertBefore(theReturn, newInstructions);
                        ASM.log("INFO", FORMAT, ["Hooked SoundSource.bufferSize()"]);
                    }
                } else {
                    ASM.log("WARN", FORMAT, ["Unable to increase sound buffer size; looping sounds will sound strange"]);
                }

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

    if (method !== null) {
        method.instructions.insert(newInstructions);
        return true;
    } else {
        return false;
    }
}

function findMethod(classNode, methodName)
{
    for each (var method in classNode.methods)
    {
        if (method.name == methodName)
            return method;
    }
    ASM.log("WARN", "Method not found: {}", [methodName]);
    return null;
}
