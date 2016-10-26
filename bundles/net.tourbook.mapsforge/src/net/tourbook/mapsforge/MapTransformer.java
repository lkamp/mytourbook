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

	private static final String	JAVA_AWT_CONTAINER	= "java/awt/Container";
	private static final String	METHOD_NAME			= "update";

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

//			if (METHOD_NAME.equals(name)&&"(Ljava/awt/Graphics;)V") {
//
//			}

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

			return super.visitMethod(access, name, desc, signature, exceptions);
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

		// skip all other classes
		if (JAVA_AWT_CONTAINER.equals(className) == false) {
			return classfileBuffer;
		}

		System.out.println("MapTransformer invoked on " + className);

		final ClassReader cr = new ClassReader(classfileBuffer);
		final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		final ClassVisitor cv = new ClassVisitor_MapTransformer(cw);

		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		return cw.toByteArray();
	}

}
