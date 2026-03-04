package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.IPathWaypoint;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderBodyMotionPath;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForcePursue;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceRotate;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPathProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionPath extends BodyMotionBase {
   public static final double MIN_GUARD_POINT_WAIT_TIME = 1.0;
   public static final boolean TESTING = false;
   protected final BodyMotionPath.Shape shape;
   protected final double pathWidth;
   protected final double nodeWidth;
   protected final double minRelativeSpeed;
   protected final double maxRelativeSpeed;
   protected final double minWalkDistance;
   protected final double maxWalkDistance;
   protected final boolean startAtNearestNode;
   protected final BodyMotionPath.Direction direction;
   protected final double minNodeDelay;
   protected final double maxNodeDelay;
   protected final int viewSegments;
   protected final boolean useNodeViewDirection;
   protected final boolean pickRandomAngle;
   protected final double minDelayScale;
   protected final double maxDelayScale;
   protected final double minPercentage;
   protected final double maxPercentage;
   protected int currentWaypointIndex = -1;
   @Nullable
   protected BodyMotionPath.Direction currentDirection;
   protected final Vector3d currentWaypointPosition = new Vector3d();
   protected final Vector3d lastWaypointPosition = new Vector3d();
   protected final IntList visitOrder = new IntArrayList();
   protected int visitIndex;
   protected final SteeringForceRotate steeringForceRotate = new SteeringForceRotate();
   protected final SteeringForcePursue steeringForcePursue = new SteeringForcePursue();
   protected double currentSpeed;
   protected final Vector3d currentPosition = new Vector3d();
   protected final Vector3d nextPosition = new Vector3d();
   protected boolean nextPositionValid;
   protected double currentNodeDelay;
   protected boolean pendingNodeDelay;
   protected boolean rotatingToView;
   protected float nodeViewDirection;
   protected double nodeWaitTime;
   protected float observationSector;
   protected double currentObservationDelay;
   protected boolean rotating;
   protected final Vector3d previousSteeringTranslation = new Vector3d(Vector3d.MIN);
   protected int currentViewSegment;

   public BodyMotionPath(@Nonnull BuilderBodyMotionPath builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.shape = builder.getShape(support);
      this.pathWidth = builder.getPathWidth();
      this.nodeWidth = builder.getNodeWidth();
      this.minRelativeSpeed = builder.getMinRelativeSpeed();
      this.maxRelativeSpeed = builder.getMaxRelativeSpeed();
      this.minWalkDistance = builder.getMinWalkDistance();
      this.maxWalkDistance = builder.getMaxWalkDistance();
      this.startAtNearestNode = builder.isStartAtNearestNode();
      this.direction = builder.getDirection();
      this.minNodeDelay = builder.getMinNodeDelay();
      this.maxNodeDelay = builder.getMaxNodeDelay();
      this.useNodeViewDirection = builder.isUseNodeViewDirection();
      double[] delayScaleRange = builder.getDelayScaleRange(support);
      this.minDelayScale = delayScaleRange[0];
      this.maxDelayScale = delayScaleRange[1];
      double[] delayPercentRange = builder.getPercentDelayRange(support);
      this.minPercentage = delayPercentRange[0];
      this.maxPercentage = delayPercentRange[1];
      this.pickRandomAngle = builder.isPickRandomAngle();
      this.viewSegments = builder.getViewSegments(support);
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.reset();
   }

   @Override
   public void loaded(Role role) {
      this.invalidateWaypoint();
      this.reset();
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
      desiredSteering.clear();
      if (!role.getActiveMotionController().canAct(ref, componentAccessor)) {
         return true;
      } else {
         IPathProvider info = sensorInfo.getExtraInfo(IPathProvider.class);
         if (info != null && info.hasPath()) {
            IPath<?> path = info.getPath();
            int numWaypoints = path.length();
            if (this.visitOrder.size() != numWaypoints) {
               this.visitOrder.clear();

               for (int i = 0; i < numWaypoints; i++) {
                  this.visitOrder.add(i);
               }

               IntLists.shuffle(this.visitOrder, ThreadLocalRandom.current());
               this.visitIndex = 0;
               this.invalidateWaypoint();
            }

            TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            if (this.currentWaypointIndex == -1) {
               if (!this.getFirstWaypoint(ref, role, path, position, componentAccessor)) {
                  return false;
               }

               this.nextPositionValid = false;
               this.currentNodeDelay = 0.0;
            }

            float heading = transformComponent.getRotation().getYaw();
            if (this.currentNodeDelay > 0.0) {
               this.currentNodeDelay -= dt;
               if (this.observationSector != 0.0F || numWaypoints == 1) {
                  this.steeringForceRotate.setHeading(heading);
                  if (this.steeringForceRotate.compute(desiredSteering)) {
                     return true;
                  }

                  desiredSteering.setYaw(heading);
                  if (this.tickObservationDelay(dt)) {
                     return true;
                  }

                  this.pickNextObservationAngle();
               }

               return true;
            } else {
               MotionController activeMotionController = role.getActiveMotionController();
               Vector3d componentSelector = activeMotionController.getComponentSelector();
               WorldSupport worldSupport = role.getWorldSupport();
               this.currentPosition.assign(position.getX(), position.getY(), position.getZ());
               int lastIndex = this.currentWaypointIndex;
               this.lastWaypointPosition.assign(this.currentWaypointPosition);

               while (this.closeToPosition(this.currentWaypointPosition, activeMotionController)) {
                  if (this.nextPositionValid || numWaypoints == 1) {
                     IPathWaypoint wayPoint = path.get(this.currentWaypointIndex);
                     if (wayPoint == null) {
                        return false;
                     }

                     this.nodeViewDirection = wayPoint.getWaypointRotation(componentAccessor).getYaw();
                     this.nodeWaitTime = wayPoint.getPauseTime();
                     this.observationSector = wayPoint.getObservationAngle() / 2.0F;
                     this.currentViewSegment = 0;
                     if (numWaypoints == 1) {
                        this.currentNodeDelay = MathUtil.maxValue(this.nodeWaitTime, 1.0);
                        this.pickNextObservationAngle();
                        desiredSteering.setYaw(heading);
                        return true;
                     }
                  }

                  this.nextPositionValid = false;
                  if (!this.nextWayPoint(path, worldSupport, componentAccessor)) {
                     if (worldSupport.hasRequestedNewPath()) {
                        desiredSteering.setTranslation(this.previousSteeringTranslation);
                     }

                     return false;
                  }

                  if (this.currentWaypointIndex == lastIndex) {
                     return false;
                  }
               }

               if (!this.nextPositionValid || this.closeToPosition(this.nextPosition, activeMotionController)) {
                  if (this.pathWidth == 0.0) {
                     this.nextPosition.assign(this.currentWaypointPosition);
                  } else {
                     double maxDistance = NPCPhysicsMath.dotProduct(
                        this.currentWaypointPosition, this.lastWaypointPosition, this.currentPosition, componentSelector
                     );
                     double distance = Math.min(RandomExtra.randomRange(this.minWalkDistance, this.maxWalkDistance), maxDistance);
                     if (distance >= maxDistance - this.nodeWidth) {
                        this.nextPosition.assign(this.currentWaypointPosition);
                     } else {
                        NPCPhysicsMath.orthoComposition(
                           this.lastWaypointPosition,
                           this.currentWaypointPosition,
                           distance,
                           Vector3d.UP,
                           RandomExtra.randomRange(-this.pathWidth / 2.0, this.pathWidth / 2.0),
                           this.nextPosition
                        );
                     }
                  }

                  this.nextPositionValid = true;
                  this.currentSpeed = RandomExtra.randomRange(this.minRelativeSpeed, this.maxRelativeSpeed);
                  this.steeringForcePursue.setTargetPosition(this.nextPosition);
                  this.steeringForcePursue.setDistances(this.nodeWidth * 1.0, 0.1);
                  this.steeringForcePursue.setComponentSelector(componentSelector);
                  if ((!this.useNodeViewDirection || !(this.minNodeDelay > 0.0)) && !(this.nodeWaitTime > 0.0)) {
                     this.steeringForceRotate.setDesiredHeading(NPCPhysicsMath.lookatHeading(this.currentPosition, this.nextPosition, heading));
                     this.pendingNodeDelay = this.minNodeDelay > 0.0;
                  } else {
                     this.pickNextObservationAngle();
                     this.rotatingToView = true;
                     this.pendingNodeDelay = false;
                  }

                  this.rotating = true;
               }

               if (this.rotating) {
                  this.steeringForceRotate.setHeading(heading);
                  if (this.useNodeViewDirection && this.rotatingToView) {
                     if (this.steeringForceRotate.compute(desiredSteering)) {
                        return true;
                     }

                     desiredSteering.setYaw(heading);
                     if (this.tickObservationDelay(dt)) {
                        return true;
                     }

                     this.rotatingToView = false;
                     this.steeringForceRotate.setDesiredHeading(NPCPhysicsMath.lookatHeading(this.currentPosition, this.nextPosition, heading));
                     if (this.minNodeDelay > 0.0) {
                        this.currentNodeDelay = RandomExtra.randomRange(this.minNodeDelay, this.maxNodeDelay);
                     }

                     if (this.nodeWaitTime > this.currentNodeDelay) {
                        this.currentNodeDelay = this.nodeWaitTime;
                     }

                     if (this.currentNodeDelay > 0.0) {
                        this.pickNextObservationAngle();
                     }

                     return true;
                  }

                  if (this.steeringForceRotate.compute(desiredSteering)) {
                     return true;
                  }

                  if (this.pendingNodeDelay) {
                     this.currentNodeDelay = RandomExtra.randomRange(this.minNodeDelay, this.maxNodeDelay);
                     this.pendingNodeDelay = false;
                     return true;
                  }

                  this.rotating = false;
               }

               this.steeringForcePursue.setSelfPosition(this.currentPosition);
               this.steeringForcePursue.setComponentSelector(activeMotionController.getComponentSelector());
               this.nextPositionValid = this.steeringForcePursue.compute(desiredSteering);
               if (desiredSteering.hasTranslation()) {
                  desiredSteering.scaleTranslation(this.currentSpeed);
               }

               this.previousSteeringTranslation.assign(desiredSteering.getTranslation());
               return true;
            }
         } else {
            return false;
         }
      }
   }

   protected boolean tickObservationDelay(double dt) {
      if (this.currentObservationDelay > 0.0) {
         this.currentObservationDelay -= dt;
         return true;
      } else {
         return false;
      }
   }

   protected void pickNextObservationAngle() {
      if (this.pickRandomAngle) {
         float angle = RandomExtra.randomRange(-this.observationSector, this.observationSector);
         this.steeringForceRotate.setDesiredHeading(this.nodeViewDirection + angle);
      } else if (this.viewSegments > 1) {
         float fullSector = this.observationSector * 2.0F;
         float start = this.nodeViewDirection - this.observationSector;
         float segment = fullSector / (this.viewSegments - 1);
         int thisSegment = this.currentViewSegment++;
         this.currentViewSegment = this.currentViewSegment % this.viewSegments;
         this.steeringForceRotate.setDesiredHeading(start + thisSegment * segment);
      } else {
         this.steeringForceRotate.setDesiredHeading(this.nodeViewDirection + this.observationSector);
         this.observationSector *= -1.0F;
      }

      this.currentObservationDelay = this.nodeWaitTime * RandomExtra.randomRange(this.minDelayScale, this.maxDelayScale);
      this.currentObservationDelay = this.currentObservationDelay
         + this.currentObservationDelay * RandomExtra.randomRange(this.minPercentage, this.maxPercentage);
   }

   protected boolean closeToPosition(Vector3d position, @Nonnull MotionController motionController) {
      return motionController.waypointDistanceSquared(this.currentPosition, position) <= this.nodeWidth * this.nodeWidth;
   }

   protected void invalidateWaypoint() {
      this.currentWaypointIndex = -1;
      this.currentDirection = null;
   }

   protected boolean nextWayPoint(@Nonnull IPath<?> path, @Nonnull WorldSupport support, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.currentWaypointIndex == -1) {
         return false;
      } else {
         int numWaypoints = path.length();
         switch (this.shape) {
            case LINE:
            case LOOP:
            case CHAIN:
               if (this.direction == BodyMotionPath.Direction.RANDOM || this.currentDirection == null) {
                  this.currentDirection = RandomExtra.randomBoolean() ? BodyMotionPath.Direction.FORWARD : BodyMotionPath.Direction.BACKWARD;
               }

               this.currentWaypointIndex = this.currentWaypointIndex + (this.currentDirection == BodyMotionPath.Direction.FORWARD ? 1 : -1);
               if (this.currentWaypointIndex < 0 || this.currentWaypointIndex >= numWaypoints) {
                  if (this.shape == BodyMotionPath.Shape.LOOP) {
                     this.currentWaypointIndex = (this.currentWaypointIndex + numWaypoints) % numWaypoints;
                  } else if (this.currentWaypointIndex < 0) {
                     if (this.shape == BodyMotionPath.Shape.CHAIN) {
                        this.currentWaypointIndex = -1;
                        support.requestNewPath();
                        return false;
                     }

                     this.currentWaypointIndex = 1;
                     this.currentDirection = BodyMotionPath.Direction.FORWARD;
                  } else if (this.currentWaypointIndex >= numWaypoints) {
                     if (this.shape == BodyMotionPath.Shape.CHAIN) {
                        this.currentWaypointIndex = -1;
                        support.requestNewPath();
                        return false;
                     }

                     this.currentWaypointIndex = numWaypoints - 2;
                     this.currentDirection = BodyMotionPath.Direction.BACKWARD;
                  }
               }
               break;
            case POINTS:
               if (this.direction == BodyMotionPath.Direction.RANDOM) {
                  int index = RandomExtra.randomRange(numWaypoints);
                  if (index == this.currentWaypointIndex) {
                     index = (index + RandomExtra.randomRange(2) * 2 - 1 + numWaypoints) % numWaypoints;
                  }

                  this.currentWaypointIndex = index;
               } else {
                  this.visitIndex++;
                  if (this.visitIndex >= this.visitOrder.size()) {
                     this.visitIndex = 0;
                     IntLists.shuffle(this.visitOrder, ThreadLocalRandom.current());
                  }

                  if (this.visitOrder.getInt(this.visitIndex) == this.currentWaypointIndex) {
                     this.visitIndex++;
                  }

                  this.currentWaypointIndex = this.visitOrder.getInt(this.visitIndex);
               }

               this.currentDirection = BodyMotionPath.Direction.FORWARD;
         }

         this.waypointIndexUpdated(path, componentAccessor);
         return true;
      }
   }

   protected boolean getFirstWaypoint(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable IPath<?> path,
      @Nonnull Vector3d lastPos,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.invalidateWaypoint();
      if (path != null && path.length() != 0) {
         this.initializeCurrentDirection();
         if (this.startAtNearestNode) {
            double distanceSquared = Double.MAX_VALUE;
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d pos = transformComponent.getPosition();
            MotionController activeMotionController = role.getActiveMotionController();

            for (int i = 0; i < path.length(); i++) {
               IPathWaypoint pathWaypoint = path.get(i);
               if (pathWaypoint == null) {
                  return false;
               }

               double distance = activeMotionController.waypointDistanceSquared(pos, pathWaypoint.getWaypointPosition(componentAccessor));
               if (distance < distanceSquared) {
                  this.currentWaypointIndex = i;
                  distanceSquared = distance;
               }
            }

            switch (this.shape) {
               case LINE:
               case CHAIN:
                  if (this.currentWaypointIndex == 0) {
                     this.currentDirection = BodyMotionPath.Direction.FORWARD;
                  } else if (this.currentWaypointIndex == path.length() - 1) {
                     this.currentDirection = BodyMotionPath.Direction.BACKWARD;
                  }
               case LOOP:
               default:
                  break;
               case POINTS:
                  if (this.direction != BodyMotionPath.Direction.RANDOM) {
                     IntLists.shuffle(this.visitOrder, ThreadLocalRandom.current());
                     this.visitIndex = this.visitOrder.indexOf(this.currentWaypointIndex);
                  }
            }
         } else {
            switch (this.shape) {
               case LINE:
               case CHAIN:
                  this.currentWaypointIndex = this.currentDirection == BodyMotionPath.Direction.FORWARD ? 0 : path.length() - 1;
                  break;
               case LOOP:
                  this.currentWaypointIndex = 0;
                  break;
               case POINTS:
                  if (this.direction != BodyMotionPath.Direction.RANDOM) {
                     IntLists.shuffle(this.visitOrder, ThreadLocalRandom.current());
                     this.visitIndex = 0;
                     this.currentWaypointIndex = this.visitOrder.getInt(this.visitIndex);
                  } else {
                     this.currentWaypointIndex = RandomExtra.randomRange(path.length());
                  }
            }
         }

         this.waypointIndexUpdated(path, componentAccessor);
         this.lastWaypointPosition.assign(lastPos.getX(), lastPos.getY(), lastPos.getZ());
         return true;
      } else {
         return false;
      }
   }

   protected void waypointIndexUpdated(@Nonnull IPath<?> path, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      IPathWaypoint pathWaypoint = path.get(this.currentWaypointIndex);
      if (pathWaypoint != null) {
         Vector3d pathWaypointPosition = pathWaypoint.getWaypointPosition(componentAccessor);
         this.currentWaypointPosition.assign(pathWaypointPosition);
      }
   }

   protected void initializeCurrentDirection() {
      if (this.direction != BodyMotionPath.Direction.RANDOM && this.direction != BodyMotionPath.Direction.ANY) {
         this.currentDirection = this.direction;
      } else {
         this.currentDirection = RandomExtra.randomBoolean() ? BodyMotionPath.Direction.FORWARD : BodyMotionPath.Direction.BACKWARD;
      }
   }

   protected void reset() {
      this.pendingNodeDelay = false;
      this.currentNodeDelay = 0.0;
      this.nextPositionValid = false;
      this.currentViewSegment = 0;
      this.nodeWaitTime = 0.0;
      this.rotating = false;
   }

   public static enum Direction implements Supplier<String> {
      FORWARD("Start visiting nodes in order"),
      BACKWARD("Start visiting nodes in reverse order "),
      RANDOM("Can change direction between nodes and randomly pick target node in Points shape mode"),
      ANY("Pick any start direction");

      private final String description;

      private Direction(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }

   public static enum Shape implements Supplier<String> {
      LINE("Nodes form an open path of line segments"),
      LOOP("Nodes form a closed loop of line segments (last node leads to first node)"),
      POINTS("Any path between nodes is possible"),
      CHAIN("Nodes form an open path of line segments and will chain together with the next nearest path upon reaching the final node");

      private final String description;

      private Shape(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
