package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionEntry {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final int index;
   @Nonnull
   private final DynamicMetaStore<Interaction> metaStore;
   private long timestamp;
   private long simulationTimestamp;
   private final InteractionSyncData serverState = new InteractionSyncData();
   private InteractionSyncData simulationState;
   @Nullable
   private InteractionSyncData clientState;
   private long waitingForSyncData;
   private long waitingForServerFinished;
   private long waitingForClientFinished;
   private boolean useSimulationState;
   private boolean desynced;
   private boolean shouldSendInitial = true;

   public InteractionEntry(int index, int counter, int rootInteraction) {
      this.index = index;
      this.metaStore = new DynamicMetaStore<>(null, Interaction.META_REGISTRY);
      this.serverState.operationCounter = counter;
      this.serverState.rootInteraction = rootInteraction;
      this.serverState.state = InteractionState.NotFinished;
   }

   public int getIndex() {
      return this.index;
   }

   public int nextForkId() {
      return this.serverState.totalForks++;
   }

   public int getNextForkId() {
      return this.serverState.totalForks;
   }

   @Nonnull
   public InteractionSyncData getState() {
      return this.useSimulationState ? this.getSimulationState() : this.getServerState();
   }

   public void setUseSimulationState(boolean useSimulationState) {
      this.useSimulationState = useSimulationState;
   }

   public float getTimeInSeconds(long tickTime) {
      long timestamp = this.getTimestamp();
      if (timestamp == 0L) {
         return 0.0F;
      } else {
         long diff = tickTime - timestamp;
         return (float)diff / 1.0E9F;
      }
   }

   public void setTimestamp(long timestamp, float shift) {
      timestamp -= (long)(shift * 1.0E9F);
      if (this.useSimulationState) {
         this.simulationTimestamp = timestamp;
      } else {
         this.timestamp = timestamp;
      }
   }

   public long getTimestamp() {
      return this.useSimulationState ? this.simulationTimestamp : this.timestamp;
   }

   public boolean isUseSimulationState() {
      return this.useSimulationState;
   }

   @Nullable
   public InteractionSyncData getClientState() {
      return this.clientState;
   }

   @Nonnull
   public DynamicMetaStore<Interaction> getMetaStore() {
      return this.metaStore;
   }

   public int getServerDataHashCode() {
      InteractionSyncData serverData = this.getState();
      float progress = serverData.progress;
      serverData.progress = (int)progress;
      int hashCode = serverData.hashCode();
      serverData.progress = progress;
      return hashCode;
   }

   @Nonnull
   public InteractionSyncData getServerState() {
      return this.serverState;
   }

   @Nonnull
   public InteractionSyncData getSimulationState() {
      if (this.simulationState == null) {
         this.simulationState = new InteractionSyncData();
         this.simulationState.operationCounter = this.serverState.operationCounter;
         this.simulationState.rootInteraction = this.serverState.rootInteraction;
         this.simulationState.state = InteractionState.NotFinished;
      }

      return this.simulationState;
   }

   public boolean setClientState(@Nullable InteractionSyncData clientState) {
      if (clientState != null
         && (clientState.operationCounter != this.serverState.operationCounter || clientState.rootInteraction != this.serverState.rootInteraction)) {
         HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
         if (ctx.isEnabled()) {
            RootInteraction root = RootInteraction.getAssetMap().getAsset(this.serverState.rootInteraction);
            Operation op = root.getOperation(this.serverState.operationCounter);
            String info;
            if (op.getInnerOperation() instanceof Interaction interaction) {
               info = interaction.getId() + " (" + interaction.getClass().getSimpleName() + ")";
            } else {
               info = op + " (" + op.getClass().getSimpleName() + ")";
            }

            ctx.log(
               "%d: Client/Server desync %d != %d, %d != %d (for %s)",
               this.index,
               this.serverState.operationCounter,
               clientState.operationCounter,
               this.serverState.rootInteraction,
               clientState.rootInteraction,
               info
            );
         }

         return false;
      } else {
         this.clientState = clientState;
         return true;
      }
   }

   public long getWaitingForSyncData() {
      return this.waitingForSyncData;
   }

   public void setWaitingForSyncData(long waitingForSyncData) {
      this.waitingForSyncData = waitingForSyncData;
   }

   public long getWaitingForServerFinished() {
      return this.waitingForServerFinished;
   }

   public void setWaitingForServerFinished(long waitingForServerFinished) {
      this.waitingForServerFinished = waitingForServerFinished;
   }

   public long getWaitingForClientFinished() {
      return this.waitingForClientFinished;
   }

   public void setWaitingForClientFinished(long waitingForClientFinished) {
      this.waitingForClientFinished = waitingForClientFinished;
   }

   public boolean consumeDesyncFlag() {
      boolean flag = this.desynced;
      this.desynced = false;
      return flag;
   }

   public void flagDesync() {
      this.desynced = true;
   }

   public boolean consumeSendInitial() {
      boolean flag = this.shouldSendInitial;
      this.shouldSendInitial = false;
      return flag;
   }

   @Nonnull
   @Override
   public String toString() {
      return "InteractionEntry{index="
         + this.index
         + ", metaStore="
         + this.metaStore
         + ", timestamp="
         + this.timestamp
         + ", getTimeInSeconds()="
         + this.getTimeInSeconds(System.nanoTime())
         + ", simulationTimestamp="
         + this.simulationTimestamp
         + ", serverState="
         + this.serverState
         + ", simulationState="
         + this.simulationState
         + ", clientState="
         + this.clientState
         + ", waitingForSyncData="
         + this.waitingForSyncData
         + ", waitingForServerFinished="
         + this.waitingForServerFinished
         + ", waitingForClientFinished="
         + this.waitingForClientFinished
         + ", useSimulationState="
         + this.useSimulationState
         + ", desynced="
         + this.desynced
         + "}";
   }
}
