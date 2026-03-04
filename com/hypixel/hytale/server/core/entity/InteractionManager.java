package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.function.TriFunction;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionChainData;
import com.hypixel.hytale.protocol.InteractionCooldown;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.RootInteractionSettings;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.protocol.packets.interaction.CancelInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.inventory.SetActiveSlot;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.modules.interaction.IInteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionTypeUtils;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionManager implements Component<EntityStore> {
   public static final double MAX_REACH_DISTANCE = 8.0;
   public static final float[] DEFAULT_CHARGE_TIMES = new float[]{0.0F};
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final Int2ObjectMap<InteractionChain> chains = new Int2ObjectOpenHashMap();
   @Nonnull
   private final Int2ObjectMap<InteractionChain> unmodifiableChains = Int2ObjectMaps.unmodifiable(this.chains);
   @Nonnull
   private final CooldownHandler cooldownHandler = new CooldownHandler();
   @Nonnull
   private final LivingEntity entity;
   @Nullable
   private final PlayerRef playerRef;
   private boolean hasRemoteClient;
   @Nonnull
   private final IInteractionSimulationHandler interactionSimulationHandler;
   @Nonnull
   private final ObjectList<InteractionSyncData> tempSyncDataList = new ObjectArrayList();
   private int lastServerChainId;
   private int lastClientChainId;
   private long packetQueueTime;
   private final float[] globalTimeShift = new float[InteractionType.VALUES.length];
   private final boolean[] globalTimeShiftDirty = new boolean[InteractionType.VALUES.length];
   private boolean timeShiftsDirty;
   private final ObjectList<SyncInteractionChain> syncPackets = new ObjectArrayList();
   private long currentTime = 1L;
   @Nonnull
   private final ObjectList<InteractionChain> chainStartQueue = new ObjectArrayList();
   @Nonnull
   private final Predicate<InteractionChain> cachedTickChain = this::tickChain;
   @Nullable
   protected CommandBuffer<EntityStore> commandBuffer;

   public InteractionManager(@Nonnull LivingEntity entity, @Nullable PlayerRef playerRef, @Nonnull IInteractionSimulationHandler simulationHandler) {
      this.entity = entity;
      this.playerRef = playerRef;
      this.hasRemoteClient = playerRef != null;
      this.interactionSimulationHandler = simulationHandler;
   }

   @Nonnull
   public Int2ObjectMap<InteractionChain> getChains() {
      return this.unmodifiableChains;
   }

   @Nonnull
   public IInteractionSimulationHandler getInteractionSimulationHandler() {
      return this.interactionSimulationHandler;
   }

   private long getOperationTimeoutThreshold() {
      if (this.playerRef != null) {
         return this.playerRef.getPacketHandler().getOperationTimeoutThreshold();
      } else {
         assert this.commandBuffer != null;

         World world = this.commandBuffer.getExternalData().getWorld();
         return world.getTickStepNanos() / 1000000 * 10;
      }
   }

   private boolean waitingForClient(@Nonnull Ref<EntityStore> ref) {
      assert this.commandBuffer != null;

      Player playerComponent = this.commandBuffer.getComponent(ref, Player.getComponentType());
      return playerComponent != null ? playerComponent.isWaitingForClientReady() : false;
   }

   @Deprecated(forRemoval = true)
   public void setHasRemoteClient(boolean hasRemoteClient) {
      this.hasRemoteClient = hasRemoteClient;
   }

   @Deprecated
   public void copyFrom(@Nonnull InteractionManager interactionManager) {
      this.chains.putAll(interactionManager.chains);
   }

   public void tick(@Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer, float dt) {
      this.currentTime = this.currentTime + commandBuffer.getExternalData().getWorld().getTickStepNanos();
      this.commandBuffer = commandBuffer;
      this.clearAllGlobalTimeShift(dt);
      this.cooldownHandler.tick(dt);
      ObjectListIterator packetQueue = this.chainStartQueue.iterator();

      while (packetQueue.hasNext()) {
         InteractionChain interactionChain = (InteractionChain)packetQueue.next();
         this.executeChain0(ref, interactionChain);
      }

      this.chainStartQueue.clear();
      Deque<SyncInteractionChain> packetQueuex = null;
      if (this.playerRef != null) {
         packetQueuex = ((GamePacketHandler)this.playerRef.getPacketHandler()).getInteractionPacketQueue();
      }

      if (packetQueuex != null && !packetQueuex.isEmpty()) {
         for (boolean first = true; this.tryConsumePacketQueue(ref, packetQueuex) || first; first = false) {
            if (!this.chains.isEmpty()) {
               this.chains.values().removeIf(this.cachedTickChain);
            }

            float cooldownDt = 0.0F;

            for (float shift : this.globalTimeShift) {
               cooldownDt = Math.max(cooldownDt, shift);
            }

            if (cooldownDt > 0.0F) {
               this.cooldownHandler.tick(cooldownDt);
            }
         }

         this.commandBuffer = null;
      } else {
         if (!this.chains.isEmpty()) {
            this.chains.values().removeIf(this.cachedTickChain);
         }

         this.commandBuffer = null;
      }
   }

   private boolean tryConsumePacketQueue(@Nonnull Ref<EntityStore> ref, @Nonnull Deque<SyncInteractionChain> packetQueue) {
      Iterator<SyncInteractionChain> it = packetQueue.iterator();
      boolean finished = false;
      boolean desynced = false;
      int highestChainId = -1;
      boolean changed = false;

      label116:
      while (it.hasNext()) {
         SyncInteractionChain packet = it.next();
         if (packet.desync) {
            HytaleLogger.Api context = LOGGER.at(Level.FINE);
            if (context.isEnabled()) {
               context.log("Client packet flagged as desync");
            }

            desynced = true;
         }

         InteractionChain chain = (InteractionChain)this.chains.get(packet.chainId);
         if (chain != null && packet.forkedId != null) {
            for (ForkedChainId id = packet.forkedId; id != null; id = id.forkedId) {
               InteractionChain subChain = chain.getForkedChain(id);
               if (subChain == null) {
                  InteractionChain.TempChain tempChain = chain.getTempForkedChain(id);
                  if (tempChain != null) {
                     tempChain.setBaseForkedChainId(id);
                     ForkedChainId lastId = id;

                     for (ForkedChainId var17 = id.forkedId; var17 != null; var17 = var17.forkedId) {
                        tempChain = tempChain.getOrCreateTempForkedChain(var17);
                        tempChain.setBaseForkedChainId(var17);
                        lastId = var17;
                     }

                     tempChain.setForkedChainId(packet.forkedId);
                     tempChain.setBaseForkedChainId(lastId);
                     tempChain.setChainData(packet.data);
                     this.sync(ref, tempChain, packet);
                     changed = true;
                     it.remove();
                     this.packetQueueTime = 0L;
                  }
                  continue label116;
               }

               chain = subChain;
            }
         }

         highestChainId = Math.max(highestChainId, packet.chainId);
         boolean isProxy = packet.data != null && !UUIDUtil.isEmptyOrNull(packet.data.proxyId);
         if (chain == null && (!finished || isProxy)) {
            if (this.syncStart(ref, packet)) {
               changed = true;
               it.remove();
               this.packetQueueTime = 0L;
            } else {
               if (!this.waitingForClient(ref)) {
                  long queuedTime;
                  if (this.packetQueueTime == 0L) {
                     this.packetQueueTime = this.currentTime;
                     queuedTime = 0L;
                  } else {
                     queuedTime = this.currentTime - this.packetQueueTime;
                  }

                  HytaleLogger.Api context = LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log("Queued chain %d for %s", packet.chainId, FormatUtil.nanosToString(queuedTime));
                  }

                  if (queuedTime > TimeUnit.MILLISECONDS.toNanos(this.getOperationTimeoutThreshold())) {
                     this.sendCancelPacket(packet.chainId, packet.forkedId);
                     it.remove();
                     context = LOGGER.at(Level.FINE);
                     if (context.isEnabled()) {
                        context.log("Discarding packet due to queuing for too long: %s", packet);
                     }
                  }
               }

               if (!desynced && !isProxy) {
                  finished = true;
               }
            }
         } else if (chain != null) {
            this.sync(ref, chain, packet);
            changed = true;
            it.remove();
            this.packetQueueTime = 0L;
         } else if (desynced) {
            this.sendCancelPacket(packet.chainId, packet.forkedId);
            it.remove();
            HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
            ctx.log("Discarding packet due to desync: %s", packet);
         }
      }

      if (desynced && !packetQueue.isEmpty()) {
         HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
         if (ctx.isEnabled()) {
            ctx.log("Discarding previous packets in queue: (before) %d", packetQueue.size());
         }

         packetQueue.removeIf(v -> {
            boolean shouldRemove = this.getChain(v.chainId, v.forkedId) == null && UUIDUtil.isEmptyOrNull(v.data.proxyId) && v.initial;
            if (shouldRemove) {
               HytaleLogger.Api ctx1 = LOGGER.at(Level.FINE);
               if (ctx1.isEnabled()) {
                  ctx1.log("Discarding: %s", v);
               }

               this.sendCancelPacket(v.chainId, v.forkedId);
            }

            return shouldRemove;
         });
         ctx = LOGGER.at(Level.FINE);
         if (ctx.isEnabled()) {
            ctx.log("Discarded previous packets in queue: (after) %d", packetQueue.size());
         }
      }

      return changed;
   }

   @Nullable
   private InteractionChain getChain(int chainId, @Nullable ForkedChainId forkedChainId) {
      InteractionChain chain = (InteractionChain)this.chains.get(chainId);
      if (chain != null && forkedChainId != null) {
         for (ForkedChainId id = forkedChainId; id != null; id = id.forkedId) {
            InteractionChain subChain = chain.getForkedChain(id);
            if (subChain == null) {
               return null;
            }

            chain = subChain;
         }
      }

      return chain;
   }

   private boolean tickChain(@Nonnull InteractionChain chain) {
      if (chain.wasPreTicked()) {
         chain.setPreTicked(false);
         return false;
      } else {
         if (!this.hasRemoteClient) {
            chain.updateSimulatedState();
         }

         chain.getForkedChains().values().removeIf(this.cachedTickChain);
         Ref<EntityStore> ref = this.entity.getReference();

         assert ref != null;

         if (chain.getServerState() != InteractionState.NotFinished) {
            if (chain.requiresClient() && chain.getClientState() == InteractionState.NotFinished) {
               if (!this.waitingForClient(ref)) {
                  if (chain.getWaitingForClientFinished() == 0L) {
                     chain.setWaitingForClientFinished(this.currentTime);
                  }

                  long waitMillis = TimeUnit.NANOSECONDS.toMillis(this.currentTime - chain.getWaitingForClientFinished());
                  HytaleLogger.Api context = LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log("Server finished chain but client hasn't! %d, %s, %s", chain.getChainId(), chain, waitMillis);
                  }

                  long threshold = this.getOperationTimeoutThreshold();
                  TimeResource timeResource = this.commandBuffer.getResource(TimeResource.getResourceType());
                  if (timeResource.getTimeDilationModifier() == 1.0F && waitMillis > threshold) {
                     this.cancelChains(chain);
                     return chain.getForkedChains().isEmpty();
                  }
               }

               return false;
            } else {
               LOGGER.at(Level.FINE).log("Remove Chain: %d, %s", chain.getChainId(), chain);
               this.handleCancelledChain(ref, chain);
               chain.onCompletion(this.cooldownHandler, this.hasRemoteClient);
               return chain.getForkedChains().isEmpty();
            }
         } else {
            int baseOpIndex = chain.getOperationIndex();

            try {
               this.doTickChain(ref, chain);
            } catch (InteractionManager.ChainCancelledException var9) {
               chain.setServerState(var9.state);
               chain.setClientState(var9.state);
               chain.updateServerState();
               if (!this.hasRemoteClient) {
                  chain.updateSimulatedState();
               }

               if (chain.requiresClient()) {
                  this.sendSyncPacket(chain, baseOpIndex, this.tempSyncDataList);
                  this.sendCancelPacket(chain);
               }
            }

            if (chain.getServerState() != InteractionState.NotFinished) {
               HytaleLogger.Api contextx = LOGGER.at(Level.FINE);
               if (contextx.isEnabled()) {
                  contextx.log("Server finished chain: %d-%s, %s in %fs", chain.getChainId(), chain.getForkedChainId(), chain, chain.getTimeInSeconds());
               }

               if (!chain.requiresClient() || chain.getClientState() != InteractionState.NotFinished) {
                  contextx = LOGGER.at(Level.FINE);
                  if (contextx.isEnabled()) {
                     contextx.log("Remove Chain: %d-%s, %s", chain.getChainId(), chain.getForkedChainId(), chain);
                  }

                  this.handleCancelledChain(ref, chain);
                  chain.onCompletion(this.cooldownHandler, this.hasRemoteClient);
                  return chain.getForkedChains().isEmpty();
               }
            } else if (chain.getClientState() != InteractionState.NotFinished && !this.waitingForClient(ref)) {
               if (chain.getWaitingForServerFinished() == 0L) {
                  chain.setWaitingForServerFinished(this.currentTime);
               }

               long waitMillisx = TimeUnit.NANOSECONDS.toMillis(this.currentTime - chain.getWaitingForServerFinished());
               HytaleLogger.Api contextxx = LOGGER.at(Level.FINE);
               if (contextxx.isEnabled()) {
                  contextxx.log("Client finished chain but server hasn't! %d, %s, %s", chain.getChainId(), chain, waitMillisx);
               }

               long threshold = this.getOperationTimeoutThreshold();
               if (waitMillisx > threshold) {
                  LOGGER.at(Level.FINE).log("Client finished chain earlier than server! %d, %s", chain.getChainId(), chain);
                  this.cancelChains(chain);
               }
            }

            return false;
         }
      }
   }

   private void handleCancelledChain(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain) {
      assert this.commandBuffer != null;

      RootInteraction root = chain.getRootInteraction();
      int maxOperations = root.getOperationMax();
      if (chain.getOperationCounter() < maxOperations) {
         InteractionEntry entry = chain.getInteraction(chain.getOperationIndex());
         if (entry != null) {
            Operation operation = root.getOperation(chain.getOperationCounter());
            if (operation == null) {
               throw new IllegalStateException("Failed to find operation during simulation tick of chain '" + root.getId() + "'");
            } else {
               InteractionContext context = chain.getContext();
               entry.getServerState().state = InteractionState.Failed;
               if (entry.getClientState() != null) {
                  entry.getClientState().state = InteractionState.Failed;
               }

               try {
                  context.initEntry(chain, entry, this.entity);
                  TimeResource timeResource = this.commandBuffer.getResource(TimeResource.getResourceType());
                  operation.handle(ref, false, entry.getTimeInSeconds(this.currentTime) * timeResource.getTimeDilationModifier(), chain.getType(), context);
               } finally {
                  context.deinitEntry(chain, entry, this.entity);
               }

               chain.setOperationCounter(maxOperations);
            }
         }
      }
   }

   private void doTickChain(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain) {
      ObjectList<InteractionSyncData> interactionData = this.tempSyncDataList;
      interactionData.clear();
      RootInteraction root = chain.getRootInteraction();
      int maxOperations = root.getOperationMax();
      int currentOp = chain.getOperationCounter();
      int baseOpIndex = chain.getOperationIndex();
      int callDepth = chain.getCallDepth();
      if (chain.consumeFirstRun()) {
         if (chain.getForkedChainId() == null) {
            chain.setTimeShift(this.getGlobalTimeShift(chain.getType()));
         } else {
            InteractionChain parent = (InteractionChain)this.chains.get(chain.getChainId());
            chain.setFirstRun(parent != null && parent.isFirstRun());
         }
      } else {
         chain.setTimeShift(0.0F);
      }

      if (!chain.getContext().getEntity().isValid()) {
         throw new InteractionManager.ChainCancelledException(chain.getServerState());
      } else {
         while (true) {
            Operation simOp = !this.hasRemoteClient ? root.getOperation(chain.getSimulatedOperationCounter()) : null;
            WaitForDataFrom simWaitFrom = simOp != null ? simOp.getWaitForDataFrom() : null;
            long tickTime = this.currentTime;
            if (!this.hasRemoteClient && simWaitFrom != WaitForDataFrom.Server) {
               this.simulationTick(ref, chain, tickTime);
            }

            interactionData.add(this.serverTick(ref, chain, tickTime));
            if (!chain.getContext().getEntity().isValid()
               && chain.getServerState() != InteractionState.Finished
               && chain.getServerState() != InteractionState.Failed) {
               throw new InteractionManager.ChainCancelledException(chain.getServerState());
            }

            if (!this.hasRemoteClient && simWaitFrom == WaitForDataFrom.Server) {
               this.simulationTick(ref, chain, tickTime);
            }

            if (!this.hasRemoteClient) {
               if (chain.getRootInteraction() != chain.getSimulatedRootInteraction()) {
                  throw new IllegalStateException(
                     "Simulation and server tick are not in sync (root interaction).\n"
                        + chain.getRootInteraction().getId()
                        + " vs "
                        + chain.getSimulatedRootInteraction()
                  );
               }

               if (chain.getOperationCounter() != chain.getSimulatedOperationCounter()) {
                  throw new IllegalStateException(
                     "Simulation and server tick are not in sync (operation position).\nRoot: "
                        + chain.getRootInteraction().getId()
                        + "\nCounter: "
                        + chain.getOperationCounter()
                        + " vs "
                        + chain.getSimulatedOperationCounter()
                        + "\nIndex: "
                        + chain.getOperationIndex()
                  );
               }
            }

            if (callDepth != chain.getCallDepth()) {
               callDepth = chain.getCallDepth();
               root = chain.getRootInteraction();
               maxOperations = root.getOperationMax();
            } else if (currentOp == chain.getOperationCounter()) {
               break;
            }

            chain.nextOperationIndex();
            currentOp = chain.getOperationCounter();
            if (currentOp >= maxOperations) {
               while (callDepth > 0) {
                  chain.popRoot();
                  callDepth = chain.getCallDepth();
                  currentOp = chain.getOperationCounter();
                  root = chain.getRootInteraction();
                  maxOperations = root.getOperationMax();
                  if (currentOp < maxOperations || callDepth == 0) {
                     break;
                  }
               }

               if (callDepth == 0 && currentOp >= maxOperations) {
                  break;
               }
            }
         }

         chain.updateServerState();
         if (!this.hasRemoteClient) {
            chain.updateSimulatedState();
         }

         if (chain.requiresClient()) {
            this.sendSyncPacket(chain, baseOpIndex, interactionData);
         }
      }
   }

   @Nullable
   private InteractionSyncData serverTick(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain, long tickTime) {
      assert this.commandBuffer != null;

      RootInteraction root = chain.getRootInteraction();
      Operation operation = root.getOperation(chain.getOperationCounter());

      assert operation != null;

      InteractionEntry entry = chain.getOrCreateInteractionEntry(chain.getOperationIndex());
      InteractionSyncData returnData = null;
      boolean wasWrong = entry.consumeDesyncFlag();
      if (entry.getClientState() == null) {
         wasWrong |= !entry.setClientState(chain.removeInteractionSyncData(chain.getOperationIndex()));
      }

      if (wasWrong) {
         returnData = entry.getServerState();
         chain.flagDesync();
         chain.clearInteractionSyncData(chain.getOperationIndex());
      }

      TimeResource timeResource = this.commandBuffer.getResource(TimeResource.getResourceType());
      float tickTimeDilation = timeResource.getTimeDilationModifier();
      if (operation.getWaitForDataFrom() != WaitForDataFrom.Client || entry.getClientState() != null) {
         int serverDataHashCode = entry.getServerDataHashCode();
         InteractionContext context = chain.getContext();
         float time = entry.getTimeInSeconds(tickTime);
         boolean firstRun = false;
         if (entry.getTimestamp() == 0L) {
            time = chain.getTimeShift();
            entry.setTimestamp(tickTime, time);
            firstRun = true;
         }

         time *= tickTimeDilation;

         try {
            context.initEntry(chain, entry, this.entity);
            operation.tick(ref, this.entity, firstRun, time, chain.getType(), context, this.cooldownHandler);
         } finally {
            context.deinitEntry(chain, entry, this.entity);
         }

         InteractionSyncData serverData = entry.getServerState();
         if (firstRun || serverDataHashCode != entry.getServerDataHashCode()) {
            returnData = serverData;
         }

         try {
            context.initEntry(chain, entry, this.entity);
            operation.handle(ref, firstRun, time, chain.getType(), context);
         } finally {
            context.deinitEntry(chain, entry, this.entity);
         }

         this.removeInteractionIfFinished(ref, chain, entry);
         return returnData;
      } else if (this.waitingForClient(ref)) {
         return null;
      } else {
         if (entry.getWaitingForSyncData() == 0L) {
            entry.setWaitingForSyncData(this.currentTime);
         }

         long waitMillis = TimeUnit.NANOSECONDS.toMillis(this.currentTime - entry.getWaitingForSyncData());
         HytaleLogger.Api contextx = LOGGER.at(Level.FINE);
         if (contextx.isEnabled()) {
            contextx.log("Wait for interaction clientData: %d, %s, %s", chain.getOperationIndex(), entry, waitMillis);
         }

         long threshold = this.getOperationTimeoutThreshold();
         if (tickTimeDilation == 1.0F && waitMillis > threshold) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Client failed to send client data, ending early to prevent desync.");
            chain.setServerState(InteractionState.Failed);
            chain.setClientState(InteractionState.Failed);
            this.cancelChains(chain);
            return null;
         } else {
            if (entry.consumeSendInitial() || wasWrong) {
               returnData = entry.getServerState();
            }

            return returnData;
         }
      }
   }

   private void removeInteractionIfFinished(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain, @Nonnull InteractionEntry entry) {
      if (chain.getOperationIndex() == entry.getIndex() && entry.getServerState().state != InteractionState.NotFinished) {
         chain.setFinalState(entry.getServerState().state);
      }

      if (entry.getServerState().state != InteractionState.NotFinished) {
         LOGGER.at(Level.FINE).log("Server finished interaction: %d, %s", entry.getIndex(), entry);
         if (!chain.requiresClient() || entry.getClientState() != null && entry.getClientState().state != InteractionState.NotFinished) {
            LOGGER.at(Level.FINER).log("Remove Interaction: %d, %s", entry.getIndex(), entry);
            chain.removeInteractionEntry(this, entry.getIndex());
         }
      } else if (entry.getClientState() != null && entry.getClientState().state != InteractionState.NotFinished && !this.waitingForClient(ref)) {
         if (entry.getWaitingForServerFinished() == 0L) {
            entry.setWaitingForServerFinished(this.currentTime);
         }

         long waitMillis = TimeUnit.NANOSECONDS.toMillis(this.currentTime - entry.getWaitingForServerFinished());
         HytaleLogger.Api context = LOGGER.at(Level.FINE);
         if (context.isEnabled()) {
            context.log("Client finished interaction but server hasn't! %s, %d, %s, %s", entry.getClientState().state, entry.getIndex(), entry, waitMillis);
         }

         long threshold = this.getOperationTimeoutThreshold();
         if (waitMillis > threshold) {
            HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
            if (ctx.isEnabled()) {
               ctx.log("Client finished interaction earlier than server! %d, %s", entry.getIndex(), entry);
            }

            this.cancelChains(chain);
         }
      }
   }

   private void simulationTick(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain, long tickTime) {
      assert this.commandBuffer != null;

      RootInteraction rootInteraction = chain.getRootInteraction();
      Operation operation = rootInteraction.getOperation(chain.getSimulatedOperationCounter());
      if (operation == null) {
         throw new IllegalStateException("Failed to find operation during simulation tick of chain '" + rootInteraction.getId() + "'");
      } else {
         InteractionEntry entry = chain.getOrCreateInteractionEntry(chain.getClientOperationIndex());
         InteractionContext context = chain.getContext();
         entry.setUseSimulationState(true);

         try {
            context.initEntry(chain, entry, this.entity);
            float time = entry.getTimeInSeconds(tickTime);
            boolean firstRun = false;
            if (entry.getTimestamp() == 0L) {
               time = chain.getTimeShift();
               entry.setTimestamp(tickTime, time);
               firstRun = true;
            }

            TimeResource timeResource = this.commandBuffer.getResource(TimeResource.getResourceType());
            float tickTimeDilation = timeResource.getTimeDilationModifier();
            time *= tickTimeDilation;
            operation.simulateTick(ref, this.entity, firstRun, time, chain.getType(), context, this.cooldownHandler);
         } finally {
            context.deinitEntry(chain, entry, this.entity);
            entry.setUseSimulationState(false);
         }

         if (!entry.setClientState(entry.getSimulationState())) {
            throw new RuntimeException("Simulation failed");
         } else {
            this.removeInteractionIfFinished(ref, chain, entry);
         }
      }
   }

   private boolean syncStart(@Nonnull Ref<EntityStore> ref, @Nonnull SyncInteractionChain packet) {
      assert this.commandBuffer != null;

      int index = packet.chainId;
      if (!packet.initial) {
         if (packet.forkedId == null) {
            HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
            if (ctx.isEnabled()) {
               ctx.log("Got syncStart for %d-%s but packet wasn't the first.", index, packet.forkedId);
            }

            this.sendCancelPacket(index, packet.forkedId);
         }

         return true;
      } else if (packet.forkedId != null) {
         HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
         if (ctx.isEnabled()) {
            ctx.log("Can't start a forked chain from the client: %d %s", index, packet.forkedId);
         }

         this.sendCancelPacket(index, packet.forkedId);
         return true;
      } else {
         InteractionType type = packet.interactionType;
         if (index <= 0) {
            HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
            if (ctx.isEnabled()) {
               ctx.log("Invalid client chainId! Got %d but client id's should be > 0", index);
            }

            this.sendCancelPacket(index, packet.forkedId);
            return true;
         } else if (index <= this.lastClientChainId) {
            HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
            if (ctx.isEnabled()) {
               ctx.log("Invalid client chainId! The last clientChainId was %d but just got %d", this.lastClientChainId, index);
            }

            this.sendCancelPacket(index, packet.forkedId);
            return true;
         } else {
            UUID proxyId = packet.data.proxyId;
            InteractionContext context;
            if (!UUIDUtil.isEmptyOrNull(proxyId)) {
               World world = this.commandBuffer.getExternalData().getWorld();
               Ref<EntityStore> proxyTarget = world.getEntityStore().getRefFromUUID(proxyId);
               if (proxyTarget == null) {
                  if (this.packetQueueTime != 0L
                     && this.currentTime - this.packetQueueTime > TimeUnit.MILLISECONDS.toNanos(this.getOperationTimeoutThreshold()) / 2L) {
                     HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
                     if (ctx.isEnabled()) {
                        ctx.log("Proxy entity never spawned");
                     }

                     this.sendCancelPacket(index, packet.forkedId);
                     return true;
                  }

                  return false;
               }

               context = InteractionContext.forProxyEntity(this, this.entity, proxyTarget);
            } else {
               context = InteractionContext.forInteraction(this, ref, type, packet.equipSlot, this.commandBuffer);
            }

            String rootInteractionId = context.getRootInteractionId(type);
            if (rootInteractionId == null) {
               HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
               if (ctx.isEnabled()) {
                  ctx.log("Missing root interaction: %d, %s, %s", index, this.entity.getInventory().getItemInHand(), type);
               }

               this.sendCancelPacket(index, packet.forkedId);
               return true;
            } else {
               RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(rootInteractionId);
               if (rootInteraction == null) {
                  return false;
               } else if (!this.applyRules(context, packet.data, type, rootInteraction)) {
                  return false;
               } else {
                  Inventory entityInventory = this.entity.getInventory();
                  ItemStack itemInHand = entityInventory.getActiveHotbarItem();
                  ItemStack utilityItem = entityInventory.getUtilityItem();
                  String serverItemInHandId = itemInHand != null ? itemInHand.getItemId() : null;
                  String serverUtilityItemId = utilityItem != null ? utilityItem.getItemId() : null;
                  if (packet.activeHotbarSlot != entityInventory.getActiveHotbarSlot()) {
                     HytaleLogger.Api ctx = LOGGER.at(Level.FINE);
                     if (ctx.isEnabled()) {
                        ctx.log(
                           "Active slot miss match: %d, %d != %d, %s, %s, %s",
                           index,
                           entityInventory.getActiveHotbarSlot(),
                           packet.activeHotbarSlot,
                           serverItemInHandId,
                           packet.itemInHandId,
                           type
                        );
                     }

                     this.sendCancelPacket(index, packet.forkedId);
                     if (this.playerRef != null) {
                        this.playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-1, entityInventory.getActiveHotbarSlot()));
                     }

                     return true;
                  } else if (packet.activeUtilitySlot != entityInventory.getActiveUtilitySlot()) {
                     HytaleLogger.Api ctxx = LOGGER.at(Level.FINE);
                     if (ctxx.isEnabled()) {
                        ctxx.log(
                           "Active slot miss match: %d, %d != %d, %s, %s, %s",
                           index,
                           entityInventory.getActiveUtilitySlot(),
                           packet.activeUtilitySlot,
                           serverItemInHandId,
                           packet.itemInHandId,
                           type
                        );
                     }

                     this.sendCancelPacket(index, packet.forkedId);
                     if (this.playerRef != null) {
                        this.playerRef.getPacketHandler().writeNoCache(new SetActiveSlot(-5, entityInventory.getActiveUtilitySlot()));
                     }

                     return true;
                  } else if (!Objects.equals(serverItemInHandId, packet.itemInHandId)) {
                     HytaleLogger.Api ctxxx = LOGGER.at(Level.FINE);
                     if (ctxxx.isEnabled()) {
                        ctxxx.log("ItemInHand miss match: %d, %s, %s, %s", index, serverItemInHandId, packet.itemInHandId, type);
                     }

                     this.sendCancelPacket(index, packet.forkedId);
                     return true;
                  } else if (!Objects.equals(serverUtilityItemId, packet.utilityItemId)) {
                     HytaleLogger.Api ctxxx = LOGGER.at(Level.FINE);
                     if (ctxxx.isEnabled()) {
                        ctxxx.log("UtilityItem miss match: %d, %s, %s, %s", index, serverUtilityItemId, packet.utilityItemId, type);
                     }

                     this.sendCancelPacket(index, packet.forkedId);
                     return true;
                  } else if (this.isOnCooldown(ref, type, rootInteraction, true)) {
                     return false;
                  } else {
                     InteractionChain chain = this.initChain(packet.data, type, context, rootInteraction, null, true);
                     chain.setChainId(index);
                     this.sync(ref, chain, packet);
                     World world = this.commandBuffer.getExternalData().getWorld();
                     if (packet.data.blockPosition != null) {
                        BlockPosition targetBlock = world.getBaseBlock(packet.data.blockPosition);
                        context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, targetBlock);
                        context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, packet.data.blockPosition);
                        if (!packet.data.blockPosition.equals(targetBlock)) {
                           WorldChunk otherChunk = world.getChunkIfInMemory(
                              ChunkUtil.indexChunkFromBlock(packet.data.blockPosition.x, packet.data.blockPosition.z)
                           );
                           if (otherChunk == null) {
                              HytaleLogger.Api ctxxx = LOGGER.at(Level.FINE);
                              if (ctxxx.isEnabled()) {
                                 ctxxx.log("Unloaded chunk interacted with: %d, %s", index, type);
                              }

                              this.sendCancelPacket(index, packet.forkedId);
                              return true;
                           }

                           int blockId = world.getBlock(targetBlock.x, targetBlock.y, targetBlock.z);
                           int otherBlockId = world.getBlock(packet.data.blockPosition.x, packet.data.blockPosition.y, packet.data.blockPosition.z);
                           if (blockId != otherBlockId) {
                              otherChunk.setBlock(
                                 packet.data.blockPosition.x, packet.data.blockPosition.y, packet.data.blockPosition.z, 0, BlockType.EMPTY, 0, 0, 1052
                              );
                           }
                        }
                     }

                     if (packet.data.entityId >= 0) {
                        EntityStore entityComponentStore = world.getEntityStore();
                        Ref<EntityStore> entityReference = entityComponentStore.getRefFromNetworkId(packet.data.entityId);
                        if (entityReference != null) {
                           context.getMetaStore().putMetaObject(Interaction.TARGET_ENTITY, entityReference);
                        }
                     }

                     if (packet.data.targetSlot != Integer.MIN_VALUE) {
                        context.getMetaStore().putMetaObject(Interaction.TARGET_SLOT, packet.data.targetSlot);
                     }

                     if (packet.data.hitLocation != null) {
                        Vector3f hit = packet.data.hitLocation;
                        context.getMetaStore().putMetaObject(Interaction.HIT_LOCATION, new Vector4d(hit.x, hit.y, hit.z, 1.0));
                     }

                     if (packet.data.hitDetail != null) {
                        context.getMetaStore().putMetaObject(Interaction.HIT_DETAIL, packet.data.hitDetail);
                     }

                     this.lastClientChainId = index;
                     if (!this.tickChain(chain)) {
                        chain.setPreTicked(true);
                        this.chains.put(index, chain);
                     }

                     return true;
                  }
               }
            }
         }
      }
   }

   public void sync(@Nonnull Ref<EntityStore> ref, @Nonnull ChainSyncStorage chainSyncStorage, @Nonnull SyncInteractionChain packet) {
      assert this.commandBuffer != null;

      if (packet.newForks != null) {
         for (SyncInteractionChain fork : packet.newForks) {
            chainSyncStorage.syncFork(ref, this, fork);
         }
      }

      if (packet.interactionData == null) {
         chainSyncStorage.setClientState(packet.state);
      } else {
         for (int i = 0; i < packet.interactionData.length; i++) {
            InteractionSyncData syncData = packet.interactionData[i];
            if (syncData != null) {
               int index = packet.operationBaseIndex + i;
               if (!chainSyncStorage.isSyncDataOutOfOrder(index)) {
                  InteractionEntry interaction = chainSyncStorage.getInteraction(index);
                  if (interaction != null && chainSyncStorage instanceof InteractionChain interactionChain) {
                     if (interaction.getClientState() != null
                           && interaction.getClientState().state != InteractionState.NotFinished
                           && syncData.state == InteractionState.NotFinished
                        || !interaction.setClientState(syncData)) {
                        chainSyncStorage.clearInteractionSyncData(index);
                        interaction.flagDesync();
                        interactionChain.flagDesync();
                        return;
                     }

                     chainSyncStorage.updateSyncPosition(index);
                     HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                     if (context.isEnabled()) {
                        TimeResource timeResource = this.commandBuffer.getResource(TimeResource.getResourceType());
                        float tickTimeDilation = timeResource.getTimeDilationModifier();
                        context.log(
                           "%d, %d: Time (Sync) - Server: %s vs Client: %s",
                           packet.chainId,
                           index,
                           interaction.getTimeInSeconds(this.currentTime) * tickTimeDilation,
                           interaction.getClientState().progress
                        );
                     }

                     this.removeInteractionIfFinished(ref, interactionChain, interaction);
                  } else {
                     chainSyncStorage.putInteractionSyncData(index, syncData);
                  }
               }
            }
         }

         int last = packet.operationBaseIndex + packet.interactionData.length;
         chainSyncStorage.clearInteractionSyncData(last);
         chainSyncStorage.setClientState(packet.state);
      }
   }

   public boolean canRun(@Nonnull InteractionType type, @Nonnull RootInteraction rootInteraction) {
      return this.canRun(type, (short)-1, rootInteraction);
   }

   public boolean canRun(@Nonnull InteractionType type, short equipSlot, @Nonnull RootInteraction rootInteraction) {
      return applyRules(null, type, equipSlot, rootInteraction, this.chains, null);
   }

   public boolean applyRules(
      @Nonnull InteractionContext context, @Nonnull InteractionChainData data, @Nonnull InteractionType type, @Nonnull RootInteraction rootInteraction
   ) {
      List<InteractionChain> chainsToCancel = new ObjectArrayList();
      if (!applyRules(data, type, context.getHeldItemSlot(), rootInteraction, this.chains, chainsToCancel)) {
         return false;
      } else {
         for (InteractionChain interactionChain : chainsToCancel) {
            this.cancelChains(interactionChain);
         }

         return true;
      }
   }

   public void cancelChains(@Nonnull InteractionChain chain) {
      chain.setServerState(InteractionState.Failed);
      chain.setClientState(InteractionState.Failed);
      this.sendCancelPacket(chain);
      ObjectIterator var2 = chain.getForkedChains().values().iterator();

      while (var2.hasNext()) {
         InteractionChain fork = (InteractionChain)var2.next();
         this.cancelChains(fork);
      }
   }

   private static boolean applyRules(
      @Nullable InteractionChainData data,
      @Nonnull InteractionType type,
      int heldItemSlot,
      @Nullable RootInteraction rootInteraction,
      @Nonnull Map<?, InteractionChain> chains,
      @Nullable List<InteractionChain> chainsToCancel
   ) {
      if (!chains.isEmpty() && rootInteraction != null) {
         for (InteractionChain chain : chains.values()) {
            if ((chain.getForkedChainId() == null || chain.isPredicted())
               && (data == null || Objects.equals(chain.getChainData().proxyId, data.proxyId))
               && (type != InteractionType.Equipped || chain.getType() != InteractionType.Equipped || chain.getContext().getHeldItemSlot() == heldItemSlot)) {
               if (chain.getServerState() == InteractionState.NotFinished) {
                  RootInteraction currentRoot = chain.getRootInteraction();
                  Operation currentOp = currentRoot.getOperation(chain.getOperationCounter());
                  if (rootInteraction.getRules()
                     .validateInterrupts(type, rootInteraction.getData().getTags(), chain.getType(), currentRoot.getData().getTags(), currentRoot.getRules())) {
                     if (chainsToCancel != null) {
                        chainsToCancel.add(chain);
                     }
                  } else if (currentOp != null
                     && currentOp.getRules() != null
                     && rootInteraction.getRules()
                        .validateInterrupts(type, rootInteraction.getData().getTags(), chain.getType(), currentOp.getTags(), currentOp.getRules())) {
                     if (chainsToCancel != null) {
                        chainsToCancel.add(chain);
                     }
                  } else {
                     if (rootInteraction.getRules()
                        .validateBlocked(type, rootInteraction.getData().getTags(), chain.getType(), currentRoot.getData().getTags(), currentRoot.getRules())) {
                        return false;
                     }

                     if (currentOp != null
                        && currentOp.getRules() != null
                        && rootInteraction.getRules()
                           .validateBlocked(type, rootInteraction.getData().getTags(), chain.getType(), currentOp.getTags(), currentOp.getRules())) {
                        return false;
                     }
                  }
               }

               if ((chainsToCancel == null || chainsToCancel.isEmpty())
                  && !applyRules(data, type, heldItemSlot, rootInteraction, chain.getForkedChains(), chainsToCancel)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public boolean tryStartChain(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull RootInteraction rootInteraction
   ) {
      InteractionChain chain = this.initChain(type, context, rootInteraction, false);
      if (!this.applyRules(context, chain.getChainData(), type, rootInteraction)) {
         return false;
      } else {
         this.executeChain(ref, commandBuffer, chain);
         return true;
      }
   }

   public void startChain(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull RootInteraction rootInteraction
   ) {
      InteractionChain chain = this.initChain(type, context, rootInteraction, false);
      this.executeChain(ref, commandBuffer, chain);
   }

   @Nonnull
   public InteractionChain initChain(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull RootInteraction rootInteraction, boolean forceRemoteSync
   ) {
      return this.initChain(type, context, rootInteraction, -1, null, forceRemoteSync);
   }

   @Nonnull
   public InteractionChain initChain(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull RootInteraction rootInteraction,
      int entityId,
      @Nullable BlockPosition blockPosition,
      boolean forceRemoteSync
   ) {
      InteractionChainData data = new InteractionChainData(entityId, UUIDUtil.EMPTY_UUID, null, null, blockPosition, Integer.MIN_VALUE, null);
      return this.initChain(data, type, context, rootInteraction, null, forceRemoteSync);
   }

   @Nonnull
   public InteractionChain initChain(
      @Nonnull InteractionChainData data,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull RootInteraction rootInteraction,
      @Nullable Runnable onCompletion,
      boolean forceRemoteSync
   ) {
      return new InteractionChain(type, context, data, rootInteraction, onCompletion, forceRemoteSync || !this.hasRemoteClient);
   }

   public void queueExecuteChain(@Nonnull InteractionChain chain) {
      this.chainStartQueue.add(chain);
   }

   public void executeChain(@Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionChain chain) {
      this.commandBuffer = commandBuffer;
      this.executeChain0(ref, chain);
      this.commandBuffer = null;
   }

   private void executeChain0(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionChain chain) {
      if (this.isOnCooldown(ref, chain.getType(), chain.getInitialRootInteraction(), false)) {
         chain.setServerState(InteractionState.Failed);
         chain.setClientState(InteractionState.Failed);
      } else {
         int index = --this.lastServerChainId;
         if (index >= 0) {
            index = this.lastServerChainId = -1;
         }

         chain.setChainId(index);
         if (!this.tickChain(chain)) {
            LOGGER.at(Level.FINE).log("Add Chain: %d, %s", index, chain);
            chain.setPreTicked(true);
            this.chains.put(index, chain);
         }
      }
   }

   private boolean isOnCooldown(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionType type, @Nonnull RootInteraction root, boolean remote) {
      assert this.commandBuffer != null;

      InteractionCooldown cooldown = root.getCooldown();
      String cooldownId = root.getId();
      float cooldownTime = InteractionTypeUtils.getDefaultCooldown(type);
      float[] cooldownChargeTimes = DEFAULT_CHARGE_TIMES;
      boolean interruptRecharge = false;
      if (cooldown != null) {
         cooldownTime = cooldown.cooldown;
         if (cooldown.chargeTimes != null && cooldown.chargeTimes.length > 0) {
            cooldownChargeTimes = cooldown.chargeTimes;
         }

         if (cooldown.cooldownId != null) {
            cooldownId = cooldown.cooldownId;
         }

         if (cooldown.interruptRecharge) {
            interruptRecharge = true;
         }

         if (cooldown.clickBypass && remote) {
            this.cooldownHandler.resetCooldown(cooldownId, cooldownTime, cooldownChargeTimes, interruptRecharge);
            return false;
         }
      }

      Player playerComponent = this.commandBuffer.getComponent(ref, Player.getComponentType());
      GameMode gameMode = playerComponent != null ? playerComponent.getGameMode() : GameMode.Adventure;
      RootInteractionSettings settings = root.getSettings().get(gameMode);
      if (settings != null) {
         cooldown = settings.cooldown;
         if (cooldown != null) {
            cooldownTime = cooldown.cooldown;
            if (cooldown.chargeTimes != null && cooldown.chargeTimes.length > 0) {
               cooldownChargeTimes = cooldown.chargeTimes;
            }

            if (cooldown.cooldownId != null) {
               cooldownId = cooldown.cooldownId;
            }

            if (cooldown.interruptRecharge) {
               interruptRecharge = true;
            }

            if (cooldown.clickBypass && remote) {
               this.cooldownHandler.resetCooldown(cooldownId, cooldownTime, cooldownChargeTimes, interruptRecharge);
               return false;
            }
         }

         if (settings.allowSkipChainOnClick && remote) {
            this.cooldownHandler.resetCooldown(cooldownId, cooldownTime, cooldownChargeTimes, interruptRecharge);
            return false;
         }
      }

      return this.cooldownHandler.isOnCooldown(root, cooldownId, cooldownTime, cooldownChargeTimes, interruptRecharge);
   }

   public void tryRunHeldInteraction(@Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type) {
      this.tryRunHeldInteraction(ref, commandBuffer, type, (short)-1);
   }

   public void tryRunHeldInteraction(
      @Nonnull Ref<EntityStore> ref, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, short equipSlot
   ) {
      label38: {
         Inventory inventory = this.entity.getInventory();
         ItemStack itemStack;

         itemStack = switch (type) {
            case Held -> inventory.getItemInHand();
            case HeldOffhand -> inventory.getUtilityItem();
            case Equipped -> {
               if (equipSlot == -1) {
                  throw new IllegalArgumentException();
               }

               yield inventory.getArmor().getItemStack(equipSlot);
               if (itemStack != null && !itemStack.isEmpty()) {
                  String rootId = itemStack.getItem().getInteractions().get(type);
                  if (rootId == null) {
                     return;
                  } else {
                     RootInteraction root = RootInteraction.getAssetMap().getAsset(rootId);
                     if (root != null && this.canRun(type, equipSlot, root)) {
                        InteractionContext context = InteractionContext.forInteraction(this, ref, type, equipSlot, commandBuffer);
                        this.startChain(ref, commandBuffer, type, context, root);
                        return;
                     } else {
                        return;
                     }
                  }
               } else {
                  return;
               }
            }
            default -> throw new IllegalArgumentException();
         };
      }
   }

   public void sendSyncPacket(@Nonnull InteractionChain chain, int operationBaseIndex, @Nullable List<InteractionSyncData> interactionData) {
      if (!chain.hasSentInitial() || interactionData != null && !ListUtil.emptyOrAllNull(interactionData) || !chain.getNewForks().isEmpty()) {
         if (this.playerRef != null) {
            SyncInteractionChain packet = makeSyncPacket(chain, operationBaseIndex, interactionData);
            this.syncPackets.add(packet);
         }
      }
   }

   @Nonnull
   private static SyncInteractionChain makeSyncPacket(
      @Nonnull InteractionChain chain, int operationBaseIndex, @Nullable List<InteractionSyncData> interactionData
   ) {
      SyncInteractionChain[] forks = null;
      List<InteractionChain> newForks = chain.getNewForks();
      if (!newForks.isEmpty()) {
         forks = new SyncInteractionChain[newForks.size()];

         for (int i = 0; i < newForks.size(); i++) {
            InteractionChain fc = newForks.get(i);
            forks[i] = makeSyncPacket(fc, 0, null);
         }

         newForks.clear();
      }

      SyncInteractionChain packet = new SyncInteractionChain(
         0,
         0,
         0,
         null,
         null,
         null,
         !chain.hasSentInitial(),
         false,
         chain.hasSentInitial() ? Integer.MIN_VALUE : RootInteraction.getRootInteractionIdOrUnknown(chain.getInitialRootInteraction().getId()),
         chain.getType(),
         chain.getContext().getHeldItemSlot(),
         chain.getChainId(),
         chain.getForkedChainId(),
         chain.getChainData(),
         chain.getServerState(),
         forks,
         operationBaseIndex,
         interactionData == null ? null : interactionData.toArray(InteractionSyncData[]::new)
      );
      chain.setSentInitial(true);
      return packet;
   }

   private void sendCancelPacket(@Nonnull InteractionChain chain) {
      this.sendCancelPacket(chain.getChainId(), chain.getForkedChainId());
   }

   public void sendCancelPacket(int chainId, ForkedChainId forkedChainId) {
      if (this.playerRef != null) {
         this.playerRef.getPacketHandler().writeNoCache(new CancelInteractionChain(chainId, forkedChainId));
      }
   }

   public void clear() {
      this.forEachInteraction((chain, _i, _a) -> {
         chain.setServerState(InteractionState.Failed);
         chain.setClientState(InteractionState.Failed);
         this.sendCancelPacket(chain);
         return null;
      }, null);
      this.chainStartQueue.clear();
   }

   public void clearAllGlobalTimeShift(float dt) {
      if (this.timeShiftsDirty) {
         boolean clearFlag = true;

         for (int i = 0; i < this.globalTimeShift.length; i++) {
            if (!this.globalTimeShiftDirty[i]) {
               this.globalTimeShift[i] = 0.0F;
            } else {
               clearFlag = false;
               this.globalTimeShift[i] = this.globalTimeShift[i] + dt;
            }
         }

         Arrays.fill(this.globalTimeShiftDirty, false);
         if (clearFlag) {
            this.timeShiftsDirty = false;
         }
      }
   }

   public void setGlobalTimeShift(@Nonnull InteractionType type, float shift) {
      if (shift < 0.0F) {
         throw new IllegalArgumentException("Can't shift backwards");
      } else {
         this.globalTimeShift[type.ordinal()] = shift;
         this.globalTimeShiftDirty[type.ordinal()] = true;
         this.timeShiftsDirty = true;
      }
   }

   public float getGlobalTimeShift(@Nonnull InteractionType type) {
      return this.globalTimeShift[type.ordinal()];
   }

   public <T> T forEachInteraction(@Nonnull TriFunction<InteractionChain, Interaction, T, T> func, @Nonnull T val) {
      return forEachInteraction(this.chains, func, val);
   }

   private static <T> T forEachInteraction(
      @Nonnull Map<?, InteractionChain> chains, @Nonnull TriFunction<InteractionChain, Interaction, T, T> func, @Nonnull T val
   ) {
      if (chains.isEmpty()) {
         return val;
      } else {
         for (InteractionChain chain : chains.values()) {
            Operation operation = chain.getRootInteraction().getOperation(chain.getOperationCounter());
            if (operation != null && operation.getInnerOperation() instanceof Interaction interaction) {
               val = func.apply(chain, interaction, val);
            }

            val = forEachInteraction(chain.getForkedChains(), func, val);
         }

         return val;
      }
   }

   public void walkChain(
      @Nonnull Ref<EntityStore> ref, @Nonnull Collector collector, @Nonnull InteractionType type, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.walkChain(ref, collector, type, null, componentAccessor);
   }

   public void walkChain(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Collector collector,
      @Nonnull InteractionType type,
      @Nullable RootInteraction rootInteraction,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      walkChain(collector, type, InteractionContext.forInteraction(this, ref, type, componentAccessor), rootInteraction);
   }

   public static void walkChain(
      @Nonnull Collector collector, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable RootInteraction rootInteraction
   ) {
      if (rootInteraction == null) {
         String rootInteractionId = context.getRootInteractionId(type);
         if (rootInteractionId == null) {
            throw new IllegalArgumentException("No interaction ID found for " + type + ", " + context);
         }

         rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
      }

      if (rootInteraction == null) {
         throw new IllegalArgumentException("No interactions are defined for " + type + ", " + context);
      } else {
         collector.start();
         collector.into(context, null);
         walkInteractions(collector, context, CollectorTag.ROOT, rootInteraction.getInteractionIds());
         collector.outof();
         collector.finished();
      }
   }

   public static boolean walkInteractions(
      @Nonnull Collector collector, @Nonnull InteractionContext context, @Nonnull CollectorTag tag, @Nonnull String[] interactionIds
   ) {
      for (String id : interactionIds) {
         if (walkInteraction(collector, context, tag, id)) {
            return true;
         }
      }

      return false;
   }

   public static boolean walkInteraction(@Nonnull Collector collector, @Nonnull InteractionContext context, @Nonnull CollectorTag tag, @Nullable String id) {
      if (id == null) {
         return false;
      } else {
         Interaction interaction = Interaction.getAssetMap().getAsset(id);
         if (interaction == null) {
            throw new IllegalArgumentException("Failed to find interaction: " + id);
         } else if (collector.collect(tag, context, interaction)) {
            return true;
         } else {
            collector.into(context, interaction);
            interaction.walk(collector, context);
            collector.outof();
            return false;
         }
      }
   }

   public ObjectList<SyncInteractionChain> getSyncPackets() {
      return this.syncPackets;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      InteractionManager manager = new InteractionManager(this.entity, this.playerRef, this.interactionSimulationHandler);
      manager.copyFrom(this);
      return manager;
   }

   public static class ChainCancelledException extends RuntimeException {
      @Nonnull
      private final InteractionState state;

      public ChainCancelledException(@Nonnull InteractionState state) {
         this.state = state;
      }
   }
}
