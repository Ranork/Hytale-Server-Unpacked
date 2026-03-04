package com.hypixel.hytale.server.npc.role;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.movement.GroupSteeringAccumulator;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceAvoidCollision;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.role.support.EntitySupport;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.role.support.RoleStats;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Role implements IAnnotatedComponentCollection {
   public static final double INTERACTION_PLAYER_DISTANCE = 10.0;
   public static final boolean DEBUG_APPLIED_FORCES = false;
   @Nonnull
   protected final CombatSupport combatSupport;
   @Nonnull
   protected final StateSupport stateSupport;
   @Nonnull
   protected final MarkedEntitySupport markedEntitySupport;
   @Nonnull
   protected final WorldSupport worldSupport;
   @Nonnull
   protected final EntitySupport entitySupport;
   @Nonnull
   protected final PositionCache positionCache;
   @Nonnull
   protected final DebugSupport debugSupport;
   protected final int initialMaxHealth;
   protected final double collisionProbeDistance;
   protected final double collisionRadius;
   protected final double collisionForceFalloff;
   protected final float collisionViewAngle;
   protected final float collisionViewHalfAngleCosine;
   protected final Steering bodySteering = new Steering();
   protected final Steering headSteering = new Steering();
   protected final SteeringForceAvoidCollision steeringForceAvoidCollision = new SteeringForceAvoidCollision();
   protected final GroupSteeringAccumulator groupSteeringAccumulator = new GroupSteeringAccumulator();
   protected final Vector3d separation = new Vector3d();
   protected final Set<Ref<EntityStore>> ignoredEntitiesForAvoidance = new HashSet<>();
   protected final double entityAvoidanceStrength;
   protected final Role.AvoidanceMode avoidanceMode;
   protected final boolean isAvoidingEntities;
   protected final double separationDistance;
   protected final double separationWeight;
   protected final double separationDistanceTarget;
   protected final double separationNearRadiusTarget;
   protected final double separationFarRadiusTarget;
   protected final boolean applySeparation;
   protected final Vector3d lastSeparationSteering = new Vector3d();
   @Nullable
   protected final float[] headPitchAngleRange;
   protected final boolean stayInEnvironment;
   protected final String allowedEnvironments;
   @Nullable
   protected final String[] flockSpawnTypes;
   protected final boolean flockSpawnTypesRandom;
   @Nonnull
   protected final String[] flockAllowedRoles;
   protected final boolean canLeadFlock;
   protected final double flockWeightAlignment;
   protected final double flockWeightSeparation;
   protected final double flockWeightCohesion;
   protected final double flockInfluenceRange;
   protected final boolean corpseStaysInFlock;
   protected final double inertia;
   protected final double knockbackScale;
   protected final boolean breathesInAir;
   protected final boolean breathesInWater;
   protected final boolean pickupDropOnDeath;
   @Nullable
   protected final String[] hotbarItems;
   @Nullable
   protected final String[] offHandItems;
   protected final double deathAnimationTime;
   protected final float despawnAnimationTime;
   protected final String dropListId;
   @Nullable
   protected final String deathInteraction;
   protected final boolean invulnerable;
   protected final int inventorySlots;
   protected final String inventoryContentsDropList;
   protected final int hotbarSlots;
   protected final int offHandSlots;
   protected final byte defaultOffHandSlot;
   protected final List<Role.DeferredAction> deferredActions = new ObjectArrayList();
   protected final RoleStats roleStats;
   @Nullable
   protected final String balanceAsset;
   @Nullable
   protected final Map<String, String> interactionVars;
   protected int roleIndex;
   protected String roleName;
   protected String appearance;
   protected boolean isActivated;
   @Nonnull
   protected Map<String, MotionController> motionControllers = new HashMap<>();
   protected MotionController activeMotionController;
   protected int[] flockSpawnTypeIndices;
   protected boolean requiresLeashPosition;
   protected boolean hasReachedTerminalAction;
   @Nullable
   protected String[] armor;
   protected boolean[] flags;
   protected Instruction rootInstruction;
   @Nullable
   protected Instruction lastBodyMotionStep;
   @Nullable
   protected Instruction lastHeadMotionStep;
   protected Instruction[] indexedInstructions;
   @Nullable
   protected Instruction interactionInstruction;
   @Nullable
   protected Instruction deathInstruction;
   protected Instruction currentTreeModeStep;
   protected boolean roleChangeRequested;
   protected final boolean isMemory;
   protected final String memoriesNameOverride;
   protected final boolean isMemoriesNameOverriden;
   protected final float spawnLockTime;
   protected final String nameTranslationKey;
   protected boolean backingAway;

   public Role(@Nonnull BuilderRole builder, @Nonnull BuilderSupport builderSupport) {
      NPCEntity npcComponent = builderSupport.getEntity();
      this.combatSupport = new CombatSupport(npcComponent, builder, builderSupport);
      this.stateSupport = new StateSupport(builder, builderSupport);
      this.markedEntitySupport = new MarkedEntitySupport(npcComponent);
      this.worldSupport = new WorldSupport(npcComponent, builder, builderSupport);
      this.entitySupport = new EntitySupport(npcComponent, builder);
      this.positionCache = new PositionCache(this);
      this.debugSupport = new DebugSupport(npcComponent, builder);
      this.initialMaxHealth = builder.getMaxHealth(builderSupport);
      this.nameTranslationKey = builder.getNameTranslationKey(builderSupport);
      this.appearance = builder.getAppearance(builderSupport);
      this.hotbarItems = builder.getHotbarItems(builderSupport);
      this.offHandItems = builder.getOffHandItems(builderSupport);
      this.defaultOffHandSlot = builder.getDefaultOffHandSlot(builderSupport);
      this.inventoryContentsDropList = builder.getInventoryItemsDropList(builderSupport);
      this.armor = builder.getArmor();
      this.inertia = builder.getInertia();

      for (MotionController motionController : this.motionControllers.values()) {
         motionController.setInertia(this.inertia);
      }

      this.knockbackScale = builder.getKnockbackScale(builderSupport);

      for (MotionController motionController : this.motionControllers.values()) {
         motionController.setKnockbackScale(this.knockbackScale);
      }

      this.positionCache.setOpaqueBlockSet(builder.getOpaqueBlockSet());
      this.dropListId = builder.getDropListId(builderSupport);
      this.isAvoidingEntities = builder.isAvoidingEntities();
      this.avoidanceMode = builder.getAvoidanceMode();
      this.collisionProbeDistance = builder.getCollisionDistance();
      this.collisionForceFalloff = builder.getCollisionForceFalloff();
      this.collisionRadius = builder.getCollisionRadius();
      this.collisionViewAngle = builder.getCollisionViewAngle();
      this.collisionViewHalfAngleCosine = TrigMathUtil.cos(this.collisionViewAngle / 2.0F);
      this.separationDistance = builder.getSeparationDistance(builderSupport);
      this.separationWeight = builder.getSeparationWeight(builderSupport);
      this.separationDistanceTarget = builder.getSeparationDistanceTarget(builderSupport);
      this.separationNearRadiusTarget = builder.getSeparationNearRadiusTarget(builderSupport);
      this.separationFarRadiusTarget = builder.getSeparationFarRadiusTarget(builderSupport);
      this.applySeparation = builder.isApplySeparation(builderSupport);
      if (builder.isOverridingHeadPitchAngle(builderSupport)) {
         this.headPitchAngleRange = builder.getHeadPitchAngleRange(builderSupport);
      } else {
         this.headPitchAngleRange = null;
      }

      this.stayInEnvironment = builder.isStayingInEnvironment();
      this.allowedEnvironments = builder.getAllowedEnvironments();
      this.entityAvoidanceStrength = builder.getEntityAvoidanceStrength();
      this.flockSpawnTypes = builder.getFlockSpawnTypes(builderSupport);
      this.flockSpawnTypesRandom = builder.isFlockSpawnTypeRandom(builderSupport);
      this.flockAllowedRoles = builder.getFlockAllowedRoles(builderSupport);
      this.canLeadFlock = builder.isCanLeadFlock(builderSupport);
      this.flockWeightAlignment = builder.getFlockWeightAlignment();
      this.flockWeightSeparation = builder.getFlockWeightSeparation();
      this.flockWeightCohesion = builder.getFlockWeightCohesion();
      this.flockInfluenceRange = builder.getFlockInfluenceRange();
      this.invulnerable = builder.isInvulnerable(builderSupport);
      this.breathesInAir = builder.isBreathesInAir(builderSupport);
      this.breathesInWater = builder.isBreathesInWater(builderSupport);
      this.pickupDropOnDeath = builder.isPickupDropOnDeath();
      this.deathAnimationTime = builder.getDeathAnimationTime();
      this.deathInteraction = builder.getDeathInteraction(builderSupport);
      this.despawnAnimationTime = builder.getDespawnAnimationTime();
      this.inventorySlots = builder.getInventorySlots();
      this.hotbarSlots = builder.getHotbarSlots();
      this.offHandSlots = builder.getOffHandSlots();
      this.corpseStaysInFlock = builder.isCorpseStaysInFlock();
      this.roleStats = builderSupport.getRoleStats();
      this.balanceAsset = builder.getBalanceAsset(builderSupport);
      this.interactionVars = builder.getInteractionVars(builderSupport);
      this.isMemory = builder.isMemory(builderSupport.getExecutionContext());
      this.memoriesNameOverride = builder.getMemoriesNameOverride(builderSupport.getExecutionContext());
      this.isMemoriesNameOverriden = this.memoriesNameOverride != null && !this.memoriesNameOverride.isEmpty();
      this.spawnLockTime = builder.getSpawnLockTime(builderSupport);
      this.entitySupport.pickRandomDisplayName(builderSupport.getHolder(), false);
      List<Instruction> instructionList = builder.getInstructionList(builderSupport);
      if (instructionList == null) {
         instructionList = new ObjectArrayList();
      }

      Instruction[] instructions = instructionList.toArray(Instruction[]::new);
      this.rootInstruction = Instruction.createRootInstruction(instructions, builderSupport);
      this.interactionInstruction = builder.getInteractionInstruction(builderSupport);
      this.deathInstruction = builder.getDeathInstruction(builderSupport);
      builder.registerStateEvaluator(builderSupport);
      this.setMotionControllers(builderSupport.getEntity(), builder.getMotionControllerMap(builderSupport), builder.getInitialMotionController(builderSupport));
      if (this.interactionInstruction != null) {
         builderSupport.trackInteractions();
      }
   }

   public int getInitialMaxHealth() {
      return this.initialMaxHealth;
   }

   public boolean isAvoidingEntities() {
      return this.isAvoidingEntities;
   }

   public double getCollisionProbeDistance() {
      return this.collisionProbeDistance;
   }

   public boolean isApplySeparation() {
      return this.applySeparation;
   }

   public double getSeparationDistance() {
      return this.separationDistance;
   }

   public Instruction getRootInstruction() {
      return this.rootInstruction;
   }

   @Nullable
   public Instruction getInteractionInstruction() {
      return this.interactionInstruction;
   }

   @Nullable
   public Instruction getDeathInstruction() {
      return this.deathInstruction;
   }

   @Nonnull
   public Steering getBodySteering() {
      return this.bodySteering;
   }

   @Nonnull
   public Steering getHeadSteering() {
      return this.headSteering;
   }

   @Nonnull
   public Set<Ref<EntityStore>> getIgnoredEntitiesForAvoidance() {
      return this.ignoredEntitiesForAvoidance;
   }

   public String getDropListId() {
      return this.dropListId;
   }

   @Nullable
   public String getBalanceAsset() {
      return this.balanceAsset;
   }

   @Nullable
   public Map<String, String> getInteractionVars() {
      return this.interactionVars;
   }

   public boolean isMemory() {
      return this.isMemory;
   }

   public String getMemoriesNameOverride() {
      return this.memoriesNameOverride;
   }

   public String getNameTranslationKey() {
      return this.nameTranslationKey;
   }

   public boolean isMemoriesNameOverriden() {
      return this.isMemoriesNameOverriden;
   }

   public float getSpawnLockTime() {
      return this.spawnLockTime;
   }

   public void postRoleBuilt(@Nonnull BuilderSupport builderSupport) {
      this.requiresLeashPosition = builderSupport.requiresLeashPosition();
      this.flags = builderSupport.allocateFlags();
      this.indexedInstructions = builderSupport.getInstructionSlotMappings();
      this.stateSupport.postRoleBuilt(builderSupport);
      this.worldSupport.postRoleBuilt(builderSupport);
      this.entitySupport.postRoleBuilt(builderSupport);
      this.markedEntitySupport.postRoleBuilder(builderSupport);
      this.rootInstruction.setContext(this, 0);
   }

   public void loaded() {
      this.rootInstruction.loaded(this);
      if (this.interactionInstruction != null) {
         this.interactionInstruction.loaded(this);
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.loaded(this);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.loaded(this);
      }
   }

   public void spawned(@Nonnull Holder<EntityStore> holder, @Nonnull NPCEntity npcComponent) {
      MotionController activeMotionController = this.getActiveMotionController();
      if (activeMotionController != null) {
         activeMotionController.spawned();
      }

      this.entitySupport.pickRandomDisplayName(holder, true);
      this.rootInstruction.spawned(this);
      if (this.interactionInstruction != null) {
         this.interactionInstruction.spawned(this);
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.spawned(this);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.spawned(this);
      }

      this.initialiseInventories(npcComponent);
   }

   public void unloaded() {
      this.worldSupport.unloaded();
      this.markedEntitySupport.unloaded();
      this.deferredActions.clear();
      this.rootInstruction.unloaded(this);
      if (this.interactionInstruction != null) {
         this.interactionInstruction.unloaded(this);
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.unloaded(this);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.unloaded(this);
      }
   }

   public void removed() {
      this.worldSupport.resetAllBlockSensors();
      this.rootInstruction.removed(this);
      if (this.interactionInstruction != null) {
         this.interactionInstruction.removed(this);
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.removed(this);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.removed(this);
      }
   }

   public void teleported(@Nonnull World from, @Nonnull World to) {
      this.rootInstruction.teleported(this, from, to);
      if (this.interactionInstruction != null) {
         this.interactionInstruction.teleported(this, from, to);
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.teleported(this, from, to);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.teleported(this, from, to);
      }
   }

   public String getAppearanceName() {
      return this.appearance;
   }

   public MotionController getActiveMotionController() {
      return this.activeMotionController;
   }

   @Nonnull
   public CombatSupport getCombatSupport() {
      return this.combatSupport;
   }

   @Nonnull
   public StateSupport getStateSupport() {
      return this.stateSupport;
   }

   @Nonnull
   public WorldSupport getWorldSupport() {
      return this.worldSupport;
   }

   @Nonnull
   public MarkedEntitySupport getMarkedEntitySupport() {
      return this.markedEntitySupport;
   }

   @Nonnull
   public PositionCache getPositionCache() {
      return this.positionCache;
   }

   @Nonnull
   public EntitySupport getEntitySupport() {
      return this.entitySupport;
   }

   @Nonnull
   public DebugSupport getDebugSupport() {
      return this.debugSupport;
   }

   public boolean isRoleChangeRequested() {
      return this.roleChangeRequested;
   }

   public void setRoleChangeRequested() {
      this.roleChangeRequested = true;
   }

   public boolean setActiveMotionController(
      @Nullable Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, @Nonnull String name, @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      MotionController motionController = this.motionControllers.get(name);
      if (motionController == null) {
         NPCPlugin.get()
            .getLogger()
            .at(Level.SEVERE)
            .log("Failed to set MotionController for NPC of type '%s': MotionController '%s' not found! ", this.getRoleName(), name);
         return false;
      } else {
         this.setActiveMotionController(ref, npcComponent, motionController, componentAccessor);
         return true;
      }
   }

   public void setActiveMotionController(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      @Nonnull MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.activeMotionController != motionController) {
         if (this.activeMotionController != null) {
            this.activeMotionController.deactivate();
         }

         this.activeMotionController = motionController;
         this.activeMotionController.activate();
         this.motionControllerChanged(ref, npcComponent, this.activeMotionController, componentAccessor);
      }
   }

   protected void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      @Nullable MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.rootInstruction.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      if (this.deathInstruction != null) {
         this.deathInstruction.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

      if (this.interactionInstruction != null) {
         this.interactionInstruction.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

      StateTransitionController stateTransitions = this.stateSupport.getStateTransitionController();
      if (stateTransitions != null) {
         stateTransitions.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   public void setMotionControllers(
      @Nonnull NPCEntity npcComponent, @Nonnull Map<String, MotionController> motionControllers, @Nullable String initialMotionController
   ) {
      this.motionControllers = motionControllers;

      for (Entry<String, MotionController> entry : this.motionControllers.entrySet()) {
         this.debugSupport.registerDebugFlagsListener(entry.getValue());
      }

      this.updateMotionControllers(null, null, null, null);
      if (!this.motionControllers.isEmpty()) {
         if (initialMotionController != null && this.setActiveMotionController(null, npcComponent, initialMotionController, null)) {
            return;
         }

         this.setActiveMotionController(null, npcComponent, RandomExtra.randomElement(new ObjectArrayList(motionControllers.values())), null);
      }
   }

   public void updateMotionControllers(
      @Nullable Ref<EntityStore> ref, @Nullable Model model, @Nullable Box boundingBox, @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (MotionController motionController : this.motionControllers.values()) {
         motionController.setRole(this);
         motionController.setInertia(this.inertia);
         motionController.setKnockbackScale(this.knockbackScale);
         motionController.setHeadPitchAngleRange(this.headPitchAngleRange);
         if (boundingBox != null && model != null) {
            motionController.updateModelParameters(ref, model, boundingBox, componentAccessor);
            motionController.updatePhysicsValues(model.getPhysicsValues());
         }
      }
   }

   public void updateMovementState(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      @Nonnull Vector3d velocity,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.activeMotionController != null) {
         this.activeMotionController.updateMovementState(ref, movementStates, this.bodySteering, velocity, componentAccessor);
      }
   }

   public void tick(@Nonnull Ref<EntityStore> ref, float tickTime, @Nonnull Store<EntityStore> store) {
      int i = 0;

      while (i < this.deferredActions.size()) {
         Role.DeferredAction action = this.deferredActions.get(i);
         if (action.tick(ref, this, tickTime, store)) {
            this.deferredActions.remove(i);
         } else {
            i++;
         }
      }

      this.computeActionsAndSteering(ref, tickTime, this.bodySteering, this.headSteering, store);
   }

   public void addDeferredAction(@Nonnull Role.DeferredAction handler) {
      this.deferredActions.add(handler);
   }

   protected void computeActionsAndSteering(
      @Nonnull Ref<EntityStore> ref, double tickTime, @Nonnull Steering bodySteering, @Nonnull Steering headSteering, @Nonnull Store<EntityStore> store
   ) {
      if (this.debugSupport.isVisSensorRanges()) {
         this.debugSupport.beginSensorVisualization();
      }

      boolean isDead = store.getArchetype(ref).contains(DeathComponent.getComponentType());
      if (isDead) {
         if (this.deathInstruction != null) {
            this.deathInstruction.execute(ref, this, tickTime, store);
         }
      } else {
         if (this.interactionInstruction != null) {
            this.positionCache.forEachPlayer((d, _playerRef, _this, _selfRef, _store) -> {
               _this.stateSupport.setInteractionIterationTarget(_playerRef);
               _this.interactionInstruction.execute(_selfRef, _this, d, _store);
            }, this, ref, store, tickTime, store);
            this.stateSupport.setInteractionIterationTarget(null);
            this.entitySupport.clearTargetPlayerActiveTasks();
         }

         this.getActiveMotionController().beforeInstructionSensorsAndActions(tickTime);
         this.entitySupport.clearNextBodyMotionStep();
         this.entitySupport.clearNextHeadMotionStep();
         if (!this.stateSupport.runTransitionActions(ref, this, tickTime, store)) {
            this.rootInstruction.execute(ref, this, tickTime, store);
         }

         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         if (!npcComponent.isPlayingDespawnAnim()) {
            this.getActiveMotionController().beforeInstructionMotion(tickTime);
            Instruction nextBodyMotionStep = this.entitySupport.getNextBodyMotionStep();
            if (nextBodyMotionStep != this.lastBodyMotionStep) {
               if (this.lastBodyMotionStep != null) {
                  this.lastBodyMotionStep.getBodyMotion().deactivate(ref, this, store);
                  this.lastBodyMotionStep.onEndMotion();
               }

               if (nextBodyMotionStep != null) {
                  nextBodyMotionStep.getBodyMotion().activate(ref, this, store);
               }
            }

            this.lastBodyMotionStep = nextBodyMotionStep;
            Instruction nextHeadMotionStep = this.entitySupport.getNextHeadMotionStep();
            if (nextHeadMotionStep != this.lastHeadMotionStep) {
               if (this.lastHeadMotionStep != null) {
                  this.lastHeadMotionStep.getHeadMotion().deactivate(ref, this, store);
                  this.lastHeadMotionStep.onEndMotion();
               }

               if (nextHeadMotionStep != null) {
                  nextHeadMotionStep.getHeadMotion().activate(ref, this, store);
               }
            }

            this.lastHeadMotionStep = nextHeadMotionStep;
            if (nextBodyMotionStep != null) {
               nextBodyMotionStep.getBodyMotion().computeSteering(ref, this, nextBodyMotionStep.getSensor().getSensorInfo(), tickTime, bodySteering, store);
            }

            if (nextHeadMotionStep != null) {
               nextHeadMotionStep.getHeadMotion().computeSteering(ref, this, nextHeadMotionStep.getSensor().getSensorInfo(), tickTime, headSteering, store);
            }
         }
      }
   }

   public void blendSeparation(
      @Nonnull Ref<EntityStore> selfRef,
      @Nonnull Vector3d position,
      @Nonnull Steering steering,
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      this.lastSeparationSteering.assign(Vector3d.ZERO);
      double maxRange = this.separationDistance;
      Ref<EntityStore> targetRef = this.markedEntitySupport.getTargetReferenceToIgnoreForAvoidance();
      if (targetRef != null && targetRef.isValid()) {
         TransformComponent targetTransformComponent = commandBuffer.getComponent(targetRef, transformComponentType);

         assert targetTransformComponent != null;

         double distance = targetTransformComponent.getPosition().distanceSquaredTo(position);
         if (distance <= this.separationNearRadiusTarget * this.separationNearRadiusTarget) {
            maxRange = this.separationDistanceTarget;
         } else if (distance < this.separationFarRadiusTarget * this.separationFarRadiusTarget) {
            double s = (Math.sqrt(distance) - this.separationNearRadiusTarget) / (this.separationFarRadiusTarget - this.separationNearRadiusTarget);
            maxRange = NPCPhysicsMath.lerp(this.separationDistanceTarget, this.separationDistance, s);
         }
      }

      this.groupSteeringAccumulator.setComponentSelector(this.activeMotionController.getComponentSelector());
      this.groupSteeringAccumulator.setMaxRange(maxRange);
      this.groupSteeringAccumulator.setViewConeHalfAngleCosine(this.collisionViewHalfAngleCosine);
      this.groupSteeringAccumulator.begin(selfRef, commandBuffer);
      this.positionCache
         .forEachEntityInAvoidanceRange(
            this.ignoredEntitiesForAvoidance,
            (ref, _groupSteeringAccumulator, _role, _buffer) -> _groupSteeringAccumulator.processEntity(ref, this.separationWeight, 1.0, 1.0, _buffer),
            this.groupSteeringAccumulator,
            this,
            commandBuffer
         );
      this.groupSteeringAccumulator.end();
      if (this.groupSteeringAccumulator.getCount() > 0) {
         Vector3d sumOfDistances = this.groupSteeringAccumulator.getSumOfDistances();
         if (sumOfDistances.squaredLength() > 1.0000000000000002E-10) {
            double speed = steering.getSpeed();
            this.separation.assign(sumOfDistances).setLength(-0.5);
            this.lastSeparationSteering.assign(this.separation);
            if (speed > 0.0) {
               this.separation.add(steering.getTranslation());
               this.separation.setLength(speed);
            }

            steering.setTranslation(this.separation);
         }
      }
   }

   @Nonnull
   public Vector3d getLastSeparationSteering() {
      return this.lastSeparationSteering;
   }

   public void blendAvoidance(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull Steering steering, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      this.steeringForceAvoidCollision.setDebug(this.debugSupport.isDebugRoleSteering());
      this.steeringForceAvoidCollision.setAvoidanceMode(this.getAvoidanceMode());
      this.steeringForceAvoidCollision.setSelf(ref, position, commandBuffer);
      if (!this.activeMotionController.estimateVelocity(steering, this.steeringForceAvoidCollision.getSelfVelocity())) {
         this.steeringForceAvoidCollision.setVelocityFromEntity(ref, commandBuffer);
      }

      if (this.collisionRadius >= 0.0) {
         this.steeringForceAvoidCollision.setSelfRadius(this.collisionRadius);
      }

      this.steeringForceAvoidCollision.setMaxDistance(this.collisionProbeDistance);
      this.steeringForceAvoidCollision.setFalloff(this.collisionForceFalloff);
      this.steeringForceAvoidCollision.setComponentSelector(this.activeMotionController.getComponentSelector());
      this.steeringForceAvoidCollision.reset();
      this.positionCache
         .forEachEntityInAvoidanceRange(
            this.ignoredEntitiesForAvoidance,
            (_ref, _steeringForceAvoidCollision, _buffer) -> _steeringForceAvoidCollision.add(_ref, _buffer),
            this.steeringForceAvoidCollision,
            commandBuffer
         );
      this.steeringForceAvoidCollision.compute(steering);
   }

   @Nonnull
   public Vector3d getLastAvoidanceSteering() {
      return this.steeringForceAvoidCollision.getLastSteeringDirection();
   }

   public void resetInstruction(int instruction) {
      this.indexedInstructions[instruction].reset();
   }

   public String getRoleName() {
      return this.roleName;
   }

   public int getRoleIndex() {
      return this.roleIndex;
   }

   public void setRoleIndex(int roleIndex, @Nonnull String roleName) {
      this.roleIndex = roleIndex;
      this.roleName = roleName;
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public boolean isBreathesInAir() {
      return this.breathesInAir;
   }

   public boolean isBreathesInWater() {
      return this.breathesInWater;
   }

   public double getInertia() {
      return this.inertia;
   }

   public double getKnockbackScale() {
      return this.knockbackScale;
   }

   public boolean canBreathe(@Nonnull BlockMaterial breathingMaterial, int fluidId) {
      return this.isInvulnerable() ? true : this.couldBreathe(breathingMaterial, fluidId);
   }

   public boolean couldBreathe(@Nonnull BlockMaterial breathingMaterial, int fluidId) {
      if (fluidId != 0) {
         return this.breathesInWater;
      } else {
         return breathingMaterial == BlockMaterial.Empty ? this.breathesInAir : false;
      }
   }

   public boolean couldBreatheCached() {
      return this.positionCache.couldBreatheCached();
   }

   public void addForce(@Nonnull Vector3d velocity, @Nullable VelocityConfig velocityConfig) {
      if (this.activeMotionController != null) {
         this.activeMotionController.addForce(velocity, velocityConfig);
      }
   }

   public void forceVelocity(@Nonnull Vector3d velocity, @Nullable VelocityConfig velocityConfig, boolean ignoreDamping) {
      if (this.activeMotionController != null) {
         this.activeMotionController.forceVelocity(velocity, velocityConfig, ignoreDamping);
      }
   }

   public void processAddVelocityInstruction(@Nonnull Vector3d velocity, @Nullable VelocityConfig velocityConfig) {
      if (this.activeMotionController != null) {
         this.activeMotionController.addForce(velocity, velocityConfig);
      }
   }

   public void processSetVelocityInstruction(@Nonnull Vector3d velocity, @Nullable VelocityConfig velocityConfig) {
      if (this.activeMotionController != null) {
         this.activeMotionController.forceVelocity(Vector3d.ZERO, null, false);
         this.activeMotionController.addForce(velocity, velocityConfig);
      }
   }

   public boolean isOnGround() {
      return this.getActiveMotionController() != null && this.getActiveMotionController().onGround();
   }

   public void setArmor(@Nonnull NPCEntity npcComponent, @Nullable String[] armor) {
      this.armor = armor;
      if (armor != null) {
         for (String s : armor) {
            RoleUtils.setArmor(npcComponent, s);
         }
      }
   }

   public boolean isPickupDropOnDeath() {
      return this.pickupDropOnDeath;
   }

   public boolean requiresLeashPosition() {
      return this.requiresLeashPosition;
   }

   public void clearOnce() {
      this.rootInstruction.clearOnce();
      if (this.interactionInstruction != null) {
         this.interactionInstruction.clearOnce();
      }

      if (this.deathInstruction != null) {
         this.deathInstruction.clearOnce();
      }

      this.stateSupport.pollNeedClearOnce();
   }

   public void clearOnceIfNeeded() {
      if (this.stateSupport.pollNeedClearOnce()) {
         this.clearOnce();
         this.stateSupport.resetLocalStateMachines();
      }
   }

   public void setMarkedTarget(@Nonnull String targetSlot, @Nonnull Ref<EntityStore> target) {
      this.markedEntitySupport.setMarkedEntity(targetSlot, target);
   }

   public boolean isFriendly(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return !this.combatSupport.getCanCauseDamage(ref, componentAccessor);
   }

   public boolean isIgnoredForAvoidance(@Nonnull Ref<EntityStore> entityReference) {
      return this.ignoredEntitiesForAvoidance.contains(entityReference);
   }

   public Role.AvoidanceMode getAvoidanceMode() {
      return this.avoidanceMode;
   }

   public double getCollisionRadius() {
      return this.collisionRadius;
   }

   public int[] getFlockSpawnTypes() {
      if (this.flockSpawnTypeIndices != null) {
         return this.flockSpawnTypeIndices;
      } else {
         int length = this.flockSpawnTypes == null ? 0 : this.flockSpawnTypes.length;
         this.flockSpawnTypeIndices = new int[length];

         for (int i = 0; i < length; i++) {
            int index = NPCPlugin.get().getIndex(this.flockSpawnTypes[i]);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalStateException(String.format("Role %s contains unknown FlockSpawnTypes NPC %s", this.roleName, this.flockSpawnTypes[i]));
            }

            this.flockSpawnTypeIndices[i] = index;
         }

         return this.flockSpawnTypeIndices;
      }
   }

   @Nonnull
   public String[] getFlockAllowedRoles() {
      return this.flockAllowedRoles != null ? Arrays.copyOf(this.flockAllowedRoles, this.flockAllowedRoles.length) : ArrayUtil.EMPTY_STRING_ARRAY;
   }

   public boolean isFlockSpawnTypesRandom() {
      return this.flockSpawnTypesRandom;
   }

   public boolean isCanLeadFlock() {
      return this.canLeadFlock;
   }

   public double getFlockInfluenceRange() {
      return this.flockInfluenceRange;
   }

   public double getDeathAnimationTime() {
      return this.deathAnimationTime;
   }

   @Nullable
   public String getDeathInteraction() {
      return this.deathInteraction;
   }

   public float getDespawnAnimationTime() {
      return this.despawnAnimationTime;
   }

   public void setReachedTerminalAction(boolean hasReached) {
      this.hasReachedTerminalAction = hasReached;
   }

   public boolean hasReachedTerminalAction() {
      return this.hasReachedTerminalAction;
   }

   public void setFlag(int index, boolean value) {
      if (this.flags == null) {
         throw new NullPointerException(String.format("Trying to set a flag in role %s but flags are null", this.getRoleName()));
      } else if (index >= 0 && index < this.flags.length) {
         this.flags[index] = value;
      } else {
         throw new IllegalArgumentException(
            String.format("Flag value cannot be less than 0 and must be less than array length %s. Value was %s", this.flags.length, index)
         );
      }
   }

   public boolean isFlagSet(int index) {
      return this.flags != null && index >= 0 && index < this.flags.length ? this.flags[index] : false;
   }

   public boolean isBackingAway() {
      return this.backingAway;
   }

   public void setBackingAway(boolean backingAway) {
      this.backingAway = backingAway;
   }

   public Instruction swapTreeModeSteps(Instruction newStep) {
      Instruction old = this.currentTreeModeStep;
      this.currentTreeModeStep = newStep;
      return old;
   }

   public void notifySensorMatch() {
      if (this.currentTreeModeStep != null) {
         this.currentTreeModeStep.notifyChildSensorMatch();
      }
   }

   public void resetAllInstructions() {
      for (Instruction instruction : this.indexedInstructions) {
         instruction.reset();
      }
   }

   @Nullable
   public String getSteeringMotionName() {
      if (this.lastBodyMotionStep == null) {
         return null;
      } else {
         BodyMotion motion = this.lastBodyMotionStep.getBodyMotion();
         if (motion != null) {
            motion = motion.getSteeringMotion();
         }

         return motion == null ? null : motion.getClass().getSimpleName();
      }
   }

   @Override
   public int componentCount() {
      return 1;
   }

   @Override
   public IAnnotatedComponent getComponent(int index) {
      return this.rootInstruction;
   }

   @Override
   public void getInfo(Role role, ComponentInfo holder) {
   }

   @Override
   public int getIndex() {
      throw new UnsupportedOperationException("Roles do not have component indexes!");
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      throw new UnsupportedOperationException("Roles do not have parent contexts!");
   }

   @Nullable
   @Override
   public IAnnotatedComponent getParent() {
      return null;
   }

   @Override
   public String getLabel() {
      return this.roleName;
   }

   private void initialiseInventories(@Nonnull NPCEntity npcComponent) {
      List<ItemStack> inventoryItems = null;
      if (this.inventoryContentsDropList != null) {
         ItemModule itemModule = ItemModule.get();
         if (itemModule.isEnabled()) {
            inventoryItems = itemModule.getRandomItemDrops(this.inventoryContentsDropList);
         }
      }

      int inventorySlots = inventoryItems != null && inventoryItems.size() > this.inventorySlots ? inventoryItems.size() : this.inventorySlots;
      if (inventorySlots > 0 || this.hotbarSlots > 3 || this.offHandSlots > 0) {
         npcComponent.setInventorySize(this.hotbarSlots, inventorySlots, this.offHandSlots);
      }

      if (inventoryItems != null) {
         ItemContainer inventory = npcComponent.getInventory().getStorage();

         for (ItemStack item : inventoryItems) {
            inventory.addItemStack(item);
         }
      }

      if (this.hotbarItems != null && this.hotbarItems.length > 0 && npcComponent.getInventory().getHotbar().isEmpty()) {
         Inventory inventory = npcComponent.getInventory();
         ItemContainer hotbar = inventory.getHotbar();

         for (byte i = 0; i < this.hotbarItems.length; i++) {
            if (this.hotbarItems[i] != null) {
               if (this.hotbarItems[i].startsWith("Droplist:")) {
                  if (!InventoryHelper.checkHotbarSlot(inventory, i)) {
                     continue;
                  }

                  List<ItemStack> items = ItemModule.get().getRandomItemDrops(this.hotbarItems[i].substring("Droplist:".length()));
                  hotbar.setItemStackForSlot(i, items.get(RandomExtra.randomRange(items.size())));
               }

               InventoryHelper.setHotbarItem(inventory, this.hotbarItems[i], i);
            }
         }
      }

      if (this.offHandItems != null && this.offHandItems.length > 0) {
         RoleUtils.setOffHandItems(npcComponent, this.offHandItems);
      }

      if (this.defaultOffHandSlot >= 0) {
         InventoryHelper.setOffHandSlot(npcComponent.getInventory(), this.defaultOffHandSlot);
      }

      this.setArmor(npcComponent, this.armor);
   }

   public boolean isCorpseStaysInFlock() {
      return this.corpseStaysInFlock;
   }

   public void onLoadFromWorldGenOrPrefab(
      @Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.entitySupport.pickRandomDisplayName(ref, true, componentAccessor);
      this.initialiseInventories(npcComponent);
   }

   public RoleStats getRoleStats() {
      return this.roleStats;
   }

   public static enum AvoidanceMode implements Supplier<String> {
      Slowdown("Only slow down NPC"),
      Evade("Only evade"),
      Any("Any avoidance allowed");

      @Nonnull
      private final String description;

      private AvoidanceMode(@Nonnull final String description) {
         this.description = description;
      }

      @Nonnull
      public String get() {
         return this.description;
      }
   }

   @FunctionalInterface
   public interface DeferredAction {
      boolean tick(@Nonnull Ref<EntityStore> var1, @Nonnull Role var2, double var3, @Nonnull Store<EntityStore> var5);
   }
}
