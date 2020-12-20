var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var HIT_GROUND_HANDLER = ASM.mapMethod("func_217577_h");

var FORMAT = "[dripparticle.js] {}";

function initializeCoreMod()
{
    return {
        "environs_dripparticle_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.particle.DripParticle$Falling"]; }
            },
            "transformer": function(classNode) {

                var callback = ASM.buildMethodCall(
                    "org/orecruncher/environs/effects/particles/ParticleHooks",
                    "dripHandler",
                    "(Lnet/minecraft/client/particle/DripParticle;)V",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(callback);

                var targetMethod = findMethod(classNode, HIT_GROUND_HANDLER);
                if (targetMethod !== null) {
                    targetMethod.instructions.insert(newInstructions);
                    ASM.log("INFO", FORMAT, ["Hooked DripParticle$Falling.groundHitHandler()"]);
                } else {
                    ASM.log("WARN", FORMAT, ["Will not be able to generate special effects for water drip impacts"]);
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
    ASM.log("WARN", "Method not found: {}",  [methodName]);
    return null;
}