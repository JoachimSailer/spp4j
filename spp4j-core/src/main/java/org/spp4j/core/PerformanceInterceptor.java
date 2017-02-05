package org.spp4j.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spp4j.core.Performance.Level;
import org.spp4j.core.config.Configuration;
import org.spp4j.core.entity.MethodDescriptor;
import org.spp4j.core.entity.PerformanceEntry;
import org.spp4j.core.worker.CleanerWriter;
import org.spp4j.core.worker.MethodWriter;
import org.spp4j.core.worker.SampleWriter;
import org.spp4j.core.worker.StatisticWriter;

@Aspect
public class PerformanceInterceptor {

	private static final ConcurrentLinkedQueue<PerformanceEntry> SAMPLE_QUEUE = new ConcurrentLinkedQueue<PerformanceEntry>();
	private static final BlockingQueue<MethodDescriptor> METHOD_QUEUE = new LinkedBlockingQueue<MethodDescriptor>();
	private static final Map<String, MethodDescriptor> DESCRIPTORS = new HashMap<String, MethodDescriptor>();
	private static final ScheduledExecutorService SCHEDULED_POOL = Executors.newScheduledThreadPool(5);
	private static final Logger LOG = LoggerFactory.getLogger(PerformanceInterceptor.class);

	static {
		final SampleWriter sampleWriter = new SampleWriter(SAMPLE_QUEUE);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				sampleWriter.setShutdown(true);
				Configuration.setProfileLevel(Level.OFF);
				SCHEDULED_POOL.shutdown();
			}
		});
		SCHEDULED_POOL.scheduleAtFixedRate(new MethodWriter(METHOD_QUEUE), 0, 10, TimeUnit.SECONDS);
		SCHEDULED_POOL.scheduleAtFixedRate(sampleWriter, 0, 1, TimeUnit.SECONDS);
		SCHEDULED_POOL.scheduleAtFixedRate(new StatisticWriter(), 0, 10, TimeUnit.MINUTES);
		SCHEDULED_POOL.scheduleAtFixedRate(new CleanerWriter(), 0, 1, TimeUnit.HOURS);
	}

	/**
	 * execution(* *(..)) && @target(org.spf4j.core.Performance)
	 * 
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("execution(@org.spp4j.core.Performance * *(..))")
	public Object collectPerformanceData(ProceedingJoinPoint joinPoint) throws Throwable {
		long interceptorStart = System.currentTimeMillis();
		int actualDuration = 0;
		Object ret = null;
		MethodDescriptor descriptor = getMethodDescriptor(joinPoint);
		Level level = descriptor.getTracingLevel();
		int configLevel = Configuration.getProfileLevel().getValue();
		boolean trace = configLevel >= level.getValue() && configLevel > 0;
		if (trace) {
			long start = System.currentTimeMillis();
			boolean success = true;
			try {
				ret = joinPoint.proceed();
			} catch (Throwable e) {
				success = false;
				throw e;
			} finally {
				long end = System.currentTimeMillis();
				actualDuration = (int) (end - start);
				PerformanceEntry entry = new PerformanceEntry(descriptor, start, actualDuration, success);
				SAMPLE_QUEUE.add(entry);
			}
		} else {
			ret = joinPoint.proceed();
		}
		LOG.debug("Interceptor took {} ms", (System.currentTimeMillis() - interceptorStart - actualDuration));
		return ret;
	}

	private static MethodDescriptor getMethodDescriptor(ProceedingJoinPoint joinPoint) {
		Signature signature = joinPoint.getStaticPart().getSignature();
		String id = signature.toString();
		MethodDescriptor descriptor = DESCRIPTORS.get(id);
		if (descriptor == null) {
			LOG.debug("Creating entry for method {}", id);
			descriptor = MethodDescriptorFactory.createMethodDescriptor(signature);
			DESCRIPTORS.put(id, descriptor);
			METHOD_QUEUE.add(descriptor);
		}
		return descriptor;
	}

}
