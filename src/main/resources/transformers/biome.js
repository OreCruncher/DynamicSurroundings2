var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');

var FORMAT = "[biome.js] {}";

function initializeCoreMod()
{
    return {
        "environs_biome_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.world.biome.Biome"]; }
            },
            "transformer": function(classNode) {
                // Add a field to cache our data
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "environs_biomeData", "Ljava/lang/Object;", null, null));
                ASM.log("INFO", FORMAT, ["Added biome data cache field"]);
                return classNode;
            }
        }
    };
}