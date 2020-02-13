var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var INVALIDATE_AND_SET_BLOCK = ASM.mapMethod("func_195597_b");
var SET_BLOCKSTATE = ASM.mapMethod("func_180501_a");

var FORMAT = "[clientworld.js] {}";

function initializeCoreMod()
{
    return {
        "sndctrl_clientworld_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.world.ClientWorld"]; }
            },
            "transformer": function(classNode) {

                var callback = ASM.buildMethodCall(
                    "org/orecruncher/lib/world/ClientBlockUpdateHandler",
                    "blockUpdateCallback",
                    "(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                newInstructions.add(callback);

                var targetMethod = findMethod(classNode, INVALIDATE_AND_SET_BLOCK);
                if (targetMethod !== null) {
                    targetMethod.instructions.insert(newInstructions);
                    ASM.log("INFO", FORMAT, ["Hooked ClientWorld.invalidateRegionAndSetBlock()"]);
                } else {
                    ASM.log("WARN", FORMAT, ["Client side block update notifications will not be processed!"]);
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