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

	private static final String	CLASS_NAME_JAVA_AWT_CONTAINER			= "java/awt/Container";
	private static final String	METHOD_DESCRIPTOR_LJAVA_AWT_GRAPHICS_V	= "(Ljava/awt/Graphics;)V";
	private static final String	METHOD_NAME_UPDATE						= "update";

	public class ClassVisitor_MapTransformer extends ClassVisitor {

		public ClassVisitor_MapTransformer(final ClassVisitor cv) {
			super(Opcodes.ASM5, cv);
		}

		@Override
		public MethodVisitor visitMethod(	final int access,
											final String name,
											final String desc,
											final String signature,
											final String[] exceptions) {

//			 [ClassVisitor_MapTransformer] 	1         	paint         	(Ljava/awt/Graphics;)V    	null   	null
//			 [ClassVisitor_MapTransformer] 	1         	print         	(Ljava/awt/Graphics;)V    	null   	null
//			 [ClassVisitor_MapTransformer] 	1         	update        	(Ljava/awt/Graphics;)V    	null   	null

//			System.out.println(
//					(/* UI.timeStampNano() + */ " [" + getClass().getSimpleName() + "] ")
//							+ (String.format("\t%-10d", access))
//							+ (String.format("\t%-50s", name == null ? "null" : name))
//							+ (String.format("\t%-50s", desc == null ? "null" : desc))
//							+ (String.format("\t%-50s", signature == null ? "null" : signature))
//							+ (String.format("\t%s", exceptions == null ? "null" : exceptions))
//			//
//			);
//			// TODO remove SYSTEM.OUT.PRINTLN

//		    public void update(Graphics g) {
//		    	if (isShowing()) {
//		    		paint(g);
//		    	}
//		    }

			if (METHOD_NAME_UPDATE.equals(name) && METHOD_DESCRIPTOR_LJAVA_AWT_GRAPHICS_V.equals(desc)) {

				System.out.println(
						"MapTransformer - writing method '"
								+ name
								+ "', returning null :-), the other code did not work :-(");

//				final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "update", "(Ljava/awt/Graphics;)V", null, null);
//				mv.visitCode();
//
//				final Label l0 = new Label();
//				mv.visitLabel(l0);
////				mv.visitLineNumber(1999, l0);
//
//				mv.visitVarInsn(ALOAD, 0);
//				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/Container", "isShowing", "()Z", false);
//
//				final Label l1 = new Label();
//				mv.visitJumpInsn(IFEQ, l1);
//
//				final Label l2 = new Label();
//				mv.visitLabel(l2);
////				mv.visitLineNumber(2000, l2);
//
//				mv.visitVarInsn(ALOAD, 0);
//				mv.visitVarInsn(ALOAD, 1);
//				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/Container", "paint", "(Ljava/awt/Graphics;)V", false);
//				mv.visitLabel(l1);
////				mv.visitLineNumber(2002, l1);
//				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
//				mv.visitInsn(RETURN);
//
//				final Label l3 = new Label();
//				mv.visitLabel(l3);
//				mv.visitMaxs(2, 2);
//				mv.visitEnd();
//
//				return mv;
				return null;
			}

			return super.visitMethod(access, name, desc, signature, exceptions);
//			return null;
		}

	}

	public static void premain(final String agentArgs, final Instrumentation inst) {

		inst.addTransformer(new MapTransformer());
	}

	@Override
	public byte[] transform(final ClassLoader loader,
							final String className,
							final Class<?> classBeingRedefined,
							final ProtectionDomain protectionDomain,
							final byte[] classfileBuffer)

			throws IllegalClassFormatException {

//		System.out.println(
//				(System.currentTimeMillis() + " [" + getClass().getSimpleName() + "] ")
//						+ (String.format("\t%-100s", className == null ? "null" : className))
//						+ (String.format("\t%-200s", loader == null ? "null" : loader))
////
//		);
//// TODO remove SYSTEM.OUT.PRINTLN

		// skip all other classes
		if (CLASS_NAME_JAVA_AWT_CONTAINER.equals(className) == false) {
			return classfileBuffer;
		}

		System.out.println("MapTransformer - invoked on '" + className + "'");

		final ClassReader cr = new ClassReader(classfileBuffer);
		final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		final ClassVisitor cv = new ClassVisitor_MapTransformer(cw);

		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		return cw.toByteArray();
	}

}
