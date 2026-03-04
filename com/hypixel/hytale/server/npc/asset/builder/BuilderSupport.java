package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.view.event.block.BlockEventType;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.role.support.EntitySupport;
import com.hypixel.hytale.server.npc.role.support.RoleStats;
import com.hypixel.hytale.server.npc.storage.AlarmStore;
import com.hypixel.hytale.server.npc.util.Alarm;
import com.hypixel.hytale.server.npc.util.Timer;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSupport {
   private final BuilderManager builderManager;
   @Nonnull
   private final NPCEntity npcEntity;
   private final Holder<EntityStore> holder;
   private final ExecutionContext executionContext;
   private boolean requireLeashPosition;
   private final SlotMapper flagSlotMapper = new SlotMapper();
   private final SlotMapper beaconSlotMapper = new SlotMapper();
   private final SlotMapper targetSlotMapper = new SlotMapper(true);
   private final SlotMapper positionSlotMapper = new SlotMapper();
   private final ReferenceSlotMapper<Timer> timerSlotMapper = new ReferenceSlotMapper<>(Timer::new);
   private final SlotMapper searchRaySlotMapper = new SlotMapper();
   private final SlotMapper parameterSlotMapper = new SlotMapper();
   @Nonnull
   private final Object2IntMap<String> instructionSlotMappings;
   private final Int2ObjectMap<String> instructionNameMappings = new Int2ObjectOpenHashMap();
   private final List<Instruction> instructions = new ObjectArrayList();
   private EventSlotMapper<BlockEventType> playerBlockEventSlotMapper;
   private EventSlotMapper<BlockEventType> npcBlockEventSlotMapper;
   private EventSlotMapper<EntityEventType> playerEntityEventSlotMapper;
   private EventSlotMapper<EntityEventType> npcEntityEventSlotMapper;
   private Scope globalScope;
   private int currentComponentIndex;
   private IntStack componentIndexStack;
   private int componentIndexSource;
   private int currentAttackIndex;
   private Int2IntMap componentLocalStateMachines;
   private BitSet localStateMachineAutoResetStates;
   private final StateMappingHelper stateHelper;
   private List<Entry<StateMappingHelper, StatePair[]>> modifiedStateMap;
   private IntSet blackboardBlockSets;
   private IntSet blockSensorResetBlockSets;
   private boolean requiresAttitudeOverrideMemory;
   private boolean trackInteractions;
   private InstructionType currentInstructionContext = InstructionType.Component;
   private ComponentContext currentComponentContext;
   @Nonnull
   private final StdScope sensorScope;
   @Nonnull
   private final Builder<?> roleBuilder;
   private final RoleStats roleStats;
   private StateEvaluator stateEvaluator;
   private ValueStore.Builder valueStoreBuilder;
   private final ArrayDeque<String> stateStack = new ArrayDeque<>();

   public BuilderSupport(
      BuilderManager builderManager,
      @Nonnull NPCEntity npcEntity,
      Holder<EntityStore> holder,
      ExecutionContext executionContext,
      @Nonnull Builder<?> roleBuilder,
      RoleStats roleStats
   ) {
      this.builderManager = builderManager;
      this.npcEntity = npcEntity;
      this.holder = holder;
      this.executionContext = executionContext;
      this.roleBuilder = roleBuilder;
      this.stateHelper = roleBuilder.getStateMappingHelper();
      this.roleStats = roleStats;
      this.sensorScope = EntitySupport.createScope(npcEntity);
      this.instructionSlotMappings = new Object2IntOpenHashMap();
      this.instructionSlotMappings.defaultReturnValue(Integer.MIN_VALUE);
   }

   public BuilderManager getBuilderManager() {
      return this.builderManager;
   }

   @Nonnull
   public NPCEntity getEntity() {
      return this.npcEntity;
   }

   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   public ExecutionContext getExecutionContext() {
      return this.executionContext;
   }

   @Nonnull
   public Builder<?> getParentSpawnable() {
      return this.roleBuilder;
   }

   public void setScope(Scope scope) {
      this.getExecutionContext().setScope(scope);
   }

   public void setGlobalScope(Scope scope) {
      this.globalScope = scope;
   }

   public Scope getGlobalScope() {
      return this.globalScope;
   }

   public void setRequireLeashPosition() {
      this.requireLeashPosition = true;
   }

   public int getFlagSlot(String name) {
      return this.flagSlotMapper.getSlot(name);
   }

   public Timer getTimerByName(String name) {
      return this.timerSlotMapper.getReference(name);
   }

   public int getBeaconMessageSlot(String name) {
      return this.beaconSlotMapper.getSlot(name);
   }

   public int getTargetSlot(String name) {
      return this.targetSlotMapper.getSlot(name);
   }

   public Alarm getAlarm(String name) {
      NPCEntity npc = this.holder.getComponent(NPCEntity.getComponentType());
      AlarmStore alarmStore = npc.getAlarmStore();
      return alarmStore.get(this.npcEntity, name);
   }

   @Nullable
   public Object2IntMap<String> getTargetSlotMappings() {
      return this.targetSlotMapper.getSlotMappings();
   }

   @Nullable
   public Int2ObjectMap<String> getTargetSlotToNameMap() {
      return this.targetSlotMapper.getNameMap();
   }

   public int getPositionSlot(String name) {
      return this.positionSlotMapper.getSlot(name);
   }

   public int getParameterSlot(String name) {
      return this.parameterSlotMapper.getSlot(name);
   }

   public int getSearchRaySlot(String name) {
      return this.searchRaySlotMapper.getSlot(name);
   }

   @Nullable
   public Vector3d[] allocatePositionSlots() {
      return allocatePositionSlots(this.positionSlotMapper);
   }

   public boolean requiresLeashPosition() {
      return this.requireLeashPosition;
   }

   public StateEvaluator getStateEvaluator() {
      return this.stateEvaluator;
   }

   public void setStateEvaluator(StateEvaluator stateEvaluator) {
      this.stateEvaluator = stateEvaluator;
   }

   public boolean[] allocateFlags() {
      int slotCount = this.flagSlotMapper.slotCount();
      return slotCount == 0 ? null : new boolean[slotCount];
   }

   @Nullable
   public Tickable[] allocateTimers() {
      List<Timer> referenceList = this.timerSlotMapper.getReferenceList();
      return referenceList.isEmpty() ? null : referenceList.toArray(Tickable[]::new);
   }

   @Nullable
   public Vector3d[] allocateSearchRayPositionSlots() {
      return allocatePositionSlots(this.searchRaySlotMapper);
   }

   @Nonnull
   public StdScope getSensorScope() {
      return this.sensorScope;
   }

   public void setToNewComponent() {
      if (this.componentIndexStack == null) {
         this.componentIndexStack = new IntArrayList();
      }

      this.componentIndexStack.push(this.currentComponentIndex);
      this.currentComponentIndex = this.componentIndexSource++;
   }

   public void addComponentLocalStateMachine(int defaultState) {
      if (this.componentLocalStateMachines == null) {
         this.componentLocalStateMachines = new Int2IntOpenHashMap();
         this.componentLocalStateMachines.defaultReturnValue(Integer.MIN_VALUE);
      }

      this.componentLocalStateMachines.put(this.getComponentIndex(), defaultState);
   }

   public int getComponentIndex() {
      return this.currentComponentIndex;
   }

   public void popComponent() {
      this.currentComponentIndex = this.componentIndexStack.popInt();
   }

   public boolean hasComponentLocalStateMachines() {
      return this.componentLocalStateMachines != null;
   }

   public Int2IntMap getComponentLocalStateMachines() {
      return this.componentLocalStateMachines;
   }

   public void setLocalStateMachineAutoReset() {
      if (this.localStateMachineAutoResetStates == null) {
         this.localStateMachineAutoResetStates = new BitSet();
      }

      this.localStateMachineAutoResetStates.set(this.getComponentIndex());
   }

   public BitSet getLocalStateMachineAutoResetStates() {
      return this.localStateMachineAutoResetStates;
   }

   public StateMappingHelper getStateHelper() {
      return this.stateHelper;
   }

   @Nullable
   public Object2IntMap<String> getBeaconSlotMappings() {
      return this.beaconSlotMapper.getSlotMappings();
   }

   public boolean hasBlockEventSupport() {
      return this.playerBlockEventSlotMapper != null || this.npcBlockEventSlotMapper != null;
   }

   public EventSlotMapper<BlockEventType> getPlayerBlockEventSlotMapper() {
      return this.playerBlockEventSlotMapper;
   }

   public EventSlotMapper<BlockEventType> getNPCBlockEventSlotMapper() {
      return this.npcBlockEventSlotMapper;
   }

   public boolean hasEntityEventSupport() {
      return this.playerEntityEventSlotMapper != null || this.npcEntityEventSlotMapper != null;
   }

   public EventSlotMapper<EntityEventType> getPlayerEntityEventSlotMapper() {
      return this.playerEntityEventSlotMapper;
   }

   public EventSlotMapper<EntityEventType> getNPCEntityEventSlotMapper() {
      return this.npcEntityEventSlotMapper;
   }

   public int getInstructionSlot(@Nullable String name) {
      int slot = this.instructionSlotMappings.getInt(name);
      if (slot == Integer.MIN_VALUE) {
         slot = this.instructions.size();
         if (name != null && !name.isEmpty()) {
            this.instructionSlotMappings.put(name, slot);
            this.instructionNameMappings.put(slot, name);
         }

         this.instructions.add(null);
      }

      return slot;
   }

   public void putInstruction(int slot, Instruction instruction) {
      Objects.requireNonNull(instruction, "Instruction cannot be null when putting instruction");
      if (slot < 0 || slot >= this.instructions.size()) {
         throw new IllegalArgumentException("Slot for putting instruction must be >= 0 and < the size of the list");
      } else if (this.instructions.get(slot) != null) {
         throw new IllegalStateException(String.format("Duplicate instruction with name: %s", this.instructionNameMappings.get(slot)));
      } else {
         this.instructions.set(slot, instruction);
      }
   }

   @Nonnull
   public Instruction[] getInstructionSlotMappings() {
      Instruction[] slots = this.instructions.toArray(Instruction[]::new);

      for (int i = 0; i < slots.length; i++) {
         Instruction instruction = slots[i];
         if (instruction == null) {
            throw new IllegalStateException("Instruction: " + (String)this.instructionNameMappings.get(i) + " doesn't exist");
         }
      }

      return slots;
   }

   public void setModifiedStateMap(@Nonnull StateMappingHelper helper, @Nonnull StatePair[] map) {
      if (this.modifiedStateMap == null) {
         this.modifiedStateMap = new ObjectArrayList();
      }

      this.modifiedStateMap.add(Map.entry(helper, map));
   }

   @Nonnull
   public StatePair getMappedStatePair(int index) {
      StatePair result = null;

      for (int i = this.modifiedStateMap.size() - 1; i >= 0; i--) {
         Entry<StateMappingHelper, StatePair[]> entry = this.modifiedStateMap.get(i);
         result = entry.getValue()[index];
         index = entry.getKey().getComponentImportStateIndex(result.getFullStateName());
         if (index < 0) {
            break;
         }
      }

      Objects.requireNonNull(result, "Result should not be null after iterating mapped state pairs");
      return result;
   }

   public void popModifiedStateMap() {
      this.modifiedStateMap.removeLast();
   }

   public void requireBlockTypeBlackboard(int blockSet) {
      if (this.blackboardBlockSets == null) {
         this.blackboardBlockSets = new IntOpenHashSet();
      }

      this.blackboardBlockSets.add(blockSet);
   }

   public void registerBlockSensorResetAction(int blockSet) {
      if (this.blockSensorResetBlockSets == null) {
         this.blockSensorResetBlockSets = new IntOpenHashSet();
      }

      this.blockSensorResetBlockSets.add(blockSet);
   }

   public boolean requiresBlockTypeBlackboard() {
      return this.blackboardBlockSets != null;
   }

   @Nonnull
   public IntList getBlockTypeBlackboardBlockSets() {
      if (this.blockSensorResetBlockSets != null) {
         this.blockSensorResetBlockSets
            .forEach(
               blockSet -> {
                  if (!this.blackboardBlockSets.contains(blockSet)) {
                     throw new IllegalStateException(
                        String.format("No block sensors match BlockSet %s in ResetBlockSensors action", BlockSet.getAssetMap().getAsset(blockSet).getId())
                     );
                  }
               }
            );
      }

      IntArrayList blockSets = new IntArrayList();
      blockSets.addAll(this.blackboardBlockSets);
      blockSets.trim();
      return IntLists.unmodifiable(blockSets);
   }

   public int getBlockEventSlot(BlockEventType type, int blockSet, double maxRange, boolean player) {
      if (player) {
         if (this.playerBlockEventSlotMapper == null) {
            this.playerBlockEventSlotMapper = new EventSlotMapper<>(BlockEventType.class, BlockEventType.VALUES);
         }

         return this.playerBlockEventSlotMapper.getEventSlot(type, blockSet, maxRange);
      } else {
         if (this.npcBlockEventSlotMapper == null) {
            this.npcBlockEventSlotMapper = new EventSlotMapper<>(BlockEventType.class, BlockEventType.VALUES);
         }

         return this.npcBlockEventSlotMapper.getEventSlot(type, blockSet, maxRange);
      }
   }

   @Nullable
   public IntSet getBlockChangeSets(BlockEventType type) {
      IntSet playerEventSets = this.playerBlockEventSlotMapper != null ? this.playerBlockEventSlotMapper.getEventSets().get(type) : null;
      IntSet npcEventSets = this.npcBlockEventSlotMapper != null ? this.npcBlockEventSlotMapper.getEventSets().get(type) : null;
      if (playerEventSets == null && npcEventSets == null) {
         return null;
      } else {
         IntOpenHashSet set = new IntOpenHashSet();
         if (playerEventSets != null) {
            set.addAll(playerEventSets);
         }

         if (npcEventSets != null) {
            set.addAll(npcEventSets);
         }

         set.trim();
         return IntSets.unmodifiable(set);
      }
   }

   public int getEntityEventSlot(EntityEventType type, int npcGroup, double maxRange, boolean player) {
      if (player) {
         if (this.playerEntityEventSlotMapper == null) {
            this.playerEntityEventSlotMapper = new EventSlotMapper<>(EntityEventType.class, EntityEventType.VALUES);
         }

         return this.playerEntityEventSlotMapper.getEventSlot(type, npcGroup, maxRange);
      } else {
         if (this.npcEntityEventSlotMapper == null) {
            this.npcEntityEventSlotMapper = new EventSlotMapper<>(EntityEventType.class, EntityEventType.VALUES);
         }

         return this.npcEntityEventSlotMapper.getEventSlot(type, npcGroup, maxRange);
      }
   }

   @Nullable
   public IntSet getEventNPCGroups(EntityEventType type) {
      IntSet playerEventSets = this.playerEntityEventSlotMapper != null ? this.playerEntityEventSlotMapper.getEventSets().get(type) : null;
      IntSet npcEventSets = this.npcEntityEventSlotMapper != null ? this.npcEntityEventSlotMapper.getEventSets().get(type) : null;
      if (playerEventSets == null && npcEventSets == null) {
         return null;
      } else {
         IntOpenHashSet set = new IntOpenHashSet();
         if (playerEventSets != null) {
            set.addAll(playerEventSets);
         }

         if (npcEventSets != null) {
            set.addAll(npcEventSets);
         }

         set.trim();
         return IntSets.unmodifiable(set);
      }
   }

   public void requireAttitudeOverrideMemory() {
      this.requiresAttitudeOverrideMemory = true;
   }

   public void trackInteractions() {
      this.trackInteractions = true;
   }

   public boolean isTrackInteractions() {
      return this.trackInteractions;
   }

   public boolean requiresAttitudeOverrideMemory() {
      return this.requiresAttitudeOverrideMemory;
   }

   public void setCurrentInstructionContext(InstructionType context) {
      this.currentInstructionContext = context;
   }

   public InstructionType getCurrentInstructionContext() {
      return this.currentInstructionContext;
   }

   public ComponentContext getCurrentComponentContext() {
      return this.currentComponentContext;
   }

   public void setCurrentComponentContext(ComponentContext currentComponentContext) {
      this.currentComponentContext = currentComponentContext;
   }

   public RoleStats getRoleStats() {
      return this.roleStats;
   }

   public int getNextAttackIndex() {
      return this.currentAttackIndex++;
   }

   public int getValueStoreStringSlot(String name) {
      if (this.valueStoreBuilder == null) {
         this.valueStoreBuilder = new ValueStore.Builder();
      }

      return this.valueStoreBuilder.getStringSlot(name);
   }

   public int getValueStoreIntSlot(String name) {
      if (this.valueStoreBuilder == null) {
         this.valueStoreBuilder = new ValueStore.Builder();
      }

      return this.valueStoreBuilder.getIntSlot(name);
   }

   public int getValueStoreDoubleSlot(String name) {
      if (this.valueStoreBuilder == null) {
         this.valueStoreBuilder = new ValueStore.Builder();
      }

      return this.valueStoreBuilder.getDoubleSlot(name);
   }

   public ValueStore.Builder getValueStoreBuilder() {
      return this.valueStoreBuilder;
   }

   @Nullable
   public String getCurrentStateName() {
      return this.stateStack.peek();
   }

   public void pushCurrentStateName(@Nonnull String currentStateName) {
      this.stateStack.push(currentStateName);
   }

   public void popCurrentStateName() {
      this.stateStack.pop();
   }

   @Nullable
   private static Vector3d[] allocatePositionSlots(@Nonnull SlotMapper mapper) {
      int slotCount = mapper.slotCount();
      if (slotCount == 0) {
         return null;
      } else {
         Vector3d[] slots = new Vector3d[slotCount];

         for (int i = 0; i < slots.length; i++) {
            slots[i] = new Vector3d();
         }

         return slots;
      }
   }
}
