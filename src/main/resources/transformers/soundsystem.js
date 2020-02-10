var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');

var SOUND_SYSTEM_INITIALIZE = ASM.mapMethod("func_216404_a");

function log(message)
{
    print("[SoundControl Transformer - SoundSystem]: " + message);
}

function initializeCoreMod()
{
    return {
        "sndctrl_soundsystem_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.audio.SoundSystem"]; }
            },
            "transformer": function(classNode) {

                var targetMethod = findMethod(classNode, SOUND_SYSTEM_INITIALIZE);

                var i;
                for (i = 0; i < targetMethod.instructions.size(); i++) {
                    var node = targetMethod.instructions.get(i);
                    if (node.getOpcode() == Opcodes.BIPUSH) {
                        if (node.operand == 8) {
                            log("Found BIPUSH 8 instruction")
                            targetMethod.instructions.set(node, new IntInsnNode(Opcodes.BIPUSH, 10));
                        }
                    }
                }

                log("Increased streaming sound source count to 10")
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
