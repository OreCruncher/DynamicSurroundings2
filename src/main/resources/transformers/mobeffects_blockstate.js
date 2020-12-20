var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');

var FORMAT = "[blockstate.js] {}";

function initializeCoreMod()
{
    return {
        "mobeffects_blockstate_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.block.BlockState"]; }
            },
            "transformer": function(classNode) {
                // Add a field to cache footprint ability state
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "mobeffects_hasfootprint", "Ljava/lang/Boolean;", null, null));
                ASM.log("INFO", FORMAT, ["Added hasfootprint cache field"]);

                // Add a field to cache footprint ability state
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "mobeffects_acoustic", "Ljava/lang/Object;", null, null));
                ASM.log("INFO", FORMAT, ["Added acoustic cache field"]);

                return classNode;
            }
        }
    };
}