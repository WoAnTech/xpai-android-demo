package cn.com.xpai;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
	private static ExecutorService executorService;

	public static ExecutorService getInstance() {
		if (executorService == null) {
			synchronized (Object.class) {
				if (executorService == null) {
					executorService = Executors.newFixedThreadPool(100);
					
				}
			}
		}
		return executorService;
	}
}
