package com.hbm.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static com.hbm.core.HbmCorePlugin.coreLogger;
import static com.hbm.core.HbmCorePlugin.fail;
import static org.objectweb.asm.Opcodes.*;

public class ForgeHooksTransformer implements IClassTransformer {
    private static final ObfSafeName isSpectator = new ObfSafeName("isSpectator", "func_175149_v");

    private static AbstractInsnNode findAnchor(MethodNode method) {
        for (AbstractInsnNode n : method.instructions.toArray()) {
            if (n.getOpcode() == GETSTATIC) {
                FieldInsnNode f = (FieldInsnNode) n;
                if ("net/minecraftforge/common/ForgeModContainer".equals(f.owner) && "fullBoundingBoxLadders".equals(f.name) && "Z".equals(f.desc)) {
                    return n;
                }
            }
        }
        return null;
    }

    private static AbstractInsnNode firstRealInsn(InsnList list) {
        AbstractInsnNode cur = list.getFirst();
        while (cur instanceof LabelNode || cur instanceof LineNumberNode || cur instanceof FrameNode) {
            cur = cur.getNext();
        }
        return cur == null ? list.getFirst() : cur;
    }

    private static void injectHook(MethodNode method, AbstractInsnNode anchor, boolean headFallback) {
        // 0: IBlockState state
        // 1: World       world
        // 2: BlockPos    pos
        // 3: EntityLivingBase entity

        LabelNode L_returnTrue = new LabelNode();
        LabelNode L_returnFalse = new LabelNode();
        LabelNode L_continue = new LabelNode();

        InsnList patch = new InsnList();

        // Fallback path for LittleTiles because they literally nuked the method
        // so we have to inject our own spectator guard
        if (headFallback) {
            LabelNode L_notSpectator = new LabelNode();
            patch.add(new VarInsnNode(ALOAD, 3));
            patch.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/entity/player/EntityPlayer"));
            patch.add(new JumpInsnNode(IFEQ, L_notSpectator));

            patch.add(new VarInsnNode(ALOAD, 3));
            patch.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/player/EntityPlayer"));
            patch.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", isSpectator.getName(), "()Z", false));
            patch.add(new JumpInsnNode(IFEQ, L_notSpectator));
            patch.add(new InsnNode(ICONST_0));
            patch.add(new InsnNode(IRETURN));
            patch.add(L_notSpectator);
        }

        // Call LadderHook.onCheckLadder(state, world, pos, entity)
        patch.add(new VarInsnNode(ALOAD, 0));
        patch.add(new VarInsnNode(ALOAD, 1));
        patch.add(new VarInsnNode(ALOAD, 2));
        patch.add(new VarInsnNode(ALOAD, 3));
        patch.add(new MethodInsnNode(INVOKESTATIC, "com/hbm/core/LadderHook", "onCheckLadder",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
                "Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraftforge/fml/common/eventhandler/Event$Result;", false));

        // Duplicate result and compare against Event.Result.ALLOW
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/eventhandler/Event$Result", "ALLOW",
                "Lnet/minecraftforge/fml/common/eventhandler/Event$Result;"));
        patch.add(new JumpInsnNode(IF_ACMPEQ, L_returnTrue));

        // Duplicate again and compare against Event.Result.DENY
        patch.add(new InsnNode(DUP));
        patch.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/eventhandler/Event$Result", "DENY",
                "Lnet/minecraftforge/fml/common/eventhandler/Event$Result;"));
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
        if (!headFallback) coreLogger.info("Injected CheckLadderEvent hook before fullBoundingBoxLadders read");
        else coreLogger.warn("A mod nuked isLivingOnLadder! Injected spectator check and CheckLadderEvent hook on isLivingOnLadder HEAD");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !"net.minecraftforge.common.ForgeHooks".equals(transformedName)) {
            return basicClass;
        }
        coreLogger.info("Patching class {} / {}", transformedName, name);

        try {
            ClassNode cn = new ClassNode();
            new ClassReader(basicClass).accept(cn, 0);

            boolean patched = false;
            for (MethodNode mn : cn.methods) {
                if ("isLivingOnLadder".equals(mn.name) &&
                    ("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
                     "Lnet/minecraft/entity/EntityLivingBase;)Z").equals(mn.desc)) {
                    coreLogger.info("Patching method: {}{}", "isLivingOnLadder",
                            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
                            "Lnet/minecraft/entity/EntityLivingBase;)Z");

                    AbstractInsnNode anchor = findAnchor(mn);
                    boolean headFallback = false;
                    if (anchor == null) {
                        anchor = firstRealInsn(mn.instructions);
                        headFallback = true;
                    }
                    injectHook(mn, anchor, headFallback);
                    patched = true;
                    break;
                }
            }

            if (!patched) {
                coreLogger.warn("Failed to find {}{}", "isLivingOnLadder",
                        "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
                        "Lnet/minecraft/entity/EntityLivingBase;)Z");
                return basicClass;
            }

            ClassWriter cw = HbmCorePlugin.getBrand() == HbmCorePlugin.Brand.CAT_SERVER ? new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
                private static String mapForLoad(String internalName) {
                    String dotted = internalName.replace('/', '.');
                    String unmapped = FMLDeobfuscatingRemapper.INSTANCE.unmap(dotted);
                    return unmapped.replace('.', '/');
                }

                @Override
                protected String getCommonSuperClass(String t1, String t2) {
                    try {
                        ClassLoader cl = Launch.classLoader;
                        String n1 = mapForLoad(t1);
                        String n2 = mapForLoad(t2);

                        Class<?> c1 = Class.forName(n1.replace('/', '.'), false, cl);
                        Class<?> c2 = Class.forName(n2.replace('/', '.'), false, cl);

                        if (c1.isAssignableFrom(c2)) return t1;
                        if (c2.isAssignableFrom(c1)) return t2;
                        if (c1.isInterface() || c2.isInterface()) return "java/lang/Object";

                        do {
                            c1 = c1.getSuperclass();
                        } while (!c1.isAssignableFrom(c2));
                        return c1.getName().replace('.', '/');
                    } catch (Throwable ex) {
                        throw new RuntimeException("Failed to find common superclass", ex);
                    }
                }
            } : new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            fail("net.minecraftforge.common.ForgeHooks", t);
            return basicClass;
        }
    }
}
