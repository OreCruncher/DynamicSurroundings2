var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

var ARROW_TICK = ASM.mapMethod("func_70071_h_");
var ARROW_CRITICAL = ASM.mapMethod("func_70241_g");

var FORMAT = "[arrow.js] {}";

function initializeCoreMod()
{
    return {
        "mobeffects_abstractarrowentity_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.entity.projectile.AbstractArrowEntity"]; }
            },
            "transformer": function(classNode) {

                var call = ASM.buildMethodCall(
                    "org/orecruncher/mobeffects/misc/ArrowEntityHandler",
                    "showCritical",
                    "(Lnet/minecraft/entity/projectile/AbstractArrowEntity;)Z",
                    ASM.MethodType.STATIC
                );

                var newInstructions = new InsnList();
                newInstructions.add(call);

                var targetMethod = findMethod(classNode, ARROW_TICK);
                if (targetMethod !== null) {
                    ASM.insertInsnList(targetMethod, ASM.MethodType.VIRTUAL, "net/minecraft/entity/projectile/AbstractArrowEntity", ARROW_CRITICAL, "()Z", newInstructions, ASM.InsertMode.REMOVE_ORIGINAL);
                    ASM.log("INFO", FORMAT, ["Hooked AbstractArrowEntity.tick()"]);
                } else {
                    ASM/log("WARN", FORMAT, "Will not be able to disable arrow particle trail");
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
