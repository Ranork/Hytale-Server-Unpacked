package com.hypixel.hytale.metrics;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.Thread.State;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class JVMMetrics {
   @Nonnull
   public static final MetricsRegistry<ClassLoader> CLASS_LOADER_METRICS_REGISTRY = new MetricsRegistry<>();
   @Nonnull
   public static final MetricsRegistry<MemoryUsage> MEMORY_USAGE_METRICS_REGISTRY = new MetricsRegistry<>();
   @Nonnull
   public static final MetricsRegistry<GarbageCollectorMXBean> GARBAGE_COLLECTOR_METRICS_REGISTRY = new MetricsRegistry<>();
   @Nonnull
   public static final MetricsRegistry<MemoryPoolMXBean> MEMORY_POOL_METRICS_REGISTRY = new MetricsRegistry<>();
   @Nonnull
   public static final MetricsRegistry<Void> METRICS_REGISTRY = new MetricsRegistry<>();

   static {
      CLASS_LOADER_METRICS_REGISTRY.register("Name", ClassLoader::getName, Codec.STRING);
      CLASS_LOADER_METRICS_REGISTRY.register("Parent", ClassLoader::getParent, CLASS_LOADER_METRICS_REGISTRY);
      MEMORY_USAGE_METRICS_REGISTRY.register("Init", MemoryUsage::getInit, Codec.LONG);
      MEMORY_USAGE_METRICS_REGISTRY.register("Used", MemoryUsage::getUsed, Codec.LONG);
      MEMORY_USAGE_METRICS_REGISTRY.register("Committed", MemoryUsage::getCommitted, Codec.LONG);
      MEMORY_USAGE_METRICS_REGISTRY.register("Max", MemoryUsage::getMax, Codec.LONG);
      GARBAGE_COLLECTOR_METRICS_REGISTRY.register("Name", MemoryManagerMXBean::getName, Codec.STRING);
      GARBAGE_COLLECTOR_METRICS_REGISTRY.register("MemoryPoolNames", MemoryManagerMXBean::getMemoryPoolNames, Codec.STRING_ARRAY);
      GARBAGE_COLLECTOR_METRICS_REGISTRY.register("CollectionCount", GarbageCollectorMXBean::getCollectionCount, Codec.LONG);
      GARBAGE_COLLECTOR_METRICS_REGISTRY.register("CollectionTime", GarbageCollectorMXBean::getCollectionTime, Codec.LONG);
      MEMORY_POOL_METRICS_REGISTRY.register("Name", MemoryPoolMXBean::getName, Codec.STRING);
      MEMORY_POOL_METRICS_REGISTRY.register("Type", MemoryPoolMXBean::getType, new EnumCodec<>(MemoryType.class));
      MEMORY_POOL_METRICS_REGISTRY.register("PeakUsage", MemoryPoolMXBean::getPeakUsage, MEMORY_USAGE_METRICS_REGISTRY);
      MEMORY_POOL_METRICS_REGISTRY.register("Usage", MemoryPoolMXBean::getUsage, MEMORY_USAGE_METRICS_REGISTRY);
      MEMORY_POOL_METRICS_REGISTRY.register("CollectionUsage", MemoryPoolMXBean::getCollectionUsage, MEMORY_USAGE_METRICS_REGISTRY);
      MetricsRegistry<MemoryPoolMXBean> usageThreshold = new MetricsRegistry<>();
      usageThreshold.register("Threshold", MemoryPoolMXBean::getUsageThreshold, Codec.LONG);
      usageThreshold.register("ThresholdCount", MemoryPoolMXBean::getUsageThresholdCount, Codec.LONG);
      usageThreshold.register("ThresholdExceeded", MemoryPoolMXBean::isUsageThresholdExceeded, Codec.BOOLEAN);
      MEMORY_POOL_METRICS_REGISTRY.register(
         "UsageThreshold", memoryPoolMXBean -> !memoryPoolMXBean.isUsageThresholdSupported() ? null : memoryPoolMXBean, usageThreshold
      );
      usageThreshold = new MetricsRegistry<>();
      usageThreshold.register("Threshold", MemoryPoolMXBean::getCollectionUsageThreshold, Codec.LONG);
      usageThreshold.register("ThresholdCount", MemoryPoolMXBean::getCollectionUsageThresholdCount, Codec.LONG);
      usageThreshold.register("ThresholdExceeded", MemoryPoolMXBean::isCollectionUsageThresholdExceeded, Codec.BOOLEAN);
      MEMORY_POOL_METRICS_REGISTRY.register(
         "CollectionUsageThreshold", memoryPoolMXBean -> !memoryPoolMXBean.isCollectionUsageThresholdSupported() ? null : memoryPoolMXBean, usageThreshold
      );
      usageThreshold = new MetricsRegistry<>();
      METRICS_REGISTRY.register("PROCESSOR", unused -> System.getenv("PROCESSOR_IDENTIFIER"), Codec.STRING);
      METRICS_REGISTRY.register("PROCESSOR_ARCHITECTURE", unused -> System.getenv("PROCESSOR_ARCHITECTURE"), Codec.STRING);
      METRICS_REGISTRY.register("PROCESSOR_ARCHITEW6432", unused -> System.getenv("PROCESSOR_ARCHITEW6432"), Codec.STRING);
      usageThreshold.register("OSName", OperatingSystemMXBean::getName, Codec.STRING);
      usageThreshold.register("OSArch", OperatingSystemMXBean::getArch, Codec.STRING);
      usageThreshold.register("OSVersion", OperatingSystemMXBean::getVersion, Codec.STRING);
      usageThreshold.register("AvailableProcessors", unused -> Runtime.getRuntime().availableProcessors(), Codec.INTEGER);
      usageThreshold.register("SystemLoadAverage", OperatingSystemMXBean::getSystemLoadAverage, Codec.DOUBLE);
      if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean) {
         usageThreshold.register(
            "CpuLoad", operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getCpuLoad(), Codec.DOUBLE
         );
         usageThreshold.register(
            "ProcessCpuLoad", operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getProcessCpuLoad(), Codec.DOUBLE
         );
         usageThreshold.register(
            "TotalMemorySize", operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getTotalMemorySize(), Codec.LONG
         );
         usageThreshold.register(
            "FreeMemorySize", operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getFreeMemorySize(), Codec.LONG
         );
         usageThreshold.register(
            "TotalSwapSpaceSize",
            operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getTotalSwapSpaceSize(),
            Codec.LONG
         );
         usageThreshold.register(
            "FreeSwapSpaceSize", operatingSystemMXBean -> ((com.sun.management.OperatingSystemMXBean)operatingSystemMXBean).getFreeSwapSpaceSize(), Codec.LONG
         );
      }

      METRICS_REGISTRY.register("System", aVoid -> ManagementFactory.getOperatingSystemMXBean(), usageThreshold);
      usageThreshold = new MetricsRegistry<>();
      usageThreshold.register("StartTime", runtimeMXBean -> Instant.ofEpochMilli(runtimeMXBean.getStartTime()), Codec.INSTANT);
      usageThreshold.register("Uptime", runtimeMXBean -> Duration.ofMillis(runtimeMXBean.getUptime()), Codec.DURATION);
      usageThreshold.register("RuntimeName", RuntimeMXBean::getName, Codec.STRING);
      usageThreshold.register("SpecName", RuntimeMXBean::getSpecName, Codec.STRING);
      usageThreshold.register("SpecVendor", RuntimeMXBean::getSpecVendor, Codec.STRING);
      usageThreshold.register("SpecVersion", RuntimeMXBean::getSpecVersion, Codec.STRING);
      usageThreshold.register("ManagementSpecVersion", RuntimeMXBean::getManagementSpecVersion, Codec.STRING);
      usageThreshold.register("VMName", RuntimeMXBean::getVmName, Codec.STRING);
      usageThreshold.register("VMVendor", RuntimeMXBean::getVmVendor, Codec.STRING);
      usageThreshold.register("VMVersion", RuntimeMXBean::getVmVersion, Codec.STRING);
      usageThreshold.register("LibraryPath", RuntimeMXBean::getLibraryPath, Codec.STRING);

      try {
         ManagementFactory.getRuntimeMXBean().getBootClassPath();
         usageThreshold.register("BootClassPath", RuntimeMXBean::getBootClassPath, Codec.STRING);
      } catch (UnsupportedOperationException var2) {
      }

      usageThreshold.register("ClassPath", RuntimeMXBean::getClassPath, Codec.STRING);
      usageThreshold.register("InputArguments", runtimeMXBean -> runtimeMXBean.getInputArguments().toArray(String[]::new), Codec.STRING_ARRAY);
      usageThreshold.register("SystemProperties", RuntimeMXBean::getSystemProperties, new MapCodec<>(Codec.STRING, HashMap::new));
      METRICS_REGISTRY.register("Runtime", aVoid -> ManagementFactory.getRuntimeMXBean(), usageThreshold);
      usageThreshold = new MetricsRegistry<>();
      usageThreshold.register("ObjectPendingFinalizationCount", memoryMXBean -> memoryMXBean.getObjectPendingFinalizationCount(), Codec.INTEGER);
      usageThreshold.register("HeapMemoryUsage", memoryMXBean -> memoryMXBean.getHeapMemoryUsage(), MEMORY_USAGE_METRICS_REGISTRY);
      usageThreshold.register("NonHeapMemoryUsage", memoryMXBean -> memoryMXBean.getNonHeapMemoryUsage(), MEMORY_USAGE_METRICS_REGISTRY);
      METRICS_REGISTRY.register("Memory", aVoid -> ManagementFactory.getMemoryMXBean(), usageThreshold);
      METRICS_REGISTRY.register(
         "GarbageCollectors",
         memoryMXBean -> ManagementFactory.getGarbageCollectorMXBeans().toArray(GarbageCollectorMXBean[]::new),
         new ArrayCodec<>(GARBAGE_COLLECTOR_METRICS_REGISTRY, GarbageCollectorMXBean[]::new)
      );
      METRICS_REGISTRY.register(
         "MemoryPools",
         memoryMXBean -> ManagementFactory.getMemoryPoolMXBeans().toArray(MemoryPoolMXBean[]::new),
         new ArrayCodec<>(MEMORY_POOL_METRICS_REGISTRY, MemoryPoolMXBean[]::new)
      );
      METRICS_REGISTRY.register("Threads", aVoid -> {
         ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
         ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
         Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
         Long2ObjectOpenHashMap<Thread> threadIdMap = new Long2ObjectOpenHashMap();

         for (Thread thread : stackTraces.keySet()) {
            threadIdMap.put(thread.getId(), thread);
         }

         JVMMetrics.ThreadMetricData[] data = new JVMMetrics.ThreadMetricData[threadInfos.length];

         for (int i = 0; i < threadInfos.length; i++) {
            ThreadInfo threadInfo = threadInfos[i];
            data[i] = new JVMMetrics.ThreadMetricData(threadInfo, (Thread)threadIdMap.get(threadInfo.getThreadId()), threadMXBean);
         }

         return data;
      }, new ArrayCodec<>(JVMMetrics.ThreadMetricData.METRICS_REGISTRY, JVMMetrics.ThreadMetricData[]::new));
      METRICS_REGISTRY.register("SecurityManager", aVoid -> {
         SecurityManager securityManager = System.getSecurityManager();
         return securityManager == null ? null : securityManager.getClass().getName();
      }, Codec.STRING);
      usageThreshold = new MetricsRegistry<>();
      usageThreshold.register("LoadedClassCount", ClassLoadingMXBean::getLoadedClassCount, Codec.INTEGER);
      usageThreshold.register("UnloadedClassCount", ClassLoadingMXBean::getUnloadedClassCount, Codec.LONG);
      usageThreshold.register("TotalLoadedClassCount", ClassLoadingMXBean::getTotalLoadedClassCount, Codec.LONG);
      usageThreshold.register("SystemClassloader", unused -> ClassLoader.getSystemClassLoader(), CLASS_LOADER_METRICS_REGISTRY);
      usageThreshold.register("JVMMetricsClassloader", unused -> JVMMetrics.class.getClassLoader(), CLASS_LOADER_METRICS_REGISTRY);
      METRICS_REGISTRY.register("ClassLoading", aVoid -> ManagementFactory.getClassLoadingMXBean(), usageThreshold);
   }

   private static class ThreadMetricData {
      @Nonnull
      public static final MetricsRegistry<StackTraceElement> STACK_TRACE_ELEMENT_METRICS_REGISTRY = new MetricsRegistry<>();
      @Nonnull
      public static final MetricsRegistry<JVMMetrics.ThreadMetricData> METRICS_REGISTRY = new MetricsRegistry<>();
      private final ThreadInfo threadInfo;
      private final Thread thread;
      private final ThreadMXBean threadMXBean;

      public ThreadMetricData(ThreadInfo threadInfo, Thread thread, ThreadMXBean threadMXBean) {
         this.threadInfo = threadInfo;
         this.thread = thread;
         this.threadMXBean = threadMXBean;
      }

      static {
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("FileName", StackTraceElement::getFileName, Codec.STRING);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("LineNumber", StackTraceElement::getLineNumber, Codec.INTEGER);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("ModuleName", StackTraceElement::getModuleName, Codec.STRING);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("ModuleVersion", StackTraceElement::getModuleVersion, Codec.STRING);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("ClassLoaderName", StackTraceElement::getClassLoaderName, Codec.STRING);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("ClassName", StackTraceElement::getClassName, Codec.STRING);
         STACK_TRACE_ELEMENT_METRICS_REGISTRY.register("MethodName", StackTraceElement::getMethodName, Codec.STRING);
         METRICS_REGISTRY.register("Id", threadMetricData -> threadMetricData.threadInfo.getThreadId(), Codec.LONG);
         METRICS_REGISTRY.register("Name", threadMetricData -> threadMetricData.threadInfo.getThreadName(), Codec.STRING);
         METRICS_REGISTRY.register("State", threadMetricData -> threadMetricData.threadInfo.getThreadState(), new EnumCodec<>(State.class));
         METRICS_REGISTRY.register("Priority", threadMetricData -> threadMetricData.threadInfo.getPriority(), Codec.INTEGER);
         METRICS_REGISTRY.register("Daemon", threadMetricData -> threadMetricData.threadInfo.isDaemon(), Codec.BOOLEAN);
         METRICS_REGISTRY.register(
            "CPUTime", threadMetricData -> threadMetricData.threadMXBean.getThreadCpuTime(threadMetricData.threadInfo.getThreadId()), Codec.LONG
         );
         METRICS_REGISTRY.register("WaitedTime", threadMetricData -> threadMetricData.threadInfo.getWaitedTime(), Codec.LONG);
         METRICS_REGISTRY.register("WaitedCount", threadMetricData -> threadMetricData.threadInfo.getWaitedCount(), Codec.LONG);
         METRICS_REGISTRY.register("BlockedTime", threadMetricData -> threadMetricData.threadInfo.getBlockedTime(), Codec.LONG);
         METRICS_REGISTRY.register("BlockedCount", threadMetricData -> threadMetricData.threadInfo.getBlockedCount(), Codec.LONG);
         METRICS_REGISTRY.register("LockName", threadMetricData -> threadMetricData.threadInfo.getLockName(), Codec.STRING);
         METRICS_REGISTRY.register("LockOwnerId", threadMetricData -> threadMetricData.threadInfo.getLockOwnerId(), Codec.LONG);
         METRICS_REGISTRY.register("LockOwnerName", threadMetricData -> threadMetricData.threadInfo.getLockOwnerName(), Codec.STRING);
         METRICS_REGISTRY.register(
            "StackTrace",
            threadMetricData -> threadMetricData.threadInfo.getStackTrace(),
            new ArrayCodec<>(STACK_TRACE_ELEMENT_METRICS_REGISTRY, StackTraceElement[]::new)
         );
         METRICS_REGISTRY.register(
            "InitStackTrace",
            threadMetricData -> threadMetricData.thread instanceof InitStackThread ? ((InitStackThread)threadMetricData.thread).getInitStack() : null,
            new ArrayCodec<>(STACK_TRACE_ELEMENT_METRICS_REGISTRY, StackTraceElement[]::new)
         );
         METRICS_REGISTRY.register(
            "Interrupted", threadMetricData -> threadMetricData.thread != null ? threadMetricData.thread.isInterrupted() : null, Codec.BOOLEAN
         );
         METRICS_REGISTRY.register(
            "ThreadClass", threadMetricData -> threadMetricData.thread != null ? threadMetricData.thread.getClass().getName() : null, Codec.STRING
         );
         MetricsRegistry<ThreadGroup> threadGroup = new MetricsRegistry<>();
         threadGroup.register("Name", ThreadGroup::getName, Codec.STRING);
         threadGroup.register("Parent", ThreadGroup::getParent, threadGroup);
         threadGroup.register("MaxPriority", ThreadGroup::getMaxPriority, Codec.INTEGER);
         threadGroup.register("Destroyed", ThreadGroup::isDestroyed, Codec.BOOLEAN);
         threadGroup.register("Daemon", ThreadGroup::isDaemon, Codec.BOOLEAN);
         threadGroup.register("ActiveCount", ThreadGroup::activeCount, Codec.INTEGER);
         threadGroup.register("ActiveGroupCount", ThreadGroup::activeGroupCount, Codec.INTEGER);
         METRICS_REGISTRY.register(
            "ThreadGroup", threadMetricData -> threadMetricData.thread != null ? threadMetricData.thread.getThreadGroup() : null, threadGroup
         );
         METRICS_REGISTRY.register(
            "UncaughtExceptionHandler",
            threadMetricData -> threadMetricData.thread != null ? threadMetricData.thread.getUncaughtExceptionHandler().getClass().getName() : null,
            Codec.STRING
         );
      }
   }
}
