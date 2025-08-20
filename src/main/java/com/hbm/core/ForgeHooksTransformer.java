package com.hbm.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static org.objectweb.asm.Opcodes.*;

public class ForgeHooksTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "net.minecraftforge.common.ForgeHooks";
    private static final String TARGET_METHOD = "isLivingOnLadder";
    private static final String TARGET_DESC =
            "(Lnet/minecraft/block/state/IBlockState;" + "Lnet/minecraft/world/World;" + "Lnet/minecraft/util/math/BlockPos;" +
            "Lnet/minecraft/entity/EntityLivingBase;)Z";

    private static final String FMOD_OWNER = "net/minecraftforge/common/ForgeModContainer";
    private static final String FMOD_FIELD = "fullBoundingBoxLadders";
    private static final String FMOD_DESC = "Z";
    private static final String HOOK_OWNER = "com/hbm/core/LadderHook";
    private static final String HOOK_NAME = "onCheckLadder";
    private static final String HOOK_DESC =
            "(Lnet/minecraft/block/state/IBlockState;" + "Lnet/minecraft/world/World;" + "Lnet/minecraft/util/math/BlockPos;" +
            "Lnet/minecraft/entity/EntityLivingBase;)" + "Lnet/minecraftforge/fml/common/eventhandler/Event$Result;";
    private static final String RESULT_OWNER = "net/minecraftforge/fml/common/eventhandler/Event$Result";
    private static final String RESULT_DESC = "L" + RESULT_OWNER + ";";
    private static final String RESULT_ALLOW = "ALLOW";
    private static final String RESULT_DENY = "DENY";

    private static void injectHook(MethodNode method) {
        AbstractInsnNode anchor = null;
        for (AbstractInsnNode n : method.instructions.toArray()) {
            if (n.getOpcode() == GETSTATIC) {
                FieldInsnNode f = (FieldInsnNode) n;
                if (FMOD_OWNER.equals(f.owner) && FMOD_FIELD.equals(f.name) && FMOD_DESC.equals(f.desc)) {
                    anchor = n;
                    break;
                }
            }
        }
        if (anchor == null) {
            throw new RuntimeException("Anchor GETSTATIC ForgeModContainer.fullBoundingBoxLadders not found in isLivingOnLadder");
        }

        // Local variables (static method params):
        // 0: IBlockState state
        // 1: World       world
        // 2: BlockPos    pos
        // 3: EntityLivingBase entity

        LabelNode L_returnTrue = new LabelNode();
        LabelNode L_returnFalse = new LabelNode();
        LabelNode L_continue = new LabelNode();

        InsnList patch = new InsnList();

        // Call LadderHook.onCheckLadder(state, world, pos, entity)
        patch.add(new VarInsnNode(ALOAD, 0));
        patch.add(new VarInsnNode(ALOAD, 1));
        patch.add(new VarInsnNode(ALOAD, 2));
        patch.add(new VarInsnNode(ALOAD, 3));
        patch.add(new MethodInsnNode(INVOKESTATIC, HOOK_OWNER, HOOK_NAME, HOOK_DESC, false));

        // Duplicate result and compare against Event.Result.ALLOW
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, RESULT_OWNER, RESULT_ALLOW, RESULT_DESC));
        patch.add(new JumpInsnNode(IF_ACMPEQ, L_returnTrue));

        // Duplicate again and compare against Event.Result.DENY
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, RESULT_OWNER, RESULT_DENY, RESULT_DESC));
        patch.add(new JumpInsnNode(IF_ACMPEQ, L_returnFalse));

        // DEFAULT -> pop the result and continue
        patch.add(new InsnNode(POP));
        patch.add(new JumpInsnNode(GOTO, L_continue));

        // ALLOW -> pop result, return true
        patch.add(L_returnTrue);
        patch.add(new InsnNode(POP));
        patch.add(new InsnNode(ICONST_1));
        patch.add(new InsnNode(IRETURN));

        // DENY -> pop result, return false
        patch.add(L_returnFalse);
        patch.add(new InsnNode(POP));
        patch.add(new InsnNode(ICONST_0));
        patch.add(new InsnNode(IRETURN));
        patch.add(L_continue);

        method.instructions.insertBefore(anchor, patch);
        coreLogger.info("Injected CheckLadderEvent hook before fullBoundingBoxLadders read");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, 0);

            boolean patched = false;
            for (MethodNode mn : cn.methods) {
                if (TARGET_METHOD.equals(mn.name) && TARGET_DESC.equals(mn.desc)) {
                    coreLogger.info("Patching method: {}{}", TARGET_METHOD, TARGET_DESC);
                    injectHook(mn);
                    patched = true;
                    break;
                }
            }

            if (!patched) {
                coreLogger.warn("Failed to find {}{}", TARGET_METHOD, TARGET_DESC);
                return basicClass;
            }

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            coreLogger.fatal("Error transforming ForgeHooks", t);
            return basicClass;
        }
    }
}
