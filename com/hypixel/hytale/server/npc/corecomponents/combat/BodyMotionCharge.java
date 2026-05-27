package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.function.predicate.QuadPredicate;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderBodyMotionCharge;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.BlockHit;
import com.hypixel.hytale.server.npc.movement.controllers.EntityHit;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.movement.controllers.RailPath;
import com.hypixel.hytale.server.npc.movement.controllers.RailPathSmoother;
import com.hypixel.hytale.server.npc.movement.controllers.RailStepConfig;
import com.hypixel.hytale.server.npc.movement.controllers.RailStepResult;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.AimingData;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap.Entry;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public class BodyMotionCharge extends BodyMotionBase implements DebugSupport.DebugFlagsChangeListener {
   private static final InteractionType INTERACTION_TYPE = InteractionType.Collision;
   private static final float CHARGE_DEBUG_SHAPE_DURATION_SECONDS = 5.0F;
   private static final double CHARGE_DEBUG_SPHERE_RADIUS = 0.2;
   private static final double CHARGE_DEBUG_CUBE_SIZE = 0.2;
   private static final int DEFAULT_COLLISION_BUFFER_CAPACITY = 8;
   private static final double MAX_RELATIVE_SPEED_CHARGE_SEARCH_RADIUS = 64.0;
   private static final double CHARGE_CANDIDATE_APPROACH_PADDING = 2.0;
   private static final double CHARGE_SEARCH_RADIUS = 14.8F;
   private static final QuadPredicate<Ref<EntityStore>, Ref<EntityStore>, Object, ComponentAccessor<EntityStore>> NOT_SELF_PREDICATE = (ref, self, ignored, accessor) -> !ref.equals(
      self
   );
   private static final QuadConsumer<Ref<EntityStore>, Ref<EntityStore>, List<Ref<EntityStore>>, ComponentAccessor<EntityStore>> COLLECT_ENTITY_CONSUMER = (ref, self, buffer, accessor) -> {
      if (ref != null && ref.isValid() && ref != self) {
         buffer.add(ref);
      }
   };
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final double relativeTurnSpeed;
   protected final double lockedOnHalfAngleRad;
   protected final double[] lockedOnDurationRange;
   protected final double[] windingUpDurationRange;
   protected final double[] postChargeDurationRange;
   protected final boolean skipLockedOnState;
   protected final boolean skipWindingUpState;
   protected final double[] chargeDistanceRange;
   protected final boolean windingUpUninterruptable;
   protected final double windingUpRelativeTurnSpeed;
   protected final double chargeRelativeSpeed;
   protected final int ignoredBlockSet;
   @Nullable
   private final Predicate<CollisionConfig> ignoredBlockFilter;
   protected final boolean clearOnceOnStateChange;
   protected final double chargeAbsoluteSpeed;
   protected final double chargeAcceleration;
   protected final boolean ignoredBlockSetTriggers;
   protected final boolean entityStopsCharge;
   protected final double climbSlope;
   protected final double dropSlope;
   protected final double horizontalSkipGapWidth;
   @Nullable
   private final String blockCollisionInteractionId;
   @Nullable
   private final String npcCollisionInteractionId;
   @Nullable
   private final String playerCollisionInteractionId;
   protected final double repeatCollisionIgnoreDuration;
   @Nullable
   private Instruction parentInstruction;
   protected final AimingData aimingData = new AimingData();
   protected final Vector3d targetPosition = new Vector3d();
   protected final Rotation3f rotation = new Rotation3f();
   protected final ProbeMoveData probeMoveData = new ProbeMoveData();
   protected final Vector3d chargeStartPosition = new Vector3d();
   protected final Vector3d chargeEndPosition = new Vector3d();
   protected final Vector3d chargeDirection = new Vector3d();
   private final RailPath railPath = new RailPath();
   private final RailPathSmoother railPathSmoother = new RailPathSmoother();
   private final RailPathSmoother.Config smootherConfig = new RailPathSmoother.Config();
   private final RailStepConfig railConfig = new RailStepConfig();
   private final RailStepResult railResult = new RailStepResult();
   private double chargeSpeed;
   private final Vector3d railDelta = new Vector3d();
   private final Vector3d railStepTargetPosition = new Vector3d();
   private final List<Ref<EntityStore>> candidateEntitiesBuffer = new ObjectArrayList(8);
   private final List<EntityHit> filteredEntityHits = new ObjectArrayList(8);
   private final BitSet acceptedEntityHitIndexes = new BitSet(8);
   private final Reference2DoubleMap<Ref<EntityStore>> lastEntityCollisionHitTimes = new Reference2DoubleOpenHashMap(8);
   private double collisionClockSeconds;
   private BodyMotionCharge.ChargeState state = BodyMotionCharge.ChargeState.LostTarget;
   private BodyMotionCharge.ChargeState sensorVisibleState = BodyMotionCharge.ChargeState.LostTarget;
   private boolean debugChargeState;
   private boolean debugChargePath;
   private boolean visChargePath;
   private boolean visChargeCollisions;
   private boolean visChargeEntityHits;
   private double phaseElapsed;
   private double activePhaseDuration;
   private double activeChargeDistance;

   public BodyMotionCharge(@Nonnull BuilderBodyMotionCharge builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.relativeTurnSpeed = builder.getRelativeTurnSpeed(support);
      this.lockedOnHalfAngleRad = builder.getLockedOnHalfAngleRadians();
      this.lockedOnDurationRange = builder.getLockedOnDurationRange(support);
      this.windingUpDurationRange = builder.getWindingUpDurationRange(support);
      this.postChargeDurationRange = builder.getPostChargeDurationRange(support);
      this.skipLockedOnState = isZeroDurationRange(this.lockedOnDurationRange);
      this.skipWindingUpState = isZeroDurationRange(this.windingUpDurationRange);
      this.chargeDistanceRange = builder.getChargeDistanceRange(support);
      this.windingUpUninterruptable = builder.isWindingUpUninterruptable(support);
      this.windingUpRelativeTurnSpeed = builder.getWindingUpRelativeTurnSpeed(support);
      this.chargeRelativeSpeed = builder.getChargeRelativeSpeed(support);
      this.activeChargeDistance = RandomExtra.randomRange(this.chargeDistanceRange);
      this.clearOnceOnStateChange = builder.isClearOnceOnStateChange(support);
      this.chargeAbsoluteSpeed = builder.getChargeAbsoluteSpeed(support);
      this.chargeAcceleration = builder.getChargeAcceleration(support);
      this.ignoredBlockSetTriggers = builder.isIgnoredBlockSetTriggers(support);
      this.entityStopsCharge = builder.isEntityStopsCharge(support);
      this.climbSlope = builder.getClimbSlope(support);
      this.dropSlope = builder.getDropSlope(support);
      this.horizontalSkipGapWidth = builder.getHorizontalSkipGapWidth(support);
      this.blockCollisionInteractionId = builder.getBlockCollisionInteraction(support);
      this.npcCollisionInteractionId = builder.getNPCCollisionInteraction(support);
      this.playerCollisionInteractionId = builder.getPlayerCollisionInteraction(support);
      this.repeatCollisionIgnoreDuration = builder.getRepeatCollisionIgnoreDuration(support);
      this.ignoredBlockSet = builder.getIgnoredBlockSet();
      if (this.ignoredBlockSet == Integer.MIN_VALUE) {
         this.ignoredBlockFilter = null;
      } else {
         int setIndex = this.ignoredBlockSet;
         this.ignoredBlockFilter = cfg -> cfg.blockType == null || !BlockSetModule.getInstance().blockInSet(setIndex, cfg.blockType);
         this.probeMoveData.setBlockCollisionFilter(this.ignoredBlockFilter);
      }

      this.aimingData.requireCloseCombat();
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.activate(ref, role, componentAccessor);
      this.state = BodyMotionCharge.ChargeState.LostTarget;
      this.sensorVisibleState = BodyMotionCharge.ChargeState.LostTarget;
      this.resetRailState();
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.deactivate(ref, role, componentAccessor);
      this.resetRailState();
   }

   private void resetRailState() {
      this.chargeSpeed = 0.0;
      this.railPath.reset();
      this.railPathSmoother.reset();
      this.smootherConfig.reset();
      this.railResult.reset();
      this.railConfig.candidateEntities = null;
      this.candidateEntitiesBuffer.clear();
      this.filteredEntityHits.clear();
      this.acceptedEntityHitIndexes.clear();
      this.lastEntityCollisionHitTimes.clear();
      this.collisionClockSeconds = 0.0;
   }

   @Override
   public void loaded(Role role) {
      super.loaded(role);
      if (this.clearOnceOnStateChange && this.parent instanceof Instruction instruction && instruction.getParent() instanceof Instruction instructionParent) {
         this.parentInstruction = instructionParent;
      }
   }

   @Override
   public void registerWithSupport(Role role) {
      super.registerWithSupport(role);
      DebugSupport debugSupport = role.getDebugSupport();
      debugSupport.registerDebugFlagsListener(this);
      this.onDebugFlagsChanged(debugSupport.getDebugFlags());
      role.getPositionCache().requirePlayerDistanceUnsorted(14.8F);
      role.getPositionCache().requireEntityDistanceUnsorted(14.8F);
   }

   @Override
   public void preComputeSteering(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, @Nonnull Store<EntityStore> store) {
      if (sensorInfo != null) {
         sensorInfo.passExtraInfo(this.aimingData);
      }
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (sensorInfo != null && sensorInfo.getPositionProvider() != null) {
         this.collisionClockSeconds += dt;
         if (this.state == BodyMotionCharge.ChargeState.LostTarget) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.LostTarget;
            desiredSteering.clear();
            this.clearEntityCollisionHits();
            this.activeChargeDistance = Math.max(1.0, RandomExtra.randomRange(this.chargeDistanceRange));
            this.aimingData.setChargeDistance(this.activeChargeDistance);
            this.transitionChargeState(BodyMotionCharge.ChargeState.Aiming, ref, role, componentAccessor);
         }

         TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         Vector3d selfPosition = transformComponent.getPosition();
         Rotation3f selfRotation = transformComponent.getRotation();
         MotionController motionController = role.getActiveMotionController();
         boolean targetAvailable = sensorInfo.getPositionProvider().providePosition(this.targetPosition);
         double targetDistanceSquared = targetAvailable ? motionController.waypointDistanceSquared(selfPosition, this.targetPosition) : Double.MAX_VALUE;
         boolean haveSolution;
         if (targetDistanceSquared <= this.activeChargeDistance * this.activeChargeDistance) {
            haveSolution = this.aimingData.computeSolution(selfPosition, this.targetPosition, null);
         } else {
            haveSolution = false;
         }

         if (this.state == BodyMotionCharge.ChargeState.Aiming) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.Aiming;
            if (!targetAvailable) {
               this.transitionChargeState(BodyMotionCharge.ChargeState.LostTarget, ref, role, componentAccessor);
               return true;
            } else if (haveSolution) {
               turnTo(desiredSteering, this.aimingData, this.relativeTurnSpeed);
               this.transitionChargeState(BodyMotionCharge.ChargeState.LockedOn, ref, role, componentAccessor);
               return true;
            } else {
               NPCPhysicsMath.rotationFromDirection(selfPosition, this.targetPosition, selfRotation, this.rotation);
               turnTo(desiredSteering, this.rotation, this.relativeTurnSpeed);
               return true;
            }
         } else if (this.state == BodyMotionCharge.ChargeState.LockedOn) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.LockedOn;
            if (targetAvailable && haveSolution) {
               turnTo(desiredSteering, this.aimingData, this.relativeTurnSpeed);
               if (this.aimingData.isOnTarget(selfRotation.yaw(), selfRotation.pitch(), this.lockedOnHalfAngleRad)) {
                  if (this.isPhaseOver(dt)) {
                     this.transitionChargeState(BodyMotionCharge.ChargeState.WindingUp, ref, role, componentAccessor);
                  }

                  return true;
               } else {
                  this.transitionChargeState(BodyMotionCharge.ChargeState.Aiming, ref, role, componentAccessor);
                  return true;
               }
            } else {
               this.transitionChargeState(BodyMotionCharge.ChargeState.LostTarget, ref, role, componentAccessor);
               return true;
            }
         } else if (this.state == BodyMotionCharge.ChargeState.WindingUp) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.WindingUp;
            if (haveSolution) {
               turnTo(desiredSteering, this.aimingData, this.windingUpRelativeTurnSpeed);
               if (this.isPhaseOver(dt)) {
                  this.transitionChargeState(BodyMotionCharge.ChargeState.Launch, ref, role, componentAccessor);
                  return true;
               } else {
                  return true;
               }
            } else if (!this.windingUpUninterruptable) {
               this.transitionChargeState(BodyMotionCharge.ChargeState.LostTarget, ref, role, componentAccessor);
               return true;
            } else {
               if (targetAvailable) {
                  NPCPhysicsMath.rotationFromDirection(selfPosition, this.targetPosition, selfRotation, this.rotation);
                  turnTo(desiredSteering, this.rotation, this.windingUpRelativeTurnSpeed);
               } else {
                  turnTo(desiredSteering, selfRotation, this.windingUpRelativeTurnSpeed);
               }

               if (this.isPhaseOver(dt)) {
                  this.transitionChargeState(BodyMotionCharge.ChargeState.Launch, ref, role, componentAccessor);
               }

               return true;
            }
         } else if (this.state == BodyMotionCharge.ChargeState.Launch) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.Launch;
            PhysicsMath.vectorFromAngles(selfRotation.yaw(), selfRotation.pitch(), this.chargeDirection);
            this.chargeDirection.normalize(this.activeChargeDistance);
            this.chargeStartPosition.set(selfPosition);
            this.chargeEndPosition.set(this.chargeStartPosition).add(this.chargeDirection);
            this.probeMoveData.setSaveSegments(true);
            this.probeCharge(this.chargeStartPosition, this.chargeDirection, motionController, ref, componentAccessor);
            this.smootherConfig.climbSlope = this.climbSlope;
            this.smootherConfig.dropSlope = this.dropSlope;
            this.smootherConfig.horizontalSkipGapWidth = this.horizontalSkipGapWidth;
            this.railPathSmoother.smooth(this.probeMoveData, motionController, this.smootherConfig);
            this.railPath.capture(this.railPathSmoother.getWaypoints(), this.railPathSmoother.getWaypointCount());
            if (this.debugChargePath) {
               Integer entityId = null;
               NetworkId networkId = componentAccessor.getComponent(ref, NetworkId.getComponentType());
               if (networkId != null) {
                  entityId = networkId.getId();
               }

               NPCPlugin.get().getLogger().at(Level.INFO).log("[NPC] entityId=%s %s", entityId, this.probeMoveData.dump());
            }

            this.probeMoveData.setSaveSegments(false);
            this.chargeSpeed = 0.0;
            this.transitionChargeState(BodyMotionCharge.ChargeState.Charging, ref, role, componentAccessor);
            return true;
         } else if (this.state == BodyMotionCharge.ChargeState.Charging) {
            this.sensorVisibleState = BodyMotionCharge.ChargeState.Charging;
            desiredSteering.clear();
            if (motionController.isForcePushed()) {
               this.transitionChargeState(BodyMotionCharge.ChargeState.Obstructed, ref, role, componentAccessor);
               return true;
            } else if (this.railPath.isFinished()) {
               this.transitionChargeState(BodyMotionCharge.ChargeState.Finished, ref, role, componentAccessor);
               return true;
            } else {
               double maxSpeed = this.getEffectiveMaximumChargeSpeed(motionController);
               this.chargeSpeed = Math.min(this.chargeSpeed + this.chargeAcceleration * dt, maxSpeed);
               double step = this.chargeSpeed * dt;
               this.railPath.snapY(selfPosition);
               int oldCursor = this.railPath.getCursor();
               double oldSegmentProgress = this.railPath.getSegmentProgress();
               this.railPath.advance(selfPosition, step, this.railDelta);
               boolean startedNewSegment = this.railPath.getCursor() != oldCursor
                  || oldSegmentProgress <= 1.0E-6 && this.railPath.getSegmentProgress() > 1.0E-6;
               this.renderChargePathDebug(componentAccessor, selfPosition, startedNewSegment);
               this.populateCandidateEntities(ref, role, componentAccessor);
               this.railConfig.ignoredBlockFilter = this.ignoredBlockFilter;
               this.railConfig.ignoredBlocksFireTriggers = this.ignoredBlockSetTriggers;
               this.railConfig.stopOnEntityHit = this.entityStopsCharge;
               this.railConfig.candidateEntities = this.candidateEntitiesBuffer.isEmpty() ? null : this.candidateEntitiesBuffer;

               try {
                  motionController.applyRailStep(ref, role, this.railDelta, this.railConfig, this.railResult, componentAccessor);
                  this.refreshEntityCollisionHits();
               } finally {
                  this.railConfig.candidateEntities = null;
                  this.candidateEntitiesBuffer.clear();
               }

               this.renderChargeCollisionDebug(componentAccessor);
               this.executeBlockCollisionInteraction(ref, componentAccessor);
               this.executeEntityCollisionInteraction(ref, componentAccessor);
               if (this.railResult.obstructed) {
                  this.transitionChargeState(BodyMotionCharge.ChargeState.Obstructed, ref, role, componentAccessor);
                  return true;
               } else if (this.railPath.isFinished()) {
                  this.transitionChargeState(BodyMotionCharge.ChargeState.Finished, ref, role, componentAccessor);
                  return true;
               } else {
                  return true;
               }
            }
         } else if (this.state != BodyMotionCharge.ChargeState.Obstructed && this.state != BodyMotionCharge.ChargeState.Finished) {
            this.sensorVisibleState = this.state;
            return true;
         } else {
            this.sensorVisibleState = this.state;
            desiredSteering.clear();
            this.clearEntityCollisionHits();
            if (this.isPhaseOver(dt)) {
               this.transitionChargeState(BodyMotionCharge.ChargeState.LostTarget, ref, role, componentAccessor);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private void transitionChargeState(
      @Nonnull BodyMotionCharge.ChargeState newState,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (newState != this.state) {
         if (newState == BodyMotionCharge.ChargeState.LockedOn && this.skipLockedOnState) {
            newState = BodyMotionCharge.ChargeState.WindingUp;
         }

         if (newState == BodyMotionCharge.ChargeState.WindingUp && this.skipWindingUpState) {
            newState = BodyMotionCharge.ChargeState.Launch;
         }

         switch (newState) {
            case LockedOn:
               this.initPhaseDuration(this.lockedOnDurationRange);
            case LostTarget:
            case Launch:
            case Charging:
            default:
               break;
            case WindingUp:
               this.initPhaseDuration(this.windingUpDurationRange);
               break;
            case Obstructed:
            case Finished:
               this.initPhaseDuration(this.postChargeDurationRange);
         }

         if (this.parentInstruction != null) {
            this.parentInstruction.clearOnce();
         }

         if (this.debugChargeState) {
            Integer entityId = null;
            NetworkId networkId = componentAccessor.getComponent(ref, NetworkId.getComponentType());
            if (networkId != null) {
               entityId = networkId.getId();
            }

            NPCPlugin.get()
               .getLogger()
               .at(Level.INFO)
               .log("BodyMotionCharge state %s -> %s role=%s entityId=%s", this.state, newState, role.getRoleName(), entityId);
         }

         this.state = newState;
      }
   }

   private void renderChargePathDebug(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Vector3dc selfPosition, boolean startedNewSegment) {
      if (this.visChargePath) {
         if (!(this.railDelta.lengthSquared() <= 1.0E-12)) {
            this.railStepTargetPosition.set(selfPosition).add(this.railDelta);
            Vector3f color = startedNewSegment ? DebugUtils.COLOR_BLUE : DebugUtils.COLOR_WHITE;
            World world = componentAccessor.getExternalData().getWorld();
            DebugUtils.addSphere(world, this.railStepTargetPosition.x, this.railStepTargetPosition.y, this.railStepTargetPosition.z, color, 0.2, 5.0F);
         }
      }
   }

   private void renderChargeCollisionDebug(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.visChargeCollisions || this.visChargeEntityHits) {
         World world = componentAccessor.getExternalData().getWorld();
         if (this.visChargeCollisions) {
            int blockHitCount = this.railResult.getPassThroughCount();

            for (int i = 0; i < blockHitCount; i++) {
               BlockHit hit = this.railResult.getPassThrough(i);
               DebugUtils.addCube(world, hit.blockX + 0.5, hit.blockY + 0.5, hit.blockZ + 0.5, DebugUtils.COLOR_RED, 0.2, 5.0F);
            }
         }

         int entityHitCount = this.railResult.getEntityHitCount();
         Vector3f color = this.visChargeEntityHits ? DebugUtils.COLOR_YELLOW : DebugUtils.COLOR_GREEN;

         for (int i = 0; i < entityHitCount; i++) {
            EntityHit hit = this.railResult.getEntityHit(i);
            if (!this.visChargeEntityHits || this.acceptedEntityHitIndexes.get(i)) {
               DebugUtils.addCube(world, hit.targetPosition.x, hit.targetPosition.y, hit.targetPosition.z, color, 0.2, 5.0F);
            }
         }
      }
   }

   protected double probeCharge(
      @Nonnull Vector3dc chargeStartPosition,
      @Nonnull Vector3dc chargeDirection,
      @Nonnull MotionController motionController,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return motionController.probeMove(ref, chargeStartPosition, chargeDirection, this.probeMoveData, componentAccessor);
   }

   private void populateCandidateEntities(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.candidateEntitiesBuffer.clear();
      PositionCache positionCache = role.getPositionCache();
      positionCache.getPlayers()
         .forEachEntityUnordered(
            14.8F, NOT_SELF_PREDICATE, COLLECT_ENTITY_CONSUMER, ref, null, this.candidateEntitiesBuffer, componentAccessor, componentAccessor
         );
      positionCache.getNpcs()
         .forEachEntityUnordered(
            14.8F, NOT_SELF_PREDICATE, COLLECT_ENTITY_CONSUMER, ref, null, this.candidateEntitiesBuffer, componentAccessor, componentAccessor
         );
   }

   private boolean isPhaseOver(double dt) {
      this.phaseElapsed += dt;
      return this.phaseElapsed >= this.activePhaseDuration;
   }

   @Nonnull
   public BodyMotionCharge.ChargeState getState() {
      return this.sensorVisibleState;
   }

   public int getChargePathCount() {
      return this.railPath.getWaypointCount();
   }

   @Nonnull
   public Vector3dc getChargePathPoint(int index) {
      return this.railPath.getWaypoint(index);
   }

   public int getBlockHitCount() {
      return this.railResult.getPassThroughCount();
   }

   @Nonnull
   public BlockHit getBlockHit(int i) {
      return this.railResult.getPassThrough(i);
   }

   public int getEntityHitCount() {
      return this.filteredEntityHits.size();
   }

   @Nonnull
   public EntityHit getEntityHit(int i) {
      return this.filteredEntityHits.get(i);
   }

   private void executeBlockCollisionInteraction(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.blockCollisionInteractionId != null) {
         InteractionManager interactionManagerComponent = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         if (interactionManagerComponent != null) {
            RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(this.blockCollisionInteractionId);
            World world = componentAccessor.getExternalData().getWorld();
            int hitCount = this.railResult.getPassThroughCount();

            for (int i = 0; i < hitCount; i++) {
               BlockHit hit = this.railResult.getPassThrough(i);
               BlockPosition pos = new BlockPosition(hit.blockX, hit.blockY, hit.blockZ);
               InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, INTERACTION_TYPE, componentAccessor);
               context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, pos);
               context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, world.getBaseBlock(pos));
               InteractionChain chain = interactionManagerComponent.initChain(INTERACTION_TYPE, context, rootInteraction, -1, pos, false);
               interactionManagerComponent.queueExecuteChain(chain);
            }
         }
      }
   }

   private void executeEntityCollisionInteraction(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.npcCollisionInteractionId != null || this.playerCollisionInteractionId != null) {
         InteractionManager interactionManagerComponent = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         if (interactionManagerComponent != null) {
            RootInteraction npcRootInteraction = this.npcCollisionInteractionId != null
               ? RootInteraction.getRootInteractionOrUnknown(this.npcCollisionInteractionId)
               : null;
            RootInteraction playerRootInteraction = this.playerCollisionInteractionId != null
               ? RootInteraction.getRootInteractionOrUnknown(this.playerCollisionInteractionId)
               : null;
            if (npcRootInteraction != null || playerRootInteraction != null) {
               int hitCount = this.filteredEntityHits.size();

               for (int i = 0; i < hitCount; i++) {
                  EntityHit hit = this.filteredEntityHits.get(i);
                  RootInteraction rootInteraction = hit.isPlayer ? playerRootInteraction : npcRootInteraction;
                  if (rootInteraction != null) {
                     Ref<EntityStore> targetRef = hit.entity;
                     if (targetRef != null && targetRef.isValid()) {
                        NetworkId networkIdComponent = componentAccessor.getComponent(targetRef, NetworkId.getComponentType());
                        int networkId = networkIdComponent != null ? networkIdComponent.getId() : -1;
                        InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, INTERACTION_TYPE, componentAccessor);
                        context.getMetaStore().putMetaObject(Interaction.TARGET_ENTITY, targetRef);
                        InteractionChain chain = interactionManagerComponent.initChain(INTERACTION_TYPE, context, rootInteraction, networkId, null, false);
                        interactionManagerComponent.queueExecuteChain(chain);
                     }
                  }
               }
            }
         }
      }
   }

   private void refreshEntityCollisionHits() {
      this.filteredEntityHits.clear();
      this.acceptedEntityHitIndexes.clear();
      int hitCount = this.railResult.getEntityHitCount();
      if (hitCount != 0) {
         if (this.repeatCollisionIgnoreDuration <= 0.0) {
            this.lastEntityCollisionHitTimes.clear();

            for (int i = 0; i < hitCount; i++) {
               this.filteredEntityHits.add(this.railResult.getEntityHit(i));
               this.acceptedEntityHitIndexes.set(i);
            }
         } else {
            double expirationTime = this.collisionClockSeconds - this.repeatCollisionIgnoreDuration;
            ObjectIterator<Entry<Ref<EntityStore>>> iterator = this.lastEntityCollisionHitTimes.reference2DoubleEntrySet().iterator();

            while (iterator.hasNext()) {
               Entry<Ref<EntityStore>> entry = (Entry<Ref<EntityStore>>)iterator.next();
               Ref<EntityStore> ref = (Ref<EntityStore>)entry.getKey();
               if (entry.getDoubleValue() <= expirationTime || ref == null || !ref.isValid()) {
                  iterator.remove();
               }
            }

            for (int i = 0; i < hitCount; i++) {
               EntityHit hit = this.railResult.getEntityHit(i);
               Ref<EntityStore> targetRef = hit.entity;
               if (targetRef != null && targetRef.isValid()) {
                  double lastHitTime = this.lastEntityCollisionHitTimes.getOrDefault(targetRef, Double.NEGATIVE_INFINITY);
                  if (!(this.collisionClockSeconds - lastHitTime < this.repeatCollisionIgnoreDuration)) {
                     this.filteredEntityHits.add(hit);
                     this.acceptedEntityHitIndexes.set(i);
                     this.lastEntityCollisionHitTimes.put(targetRef, this.collisionClockSeconds);
                  }
               }
            }
         }
      }
   }

   private void clearEntityCollisionHits() {
      this.filteredEntityHits.clear();
      this.acceptedEntityHitIndexes.clear();
   }

   private double getEffectiveMaximumChargeSpeed(@Nullable MotionController motionController) {
      if (this.chargeAbsoluteSpeed > 0.0) {
         return this.chargeAbsoluteSpeed;
      } else {
         double maximumSpeed = motionController != null ? motionController.getMaximumSpeed() : 0.0;
         return this.chargeRelativeSpeed * maximumSpeed;
      }
   }

   private void initPhaseDuration(@Nonnull double[] durationRange) {
      this.activePhaseDuration = RandomExtra.randomRange(durationRange);
      this.phaseElapsed = 0.0;
   }

   private static boolean isZeroDurationRange(@Nonnull double[] durationRange) {
      return durationRange[0] == 0.0 && durationRange[1] == 0.0;
   }

   @Override
   public void onDebugFlagsChanged(EnumSet<RoleDebugFlags> newFlags) {
      this.debugChargeState = newFlags.contains(RoleDebugFlags.ChargeState);
      this.debugChargePath = newFlags.contains(RoleDebugFlags.ChargePath);
      this.visChargePath = newFlags.contains(RoleDebugFlags.VisChargePath);
      this.visChargeCollisions = newFlags.contains(RoleDebugFlags.VisChargeCollisions);
      this.visChargeEntityHits = newFlags.contains(RoleDebugFlags.VisChargeEntityHits);
   }

   private static void turnTo(@Nonnull Steering desiredSteering, @Nonnull Rotation3f rotation, double turnSpeed) {
      desiredSteering.setYaw(rotation.yaw());
      desiredSteering.setPitch(rotation.pitch());
      desiredSteering.setRelativeTurnSpeed(turnSpeed);
   }

   private static void turnTo(@Nonnull Steering desiredSteering, @Nonnull AimingData aimingData, double turnSpeed) {
      desiredSteering.setYaw(aimingData.getYaw());
      desiredSteering.setPitch(aimingData.getPitch());
      desiredSteering.setRelativeTurnSpeed(turnSpeed);
   }

   private static enum ChargeEvents {
      HitPlayer,
      HitNPC,
      HitDestructible,
      HitStop,
      AtTargetPosition;
   }

   public static enum ChargeState implements Supplier<String> {
      Aiming("Have a target"),
      LockedOn("Target in range and within view cone"),
      LostTarget("Target lost or out of range"),
      WindingUp("Preparing to charge"),
      Launch("Starting the charge"),
      Charging("Charging"),
      Obstructed("Charge obstructed by something"),
      Finished("Finished charge");

      private final String description;

      private ChargeState(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
