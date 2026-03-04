package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.RoleDebugDisplay;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DebugSupport {
   protected final NPCEntity parent;
   @Nullable
   protected RoleDebugDisplay debugDisplay;
   protected boolean debugRoleSteering;
   protected boolean debugMotionSteering;
   protected EnumSet<RoleDebugFlags> debugFlags;
   @Nullable
   protected String displayCustomString;
   @Nullable
   protected String displayPathfinderString;
   protected boolean traceSuccess;
   protected boolean traceFail;
   protected boolean traceSensorFails;
   protected Sensor lastFailingSensor;
   protected List<DebugSupport.DebugFlagsChangeListener> debugFlagsChangeListeners = new ArrayList<>();
   protected boolean visSensorRanges;
   protected int currentSensorColorIndex;
   @Nullable
   protected List<DebugSupport.SensorVisData> sensorVisDataList;
   @Nullable
   protected Map<Ref<EntityStore>, List<DebugSupport.EntityVisData>> entityVisDataMap;

   public DebugSupport(NPCEntity parent, @Nonnull BuilderRole builder) {
      this.parent = parent;
      this.debugFlags = builder.getDebugFlags();
   }

   @Nullable
   public RoleDebugDisplay getDebugDisplay() {
      return this.debugDisplay;
   }

   public boolean isTraceSuccess() {
      return this.traceSuccess;
   }

   public boolean isTraceFail() {
      return this.traceFail;
   }

   public boolean isTraceSensorFails() {
      return this.traceSensorFails;
   }

   public void setLastFailingSensor(Sensor sensor) {
      this.lastFailingSensor = sensor;
   }

   public Sensor getLastFailingSensor() {
      return this.lastFailingSensor;
   }

   public boolean isDebugRoleSteering() {
      return this.debugRoleSteering;
   }

   public boolean isDebugMotionSteering() {
      return this.debugMotionSteering;
   }

   public void setDisplayCustomString(String displayCustomString) {
      this.displayCustomString = displayCustomString;
   }

   @Nullable
   public String pollDisplayCustomString() {
      String ret = this.displayCustomString;
      this.displayCustomString = null;
      return ret;
   }

   public void setDisplayPathfinderString(String displayPathfinderString) {
      this.displayPathfinderString = displayPathfinderString;
   }

   @Nullable
   public String pollDisplayPathfinderString() {
      String ret = this.displayPathfinderString;
      this.displayPathfinderString = null;
      return ret;
   }

   public EnumSet<RoleDebugFlags> getDebugFlags() {
      return this.debugFlags;
   }

   public void setDebugFlags(EnumSet<RoleDebugFlags> debugFlags) {
      this.debugFlags = debugFlags;
      this.onDebugFlagsChanged();
      this.notifyDebugFlagsListeners(debugFlags);
   }

   public boolean isDebugFlagSet(RoleDebugFlags flag) {
      return this.debugFlags.contains(flag);
   }

   public boolean isAnyDebugFlagSet(@Nonnull EnumSet<RoleDebugFlags> flags) {
      for (RoleDebugFlags d : flags) {
         if (this.debugFlags.contains(d)) {
            return true;
         }
      }

      return false;
   }

   protected void onDebugFlagsChanged() {
      this.debugRoleSteering = this.isDebugFlagSet(RoleDebugFlags.SteeringRole);
      this.debugMotionSteering = this.isDebugFlagSet(RoleDebugFlags.MotionControllerSteer);
      this.traceFail = this.isDebugFlagSet(RoleDebugFlags.TraceFail);
      this.traceSuccess = this.isDebugFlagSet(RoleDebugFlags.TraceSuccess);
      this.traceSensorFails = this.isDebugFlagSet(RoleDebugFlags.TraceSensorFailures);
      this.visSensorRanges = this.isDebugFlagSet(RoleDebugFlags.VisSensorRanges);
      this.debugDisplay = RoleDebugDisplay.create(this.debugFlags, this.debugDisplay);
   }

   public void registerDebugFlagsListener(DebugSupport.DebugFlagsChangeListener listener) {
      this.debugFlagsChangeListeners.add(listener);
   }

   public void removeDebugFlagsListener(DebugSupport.DebugFlagsChangeListener listener) {
      this.debugFlagsChangeListeners.remove(listener);
   }

   public void notifyDebugFlagsListeners(EnumSet<RoleDebugFlags> flags) {
      for (DebugSupport.DebugFlagsChangeListener listener : this.debugFlagsChangeListeners) {
         listener.onDebugFlagsChanged(flags);
      }
   }

   public boolean isVisSensorRanges() {
      return this.visSensorRanges;
   }

   public void beginSensorVisualization() {
      this.currentSensorColorIndex = 0;
      if (this.sensorVisDataList != null) {
         this.sensorVisDataList.clear();
      }

      if (this.entityVisDataMap != null) {
         for (List<DebugSupport.EntityVisData> list : this.entityVisDataMap.values()) {
            list.clear();
         }
      }
   }

   public int recordSensorRange(double range, double minRange, double viewAngle) {
      if (this.sensorVisDataList == null) {
         this.sensorVisDataList = new ArrayList<>();
      }

      int colorIndex = this.currentSensorColorIndex++;
      this.sensorVisDataList.add(new DebugSupport.SensorVisData(range, minRange, colorIndex, viewAngle));
      return colorIndex;
   }

   public void recordEntityCheck(@Nonnull Ref<EntityStore> entityRef, int sensorColorIndex, boolean matched) {
      if (this.entityVisDataMap == null) {
         this.entityVisDataMap = new HashMap<>();
      }

      this.entityVisDataMap.computeIfAbsent(entityRef, k -> new ArrayList<>()).add(new DebugSupport.EntityVisData(sensorColorIndex, matched));
   }

   @Nullable
   public List<DebugSupport.SensorVisData> getSensorVisData() {
      return this.sensorVisDataList;
   }

   @Nullable
   public Map<Ref<EntityStore>, List<DebugSupport.EntityVisData>> getEntityVisData() {
      return this.entityVisDataMap;
   }

   public boolean hasSensorVisData() {
      return this.sensorVisDataList != null && !this.sensorVisDataList.isEmpty();
   }

   public void clearSensorVisData() {
      if (this.sensorVisDataList != null) {
         this.sensorVisDataList.clear();
      }
   }

   public interface DebugFlagsChangeListener {
      void onDebugFlagsChanged(EnumSet<RoleDebugFlags> var1);
   }

   public record EntityVisData(int sensorColorIndex, boolean matched) {
   }

   public record SensorVisData(double range, double minRange, int colorIndex, double viewAngle) {
   }
}
