package net.guojing.instrument;


import net.guojing.instrument.modules.ClassTransformer;

import java.lang.instrument.Instrumentation;

public class Main extends Thread {
	public static void premain(String args, Instrumentation inst) throws Throwable {
		ClassTransformer.pool.importPackage("net.guojing.instrument.modules");

		inst.addTransformer(new ClassTransformer());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					ClassTransformer.saveAll();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
}