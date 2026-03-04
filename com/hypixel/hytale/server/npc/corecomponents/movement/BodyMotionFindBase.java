package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFindBase;
import com.hypixel.hytale.server.npc.movement.NavState;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceWithTarget;
import com.hypixel.hytale.server.npc.navigation.AStarBase;
import com.hypixel.hytale.server.npc.navigation.AStarDebugBase;
import com.hypixel.hytale.server.npc.navigation.AStarEvaluator;
import com.hypixel.hytale.server.npc.navigation.AStarNode;
import com.hypixel.hytale.server.npc.navigation.AStarNodePoolProvider;
import com.hypixel.hytale.server.npc.navigation.AStarNodePoolProviderSimple;
import com.hypixel.hytale.server.npc.navigation.PathFollower;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BodyMotionFindBase<T extends AStarBase> extends BodyMotionBase implements AStarEvaluator {
   protected final int nodesPerTick;
   protected final boolean useBestPath;
   protected final double throttleDelayMin;
   protected final double throttleDelayMax;
   protected final int throttleIgnoreCount;
   protected final boolean useSteering;
   protected final boolean usePathfinder;
   protected final boolean skipSteering;
   protected final double minPathLength;
   protected final double minPathLengthSquared;
   protected final boolean canSkipSteering;
   protected final boolean isAvoidingBlockDamage;
   protected final boolean isRelaxedMoveConstraints;
   protected final double desiredAltitudeWeight;
   protected final boolean dbgStatus;
   protected final boolean dbgProfile;
   protected final boolean dbgMaps;
   protected final boolean dbgOpens;
   protected final boolean dbgPath;
   protected final boolean dbgRebuild;
   protected final boolean dbgNodes;
   protected final boolean dbgStay;
   protected final boolean dbgMotionState;
   @Nonnull
   protected final T aStar;
   @Nonnull
   protected final AStarDebugBase aStarDebug;
   protected final PathFollower pathFollower = new PathFollower();
   protected final ProbeMoveData probeMoveData = new ProbeMoveData();
   protected AStarNodePoolProvider sharedNodePoolProvider;
   protected int throttleCount;
   protected double throttleTime;
   protected double targetDeltaSquared;
   protected boolean wasSteering;
   protected double throttleDelay;
   protected boolean passedWaypoint;
   protected boolean wasAvoidingBlockDamage;
   protected boolean dbgDisplayString;
   protected StringBuilder debugString;

   public BodyMotionFindBase(@Nonnull BuilderBodyMotionFindBase builderBodyMotionFindBase, @Nonnull BuilderSupport support, @Nonnull T aStar) {
      super(builderBodyMotionFindBase);
      this.aStar = aStar;
      this.aStarDebug = aStar.createDebugHelper(HytaleLogger.forEnclosingClass());
      this.useBestPath = builderBodyMotionFindBase.getUseBestPath(support);
      this.nodesPerTick = builderBodyMotionFindBase.getNodesPerTick(support);
      this.usePathfinder = builderBodyMotionFindBase.isUsePathfinder(support);
      this.useSteering = builderBodyMotionFindBase.isUseSteering(support);
      this.skipSteering = builderBodyMotionFindBase.isSkipSteering(support);
      this.canSkipSteering = this.skipSteering || this.usePathfinder;
      this.minPathLength = builderBodyMotionFindBase.getMinPathLength(support);
      this.minPathLengthSquared = this.minPathLength * this.minPathLength;
      double[] throttleDelayRange = builderBodyMotionFindBase.getThrottleDelayRange(support);
      this.throttleDelayMin = throttleDelayRange[0];
      this.throttleDelayMax = throttleDelayRange[1];
      this.throttleIgnoreCount = builderBodyMotionFindBase.getThrottleIgnoreCount(support);
      this.isRelaxedMoveConstraints = builderBodyMotionFindBase.isRelaxedMoveConstraints(support);
      this.isAvoidingBlockDamage = builderBodyMotionFindBase.isAvoidingBlockDamage(support);
      this.desiredAltitudeWeight = builderBodyMotionFindBase.getDesiredAltitudeWeight(support);
      this.probeMoveData.setRelaxedMoveConstraints(this.isRelaxedMoveConstraints);
      this.pathFollower.setPathSmoothing(builderBodyMotionFindBase.getPathSmoothing(support));
      this.pathFollower.setRelativeSpeed(builderBodyMotionFindBase.getRelativeSpeed(support));
      this.pathFollower.setRelativeSpeedWaypoint(builderBodyMotionFindBase.getRelativeSpeedWaypoint(support));
      this.pathFollower.setWaypointRadius(builderBodyMotionFindBase.getWaypointRadius(support));
      this.pathFollower.setRejectionWeight(builderBodyMotionFindBase.getRejectionWeight(support));
      this.pathFollower.setBlendHeading(builderBodyMotionFindBase.getBlendHeading(support));
      this.aStar.setMaxPathLength(builderBodyMotionFindBase.getMaxPathLength(support));
      this.aStar.setOpenNodesLimit(builderBodyMotionFindBase.getMaxOpenNodes(support));
      this.aStar.setTotalNodesLimit(builderBodyMotionFindBase.getMaxTotalNodes(support));
      this.aStar.setCanMoveDiagonal(builderBodyMotionFindBase.isDiagonalMoves(support));
      this.aStar.setOptimizedBuildPath(builderBodyMotionFindBase.isBuildOptimisedPath(support));
      EnumSet<BodyMotionFindBase.DebugFlags> debugFlags = builderBodyMotionFindBase.getParsedDebugFlags();
      this.dbgRebuild = debugFlags.contains(BodyMotionFindBase.DebugFlags.Rebuild);
      this.dbgNodes = debugFlags.contains(BodyMotionFindBase.DebugFlags.Nodes);
      this.dbgProfile = debugFlags.contains(BodyMotionFindBase.DebugFlags.Profile);
      this.dbgPath = debugFlags.contains(BodyMotionFindBase.DebugFlags.Path);
      this.dbgOpens = debugFlags.contains(BodyMotionFindBase.DebugFlags.Opens);
      this.dbgMaps = debugFlags.contains(BodyMotionFindBase.DebugFlags.Maps);
      this.dbgStatus = debugFlags.contains(BodyMotionFindBase.DebugFlags.Status);
      this.dbgStay = debugFlags.contains(BodyMotionFindBase.DebugFlags.Stay);
      this.dbgMotionState = debugFlags.contains(BodyMotionFindBase.DebugFlags.Motion);
      this.dbgDisplayString = false;
      this.pathFollower.setDebugNodes(this.dbgNodes);
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      MotionController activeMotionController = role.getActiveMotionController();
      this.sharedNodePoolProvider = componentAccessor.getResource(AStarNodePoolProviderSimple.getResourceType());
      this.dbgDisplayString = role.getDebugSupport().getDebugFlags().contains(RoleDebugFlags.Pathfinder);
      this.setNavStateInit(activeMotionController);
      this.wasSteering = false;
      this.wasAvoidingBlockDamage = this.isAvoidingBlockDamage && activeMotionController.isAvoidingBlockDamage();
      this.aStar.setStartPosition(transformComponent.getPosition());
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.pathFollower.clearPath();
      this.aStar.clearPath();
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider infoProvider,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      desiredSteering.clear();
      MotionController activeMotionController = role.getActiveMotionController();
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      if (!this.canComputeMotion(ref, role, infoProvider, componentAccessor)) {
         this.setNavStateAborted(activeMotionController);
         this.wasSteering = false;
         return false;
      } else {
         if (!this.isAvoidingBlockDamage) {
            activeMotionController.setAvoidingBlockDamage(false);
         }

         activeMotionController.setRelaxedMoveConstraints(this.isRelaxedMoveConstraints);
         this.probeMoveData.setAvoidingBlockDamage(activeMotionController.isAvoidingBlockDamage());
         if (this.isGoalReached(ref, activeMotionController, position, componentAccessor)) {
            this.setNavStateAtGoal(role.getActiveMotionController());
            this.wasSteering = false;
            if (this.dbgMotionState) {
               ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: At Goal");
            }

            return false;
         } else {
            if (this.throttleCount > this.throttleIgnoreCount) {
               this.throttleTime += dt;
            }

            this.throttleDelay -= dt;
            if (this.throttleDelay > 0.0) {
               this.wasSteering = false;
               if (!this.mustAbortThrottling(activeMotionController, ref)) {
                  this.onThrottling(activeMotionController, ref, desiredSteering, componentAccessor);
                  this.setNavStateThrottling(activeMotionController);
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Delaying");
                  }

                  return true;
               }

               this.resetThrottleCount();
            }

            boolean unobstructed = !activeMotionController.isObstructed();
            if (this.passedWaypoint && this.pathFollower.getCurrentWaypoint() != null && this.useSteering && unobstructed) {
               if (this.canSwitchToSteering(ref, activeMotionController, componentAccessor)) {
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Switch to steering");
                  }

                  this.forceRecomputePath(activeMotionController);
                  this.wasSteering = true;
               }

               this.passedWaypoint = false;
            }

            if (this.pathFollower.getCurrentWaypoint() == null && !this.aStar.isComputing() && this.useSteering) {
               if (unobstructed
                  && (this.wasSteering || !this.canSkipSteering || !this.shouldSkipSteering(ref, activeMotionController, position, componentAccessor))
                  && this.computeSteering(ref, role, position, desiredSteering, componentAccessor)) {
                  this.setNavStateSteering(role.getActiveMotionController());
                  this.onSteering(activeMotionController, ref, componentAccessor);
                  this.wasSteering = true;
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Steering");
                  }

                  return true;
               }

               if (!this.usePathfinder) {
                  this.setNavStateBlocked(role.getActiveMotionController());
                  this.wasSteering = false;
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Blocked");
                  }

                  return false;
               }

               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: End of steering - Fall through path finder");
               }

               this.forceRecomputePath(activeMotionController);
            }

            this.wasSteering = false;
            boolean mustRecomputePath = this.mustRecomputePath(activeMotionController);
            boolean forceRecomputePath = activeMotionController.isForceRecomputePath();
            if (mustRecomputePath || forceRecomputePath) {
               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100))
                     .log("MotionFindBase: Trigger Recomputing path reason mustRecompute=%s forceRecompute=%s", mustRecomputePath, forceRecomputePath);
               }

               this.forceRecomputePath(activeMotionController);
            }

            if (this.pathFollower.getCurrentWaypoint() != null) {
               this.updatePathFollower(ref, position, activeMotionController, componentAccessor);
               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Updating path follower");
               }
            }

            if (this.pathFollower.pathInFinalStage() && activeMotionController.canAct(ref, componentAccessor)) {
               if (this.pathFollower.getCurrentWaypoint() == null) {
                  if (this.aStar.getPath() != null) {
                     this.setPath(ref, position, activeMotionController, componentAccessor);
                     if (this.dbgMotionState) {
                        ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: End of path - Precomputed exists");
                     }
                  } else if (!this.aStar.isComputing()) {
                     if (unobstructed && (!this.canSkipSteering || !this.shouldSkipSteering(ref, activeMotionController, position, componentAccessor))) {
                        this.setNavStateSteering(activeMotionController);
                        if (this.dbgMotionState) {
                           ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100))
                              .log("MotionFindBase: End of path - Switch back to steering");
                        }

                        return false;
                     }

                     if (this.shouldDeferPathComputation(activeMotionController, position, componentAccessor)) {
                        this.onDeferring(activeMotionController, ref, desiredSteering, componentAccessor);
                        this.setNavStateDeferred(activeMotionController);
                        if (this.dbgMotionState) {
                           ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100))
                              .log("MotionFindBase: End of path - Deferring computations");
                        }

                        return true;
                     }

                     if (!this.startPathFinder(ref, position, role, activeMotionController, componentAccessor)) {
                        this.onNoPathFound(activeMotionController);
                        this.setNavStateAborted(activeMotionController);
                        if (this.dbgMotionState) {
                           ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100))
                              .log("MotionFindBase: End of path - Start path finder failed");
                        }

                        return false;
                     }

                     if (this.dbgMotionState) {
                        ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: End of path - Restarted path finder");
                     }
                  }
               } else if (this.pathFollower.isWaypointFrozen()) {
                  this.aStar.clearPath();
                  Vector3d targetPosition = this.pathFollower.getCurrentWaypointPosition();
                  if (targetPosition.distanceSquaredTo(position) < 1.0) {
                     this.pathFollower.setWaypointFrozen(false);
                     if (this.canSkipSteering && this.shouldSkipSteering(ref, activeMotionController, targetPosition, componentAccessor)) {
                        this.startPathFinder(ref, position, role, activeMotionController, componentAccessor);
                        if (this.dbgMotionState) {
                           ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Early start path computation");
                        }
                     } else if (this.dbgMotionState) {
                        ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Skipped early start path computation");
                     }
                  }
               }
            }

            if (this.aStar.isComputing() && this.continuePathFinder(ref, activeMotionController, componentAccessor)) {
               if (this.pathFollower.getCurrentWaypoint() == null) {
                  this.setNavStateComputing(activeMotionController);
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Computing - no follower");
                  }

                  return true;
               }

               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Computing");
               }
            }

            if (this.pathFollower.getCurrentWaypoint() == null) {
               if (this.aStar.getPath() == null) {
                  this.setNavStateThrottling(activeMotionController);
                  if (this.dbgMotionState) {
                     ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: No path - throttling");
                  }

                  return false;
               }

               this.setPath(ref, position, activeMotionController, componentAccessor);
               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: No path - setting new path");
               }
            }

            if (activeMotionController.canAct(ref, componentAccessor) && !this.dbgStay) {
               this.pathFollower.executePath(position, activeMotionController, desiredSteering);
               if (this.dbgMotionState) {
                  ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFindBase: Executing path");
               }
            }

            this.setNavStateFollowing(activeMotionController);
            return true;
         }
      }
   }

   public abstract void findBestPath(AStarBase var1, MotionController var2);

   protected boolean startPathFinder(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      Role role,
      @Nonnull MotionController activeMotionController,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.dbgProfile) {
         long time = System.nanoTime();
         AStarBase.Progress progress = this.startComputePath(ref, role, activeMotionController, position, componentAccessor);
         if (progress == AStarBase.Progress.COMPUTING) {
            progress = this.aStar.computePath(ref, activeMotionController, this.probeMoveData, Integer.MAX_VALUE, componentAccessor);
         }

         time = System.nanoTime() - time;
         NPCPlugin.get().getLogger().at(Level.INFO).log("Path computation profile %s in %s", progress.toString(), time / 1000L);
         if (progress != AStarBase.Progress.ACCOMPLISHED) {
            return false;
         }

         if (this.dbgPath) {
            this.aStarDebug.dumpPath();
            this.aStarDebug.dumpMap(true, activeMotionController);
         }
      } else {
         AStarBase.Progress progressx = this.startComputePath(ref, role, activeMotionController, position, componentAccessor);
         if (progressx != AStarBase.Progress.COMPUTING) {
            if (this.dbgStatus) {
               NPCPlugin.get().getLogger().at(Level.INFO).log("Path computation start failed %s", progressx.toString());
            }

            return false;
         }
      }

      return true;
   }

   protected boolean continuePathFinder(
      @Nonnull Ref<EntityStore> ref, @Nonnull MotionController activeMotionController, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AStarBase.Progress progress = this.aStar.computePath(ref, activeMotionController, this.probeMoveData, this.nodesPerTick, componentAccessor);
      if (progress == AStarBase.Progress.COMPUTING) {
         this.setNavStateComputing(activeMotionController);
         if (this.dbgOpens) {
            this.aStarDebug.dumpOpens(activeMotionController);
         }

         if (this.dbgMaps) {
            this.aStarDebug.dumpMap(true, activeMotionController);
         }

         return true;
      } else {
         if (progress == AStarBase.Progress.ACCOMPLISHED) {
            this.resetThrottleCount();
         } else if (this.useBestPath) {
            this.findBestPath(this.aStar, activeMotionController);
            this.throttleCount++;
            if (this.aStar.getPath() != null) {
               progress = AStarBase.Progress.ACCOMPLISHED;
            }
         }

         if (progress != AStarBase.Progress.ACCOMPLISHED) {
            if (this.dbgStatus) {
               NPCPlugin.get().getLogger().at(Level.INFO).log("Path computation failed %s", progress.toString());
            }

            this.aStar.clearPath();
            this.onNoPathFound(activeMotionController);
            return false;
         } else {
            double pathLengthSquared = this.aStar.getEndPosition().distanceSquaredTo(this.aStar.getPosition());
            if (pathLengthSquared < this.minPathLengthSquared) {
               if (this.dbgStatus) {
                  NPCPlugin.get().getLogger().at(Level.INFO).log("Path computation failed. Path to short length=%s", Math.sqrt(pathLengthSquared));
               }

               this.aStar.clearPath();
               this.onNoPathFound(activeMotionController);
               return false;
            } else {
               if (this.dbgPath) {
                  this.aStarDebug.dumpPath();
               }

               if (this.dbgPath || this.dbgMaps) {
                  this.aStarDebug.dumpMap(true, activeMotionController);
               }

               return false;
            }
         }
      }
   }

   protected boolean updatePathFollower(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull MotionController activeMotionController,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.pathFollower.updateCurrentTarget(position, activeMotionController)) {
         return false;
      } else {
         if (this.pathFollower.shouldSmoothPath()) {
            this.passedWaypoint = true;
            this.pathFollower.smoothPath(ref, position, activeMotionController, this.probeMoveData, componentAccessor);
         }

         return true;
      }
   }

   protected boolean canSwitchToSteering(
      @Nonnull Ref<EntityStore> ref, MotionController motionController, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return false;
   }

   protected boolean shouldSkipSteering(
      @Nonnull Ref<EntityStore> ref, MotionController activeMotionController, Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return !this.useSteering;
   }

   protected boolean computeSteering(
      @Nonnull Ref<EntityStore> ref, Role role, Vector3d position, Steering desiredSteering, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return false;
   }

   protected boolean scaleSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull SteeringForceWithTarget steeringForce,
      @Nonnull Steering desiredSteering,
      double desiredAltitudeWeight,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      MotionController motionController = role.getActiveMotionController();
      boolean approachDesiredHeight = !motionController.is2D() && desiredAltitudeWeight > 0.0;
      boolean withinRange = approachDesiredHeight && motionController.getDesiredVerticalRange(ref, componentAccessor).isWithinRange();
      if (withinRange) {
         steeringForce.setComponentSelector(role.getActiveMotionController().getPlanarComponentSelector());
      } else {
         steeringForce.setComponentSelector(role.getActiveMotionController().getComponentSelector());
      }

      if (!steeringForce.compute(desiredSteering)) {
         return false;
      } else {
         desiredSteering.scaleTranslation(this.getRelativeSpeed());
         if (approachDesiredHeight && !withinRange) {
            MotionController.VerticalRange desiredAltitudeRange = motionController.getDesiredVerticalRange(ref, componentAccessor);
            if (desiredAltitudeRange.current > desiredAltitudeRange.max) {
               desiredSteering.setY(-this.computeDesiredYTranslation(desiredSteering, motionController.getMaxSinkAngle(), desiredAltitudeWeight));
            } else if (desiredAltitudeRange.current < desiredAltitudeRange.min) {
               desiredSteering.setY(this.computeDesiredYTranslation(desiredSteering, motionController.getMaxClimbAngle(), desiredAltitudeWeight));
            }
         }

         motionController.requireDepthProbing();
         return true;
      }
   }

   protected double computeDesiredYTranslation(@Nonnull Steering desiredSteering, float maxAngle, double desiredAltitudeWeight) {
      double dirX = desiredSteering.getX();
      double dirZ = desiredSteering.getZ();
      double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
      return length * TrigMathUtil.sin(maxAngle * desiredAltitudeWeight);
   }

   protected void onNoPathFound(MotionController motionController) {
      this.aStar.clearPath();
   }

   protected void onBlockedPath() {
   }

   protected void onSteering(MotionController activeMotionController, Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
   }

   protected void onThrottling(MotionController motionController, Ref<EntityStore> ref, Steering steering, ComponentAccessor<EntityStore> componentAccessor) {
   }

   protected void onDeferring(MotionController motionController, Ref<EntityStore> ref, Steering steering, ComponentAccessor<EntityStore> componentAccessor) {
   }

   protected boolean mustAbortThrottling(MotionController motionController, Ref<EntityStore> ref) {
      return false;
   }

   protected abstract boolean isGoalReached(Ref<EntityStore> var1, MotionController var2, Vector3d var3, ComponentAccessor<EntityStore> var4);

   protected void setNavState(NavState state, String label, boolean reset, @Nonnull MotionController activeMotionController) {
      if (reset) {
         this.resetThrottleCount();
         this.targetDeltaSquared = 0.0;
         this.forceRecomputePath(activeMotionController);
      }

      activeMotionController.setNavState(state, this.throttleTime, this.targetDeltaSquared);
      if (this.dbgDisplayString) {
         if (this.debugString == null) {
            this.debugString = new StringBuilder();
         }

         this.debugString.append(label).append(" TC:").append(this.throttleCount).append(" TT:").append(MathUtil.floor(this.throttleTime * 10.0) / 10.0);
         this.decorateDebugString(this.debugString);
         activeMotionController.getRole().getDebugSupport().setDisplayPathfinderString(this.debugString.toString());
         this.debugString.setLength(0);
      }
   }

   protected void decorateDebugString(StringBuilder dbgString) {
   }

   protected void setNavStateInit(@Nonnull MotionController motionController) {
      this.setNavState(NavState.INIT, "I-START", true, motionController);
   }

   protected void setNavStateComputing(@Nonnull MotionController motionController) {
      this.setNavState(NavState.PROGRESSING, "P-COMPT", false, motionController);
   }

   protected void setNavStateDeferred(@Nonnull MotionController motionController) {
      this.setNavState(NavState.DEFER, "D-DEFER", false, motionController);
   }

   protected void setNavStateAtGoal(@Nonnull MotionController motionController) {
      this.setNavState(NavState.AT_GOAL, "G-GOAL", true, motionController);
   }

   protected void setNavStateFollowing(@Nonnull MotionController motionController) {
      this.setNavState(NavState.PROGRESSING, "P-FOLLW", false, motionController);
   }

   protected void setNavStateSteering(@Nonnull MotionController motionController) {
      this.setNavState(NavState.PROGRESSING, "P-STEER", false, motionController);
   }

   protected void setNavStateBlocked(@Nonnull MotionController motionController) {
      this.setNavState(NavState.BLOCKED, "B-BLOCK", false, motionController);
   }

   protected void setNavStateAborted(@Nonnull MotionController motionController) {
      this.setNavState(NavState.ABORTED, "A-ABORT", true, motionController);
   }

   protected void setNavStateThrottling(@Nonnull MotionController motionController) {
      if (this.throttleDelay <= 0.0 && this.throttleDelayMax > 0.0 && this.throttleCount > this.throttleIgnoreCount) {
         this.throttleDelay = RandomExtra.randomRange(this.throttleDelayMin, this.throttleDelayMax);
      }

      this.setNavState(NavState.PROGRESSING, "P-THRTL", false, motionController);
   }

   protected void setPath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull MotionController activeMotionController,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AStarNode aStarPath = this.aStar.getPath();
      this.pathFollower.setPath(aStarPath, position);
      this.passedWaypoint = false;
      this.updatePathFollower(ref, position, activeMotionController, componentAccessor);
   }

   protected void resetThrottleCount() {
      this.throttleTime = 0.0;
      this.throttleCount = 0;
      this.throttleDelay = 0.0;
   }

   protected AStarBase.Progress startComputePath(
      @Nonnull Ref<EntityStore> ref,
      Role role,
      @Nonnull MotionController activeMotionController,
      @Nonnull Vector3d position,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.aStar.initComputePath(ref, position, this, activeMotionController, this.probeMoveData, this.sharedNodePoolProvider, componentAccessor);
   }

   protected boolean shouldDeferPathComputation(MotionController motionController, Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return false;
   }

   protected boolean canComputeMotion(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider positionProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return true;
   }

   protected boolean mustRecomputePath(@Nonnull MotionController activeMotionController) {
      if (this.dbgRebuild) {
         return true;
      } else if (activeMotionController.isObstructed()) {
         if (this.dbgStatus) {
            NPCPlugin.get().getLogger().at(Level.INFO).log("Recomputing Path - Blocked");
         }

         this.onBlockedPath();
         return true;
      } else {
         boolean avoidingBlockDamage = activeMotionController.isAvoidingBlockDamage();
         if (this.wasAvoidingBlockDamage != avoidingBlockDamage) {
            this.wasAvoidingBlockDamage = avoidingBlockDamage;
            if (this.dbgStatus) {
               NPCPlugin.get().getLogger().at(Level.INFO).log("Recomputing Path - AvoidBlockDamage changed");
            }

            return true;
         } else {
            return false;
         }
      }
   }

   protected double getRelativeSpeed() {
      return this.pathFollower.getRelativeSpeed();
   }

   protected void forceRecomputePath(MotionController activeMotionController) {
      this.aStar.clearPath();
      this.pathFollower.clearPath();
   }

   public static enum DebugFlags implements Supplier<String> {
      Opens("Display open nodes each step when computing"),
      Maps("Display map each step when computing"),
      Path("Display map when path was found"),
      Status("Display status messages"),
      Rebuild("Force immediate rebuild of path"),
      Profile("Measure time for path finding"),
      Nodes("Display walk node information"),
      Motion("Display Motion state changes"),
      Stay("Don't move. Only compute path");

      private final String description;

      private DebugFlags(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
