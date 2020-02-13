var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');

var SOUND_SYSTEM_INITIALIZE = ASM.mapMethod("func_216404_a");

var FORMAT = "[soundsystem.js] {}";

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

                if (targetMethod !== null) {
                    var i;
                    for (i = 0; i < targetMethod.instructions.size(); i++) {
                        var node = targetMethod.instructions.get(i);
                        if (node.getOpcode() == Opcodes.BIPUSH) {
                            if (node.operand == 8) {
                                ASM.log("INFO", FORMAT, ["Found BIPUSH 8 instruction"]);
                                targetMethod.instructions.set(node, new IntInsnNode(Opcodes.BIPUSH, 10));
                            }
                        }
                    }

                    ASM.log("INFO", FORMAT, ["Increased streaming sound source count to 10"]);
                } else {
                    ASM.log("WARN", FORMAT, ["Sound engine will be limited to 8 streaming sounds, max"]);
                }
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
    ASM.log("WARN", "Method not found: {}", [methodName]);
    return null;
}
