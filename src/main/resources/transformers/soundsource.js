var ASM = Java.type("net.minecraftforge.coremod.api.ASMAPI");

var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');

function initializeCoreMod()
{
    return {
        "temperature_transformer": {
            "target": {
                "type": "CLASS",
                "names": function(listofclasses) { return ["net.minecraft.client.audio.SoundSource"]; }
            },
            "transformer": function(classNode) {
                // Add a field to cache our data
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "sndctrl_context", "Lorg/orecruncher/sndctrl/audio/handlers/SourceContext;", null, null));
                print("[SoundControl Transformer]: Patched SoundSource - Added context field");
                return classNode;
            }
        }
    };
}
