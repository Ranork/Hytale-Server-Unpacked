package com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class TimeInstrument {
   private int sampleCount;
   @Nonnull
   private TimeInstrument.Probe totalProbe;
   private String header;

   public TimeInstrument(@Nonnull String header) {
      this.header = header;
      this.sampleCount = 0;
      this.totalProbe = null;
   }

   public void takeSample(@Nonnull TimeInstrument.Probe probe) {
      if (this.totalProbe == null) {
         this.totalProbe = probe;
         this.sampleCount++;
      } else if (!probe.isCompatibleForAddition(this.totalProbe)) {
         LoggerUtil.getLogger().warning("Discarded a probe because of mismatched structures.");

         assert false;
      } else {
         this.sampleCount++;
         this.totalProbe.add(probe);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      StringBuilder s = new StringBuilder();
      s.append("Performance Report\n");
      s.append("Sample Count: ").append(this.sampleCount).append("\n");
      s.append(this.header).append("\n");
      s.append(this.toString(0, this.totalProbe));
      return s.toString();
   }

   @Nonnull
   private String toString(int indentation, @Nonnull TimeInstrument.Probe probe) {
      long ns = probe.getTotalTime() / this.sampleCount;
      StringBuilder s = new StringBuilder();
      s.append("\t".repeat(indentation));
      s.append(probe.getName()).append(": ");
      s.append(LoggerUtil.nsToMsDecimal(ns)).append(" ms");
      s.append("\n");
      List<TimeInstrument.Probe> childProbes = probe.getProbes();

      for (int i = 0; i < childProbes.size(); i++) {
         s.append(this.toString(indentation + 1, childProbes.get(i)));
      }

      return s.toString();
   }

   public static class Probe {
      @Nonnull
      private final String name;
      private long startTime;
      private long totalTime;
      private TimeInstrument.Probe.State state;
      private List<TimeInstrument.Probe> probes;

      public Probe(@Nonnull String name) {
         this.name = name;
         this.state = TimeInstrument.Probe.State.NOT_STARTED;
         this.probes = new ArrayList<>(1);
      }

      @Nonnull
      public TimeInstrument.Probe start() {
         this.startTime = System.nanoTime();
         this.state = TimeInstrument.Probe.State.STARTED;
         return this;
      }

      @Nonnull
      public TimeInstrument.Probe stop() {
         assert this.state == TimeInstrument.Probe.State.STARTED;

         this.state = TimeInstrument.Probe.State.COMPLETED;
         this.totalTime = System.nanoTime() - this.startTime;
         return this;
      }

      public long getTotalTime() {
         assert this.state == TimeInstrument.Probe.State.COMPLETED;

         return this.totalTime;
      }

      @Nonnull
      public String getName() {
         return this.name;
      }

      @Nonnull
      public List<TimeInstrument.Probe> getProbes() {
         return this.probes;
      }

      @Nonnull
      public TimeInstrument.Probe createProbe(@Nonnull String name) {
         TimeInstrument.Probe probe = new TimeInstrument.Probe(name);
         this.probes.add(probe);
         return probe;
      }

      public boolean isCompatibleForAddition(@Nonnull TimeInstrument.Probe other) {
         if (this.probes.size() == other.probes.size() && this.name.equals(other.name)) {
            for (int i = 0; i < this.probes.size(); i++) {
               if (!this.probes.get(i).isCompatibleForAddition(other.probes.get(i))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      public void add(@Nonnull TimeInstrument.Probe probe) {
         assert this.state == TimeInstrument.Probe.State.COMPLETED;

         assert this.isCompatibleForAddition(probe);

         this.totalTime = this.totalTime + probe.getTotalTime();

         for (int i = 0; i < this.probes.size(); i++) {
            this.probes.get(i).add(probe.probes.get(i));
         }
      }

      private static enum State {
         NOT_STARTED,
         STARTED,
         COMPLETED;
      }
   }
}
