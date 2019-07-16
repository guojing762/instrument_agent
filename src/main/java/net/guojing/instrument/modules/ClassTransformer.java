package net.guojing.instrument.modules;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.attribute.FileTime;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class ClassTransformer extends Thread implements ClassFileTransformer {
	private static ArrayList<MyFile> cls = new ArrayList<MyFile>();
	public static ClassPool pool = ClassPool.getDefault();

	public static byte[] dump(CtClass cl) throws Throwable {
		byte[] b = null;

		b = cl.toBytecode();
		cl.stopPruning(true);

		cls.add(new MyFile(cl.getName().replaceAll("\\.", "/") + ".class", b));

		return b;
	}

	public static void saveAll() throws Throwable {
		cls.add(new MyFile(ClassNameGen.class.getName().replaceAll("\\.", "/") + ".class",
				pool.get(ClassNameGen.class.getName()).toBytecode()));
		
		File f = new File("cracked.jar");
		if (f.exists())
			f.delete();
		f.createNewFile();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
		/* Start of combination of jar's */
		ZipFile zf = new ZipFile("D:/project/learn/class/allatori.jar");
		Enumeration<? extends ZipEntry> in = zf.entries();

		byte[] data;

		while (in.hasMoreElements()) {
			ZipEntry ze = in.nextElement();
			boolean finded = false;

			for (MyFile mc : cls)
				if (mc != null && ze != null && mc.name != null && ze.getName() != null
						&& mc.name.equals(ze.getName())) {
					finded = true;
					break;
				}

			if (zf != null && ze != null && !finded) {
				DataInputStream dis = new DataInputStream(zf.getInputStream(ze));
				data = new byte[(int) ze.getSize()];
				dis.readFully(data);
				dis.close();

				ze = modifyEntry(new ZipEntry(ze.getName()));
				
				out.putNextEntry(ze);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
		}
		zf.close();
		/* End of combination of jar's */
		for (MyFile mc : cls) {
			data = mc.bytecode;
			ZipEntry ze = modifyEntry(new ZipEntry(mc.name));
			
			out.putNextEntry(ze);
			out.write(data, 0, data.length);
			out.closeEntry();
		}

		out.setComment("P4ck3d by m00fM0nk3y'5 4ll470r1 4u70-cr4ck (1337 15 u53d)");
		out.setLevel(9);
		out.close();
	}
	
	public static ZipEntry modifyEntry(ZipEntry ze) {
		final long time = 0;
		
		ze.setTime(time);
		ze = ze.setCreationTime(FileTime.fromMillis(time)).setLastAccessTime(FileTime.fromMillis(time)).setLastModifiedTime(FileTime.fromMillis(time));
		
		return ze;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		String name = className.replaceAll("/", ".");
		CtClass cl = null;

		if (!name.startsWith("com.allatori."))
			return classfileBuffer;

		final String toSearch = "THIS_IS_DEMO_VERSION_NOT_FOR_COMMERCIAL_USE";
		String signature = "(Ljava/lang/String;)Ljava/lang/String;";

		try {
			if (!Utils.isContains(classfileBuffer, toSearch)) {
				cls.add(new MyFile(className + ".class", classfileBuffer));

				return classfileBuffer;
			}

			cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

			// Anti-comment of .jar file
			// To search for find: setComment
			// FIXME: HAND-MADE PATCH!!!
			if (cl.getName().equals("com.allatori.IiIIiiiIii"))
				for (CtMethod md : cl.getDeclaredMethods())
					if (md.getReturnType() == CtClass.voidType && md.getParameterTypes().length == 1
							&& md.getParameterTypes()[0].getName().equalsIgnoreCase("java.util.jar.JarOutputStream"))
						md.setBody("{ }");
			/* End of patch */

			// Replace main logo in Allatori
			// To search for find: go to com.allatori.Obfuscate and find firstly
			// System.out.orintln(). See where it
			// FIXME: HAND-MADE PATCH!!!
			if (cl.getName().equals("com.allatori.Obfuscate")) {
				final String patchLogoN = ClassNameGen.get(6);
                String code = "{ String s = \"\";"
                        + "s += \"################################################\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"#        ## #   #    ## ### ### ##  ###        #\\n\"; "
                        + "s += \"#       # # #   #   # #  #  # # # #  #         #\\n\"; "
                        + "s += \"#       ### #   #   ###  #  # # ##   #         #\\n\"; "
                        + "s += \"#       # # ### ### # #  #  ### # # ###        #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"#                DEMO VERSION!                 #\\n\"; "
                        + "s += \"#           NOT FOR COMMERCIAL USE!            #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"#       Demo version adds System.out's         #\\n\"; "
                        + "s += \"#       and gives 'ALLATORI_DEMO' name         #\\n\"; "
                        + "s += \"#       to some fields and methods.            #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"# Obfuscation by Allatori Obfuscator v7.0 DEMO #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"#           http://www.allatori.com            #\\n\"; "
                        + "s += \"#                                              #\\n\"; "
                        + "s += \"################################################\\n\"; "+ "return s; }";


				// Add default allatori logo
				CtMethod md = CtMethod.make("public static String " + patchLogoN + "() " + code, cl);

				cl.addMethod(md);
				
				cl.getDeclaredMethod("main").instrument(new ExprEditor() {
					@Override
					public void edit(MethodCall mdc) {
						try {
							CtMethod md = mdc.getMethod();
							
							if (md.getName().equals(toSearch) && mdc.getSignature().equals("()Ljava/lang/String;"))
								mdc.replace("{ $_ = " + patchLogoN + "(); }");
						
							super.edit(mdc);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				});
			}
			/* End of patch */
			ArrayList<CtMethod> mdl = Utils.get(cl.getDeclaredMethods(), toSearch, signature);

			if (mdl == null) {
				cls.add(new MyFile(className + ".class", classfileBuffer = cl.toBytecode()));

				return classfileBuffer;
			}

			String arg0 = "iiIIiiIIiI";
			String retOnFail = arg0;
			for (CtMethod md : mdl)
				if (Utils.dump)
					md.insertAfter("{ if(" + arg0 + " != null) if(" + arg0 + " instanceof String) { "
							+ "System.out.println(\"\\n" + cl.getName() + ":\\n" + "\" +  " + arg0 + "); }",
							false);
				else {
					// Replace logo
					md.insertAfter("{ if(" + arg0 + " != null) if(" + arg0 + " instanceof String) { "
							+ "if(" + arg0 + ".indexOf(\"http://www.allatori.com\") > -1) {" + (Utils.debug
							? "System.out.println(\"Replaced allatori logo in " + cl.getName() + "\");" : "")
							+ " return \"\"; }"
							+ "} else { return " + retOnFail + "; } }", false);
					// Replace prefixes
					md.insertAfter("{ if(" + arg0 + " != null) if(" + arg0 + " instanceof String) { "
									+ "if(" + arg0 +  ".indexOf(\"ALLATORIxDEMO\") > -1) {"
									+ (Utils.debug ? " System.out.println(\"Replaced allatori prefix in " + cl.getName()
											+ "\");" : "")
									+ " return " + arg0 + ".replaceAll(\"ALLATORIxDEMOx\", ClassNameGen.get(6))"
									+ "   .replaceAll(\"ALLATORIxDEMO\", ClassNameGen.get(6)); }"
									+ "} else { return " + retOnFail + "; } }",
							false);
				}

			classfileBuffer = dump(cl);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (cl != null) {
				cl.defrost();
				cl.detach();
			}
		}

		return classfileBuffer;
	}
}
