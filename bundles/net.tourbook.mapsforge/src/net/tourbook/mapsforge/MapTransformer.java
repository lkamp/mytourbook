package net.tourbook.mapsforge;

//import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
//import static org.objectweb.asm.Opcodes.ALOAD;
//import static org.objectweb.asm.Opcodes.IFEQ;
//import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
//import static org.objectweb.asm.Opcodes.RETURN;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Prevent that java.awt.Container is painting the background color which causes max flickering :-(
 */
public class MapTransformer implements ClassFileTransformer {

	private static final String JAVA_AWT_CONTAINER = "java/awt/Container";

	public static void premain(final String agentArgs, final Instrumentation inst) {

		inst.addTransformer(new MapTransformer());
	}

	private MethodVisitor createMethodVisitor(final ClassWriter cw) {

		return new MethodVisitor(Opcodes.ASM5) {

//			@Override
//			public void visitMethodInsn(final int opcode,
//										final String owner,
//										final String name,
//										final String desc,
//										final boolean itf) {
//
////				if (/*
////					 * opcode == Opcodes.INVOKEVIRTUAL &&
////					 */ owner.equals("java/awt/Component")
////						&& name.equals("update")
////						&& desc.equals("()Ljava/lang/String;")) {
////
//////					public void update(Graphics g) {
//////			    		paint(g);
//////					}
////
////				{
////					mv = cw.visitMethod(ACC_PUBLIC, "update", "(Ljava/awt/Graphics;)V", null, null);
////					mv.visitCode();
////					final Label l0 = new Label();
////					mv.visitLabel(l0);
////					mv.visitLineNumber(1999, l0);
////					mv.visitVarInsn(ALOAD, 0);
////					mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/Container", "isShowing", "()Z", false);
////					final Label l1 = new Label();
////					mv.visitJumpInsn(IFEQ, l1);
////					final Label l2 = new Label();
////					mv.visitLabel(l2);
////					mv.visitLineNumber(2000, l2);
////					mv.visitVarInsn(ALOAD, 0);
////					mv.visitVarInsn(ALOAD, 1);
////					mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/Container", "paint", "(Ljava/awt/Graphics;)V", false);
////					mv.visitLabel(l1);
////					mv.visitLineNumber(2002, l1);
////					mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
////					mv.visitInsn(RETURN);
////					final Label l3 = new Label();
////					mv.visitLabel(l3);
////					mv.visitMaxs(2, 2);
////					mv.visitEnd();
////				}
////
////				} else {
////
//				super.visitMethodInsn(opcode, owner, name, desc, itf);
////				}
//			}
		};
	}

	@Override
	public byte[] transform(final ClassLoader loader,
							final String className,
							final Class<?> classBeingRedefined,
							final ProtectionDomain protectionDomain,
							final byte[] classfileBuffer)
			throws IllegalClassFormatException {

//		System.out.println("MapTransformer invoked on " + className);
//
//		return classfileBuffer;

//		if (!className.contains(JAVA_AWT_CONTAINER)) {
//			return classfileBuffer;
//		}

		System.out.println("MapTransformer invoked on " + className);

		final ClassReader cr = new ClassReader(classfileBuffer);
		final ClassWriter cw = new ClassWriter(cr, 0);

		final ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {


			@Override
			public final MethodVisitor visitMethod(	final int access,
													final String name,
													final String desc,
													final String signature,
													final String[] exceptions) {

				return new MethodWriter(this, access, name, desc, signature, exceptions, computeMaxs, computeFrames);
			}

//			@Override
//			public MethodVisitor visitMethod(	final int access,
//												final String name,
//												final String desc,
//												final String signature,
//												final String[] exceptions) {
//				// TODO Auto-generated method stub
//				return new MethodVisitor(Opcodes.ASM5) {};
//			}

//			@Override
//			public MethodVisitor visitMethod(	final int access,
//												final String name,
//												final String desc,
//												final String signature,
//												final String[] exceptions) {
//
//				return createMethodVisitor(cw);
//			}
		};

		cr.accept(cv, 0);

		return cw.toByteArray();
	}

}
