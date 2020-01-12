var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');

var SOURCE_PLAY = ASM.mapMethod("func_216438_c");
var SOURCE_TICK = ASM.mapMethod("func_216434_i");
var SOURCE_STOP = ASM.mapMethod("func_216436_b");
var SOURCE_AUDIO_STREAM = ASM.mapMethod("func_216433_a");
var FIELD_AUDIO_STREAM = ASM.mapField("field_216444_e");
var SOURCE_PLAY_BUFFER = ASM.mapMethod("func_216429_a");

function log(message)
{
    print("[SoundControl Transformer - SoundSource]: " + message);
}

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
                method.instructions.insert(newInstructions);
                log("Hooked SoundSource.playBuffer()");

/*
                method = findMethod(classNode, SOURCE_AUDIO_STREAM);
                var target = ASM.findFirstInstruction(method, Opcodes.ICONST_4);
                method.instructions.set(target, new VarInsnNode(Opcodes.BIPUSH, 32));
                log("Changed from 4 buffers to 8 in SoundSource");
                */

/*
                // Hook for doing mono conversion on streams.  Not that it is needed but leaving in case so I don't
                // have to remember how to do it. :)
                var audioFormat = ASM.buildMethodCall(
                    "org/orecruncher/sndctrl/audio/Conversion",
                    "convert",
                    "(Lnet/minecraft/client/audio/IAudioStream;)Lnet/minecraft/client/audio/IAudioStream;",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(audioFormat);
                newInstructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/audio/SoundSource", FIELD_AUDIO_STREAM, "Lnet/minecraft/client/audio/IAudioStream;"));

                var method = findMethod(classNode, SOURCE_AUDIO_STREAM);
                var insertPoint = ASM.findFirstInstructionAfter(method, Opcodes.PUTFIELD, 0);
                method.instructions.insert(insertPoint, newInstructions);
                log("Hooked SoundSource.attach()");
*/
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
