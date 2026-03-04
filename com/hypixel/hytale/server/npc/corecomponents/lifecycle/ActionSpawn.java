package com.hypixel.hytale.server.npc.corecomponents.lifecycle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionSpawn;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerFly;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.systems.NewSpawnStartTickingSystem;
import com.hypixel.hytale.server.npc.util.AimingHelper;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.Pair;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSpawn extends ActionBase {
   protected final float spawnDirection;
   protected final float spawnAngle;
   protected final boolean fanOut;
   protected final double minDistance;
   protected final double maxDistance;
   protected final String kind;
   protected final String flock;
   protected final int roleIndex;
   protected final int maxCount;
   protected final int minCount;
   protected final double minDelay;
   protected final double maxDelay;
   protected final Vector3d position = new Vector3d();
   protected final Vector3f rotation = new Vector3f();
   protected final boolean launchAtTarget;
   protected final boolean pitchHigh;
   protected final Vector3d targetPosition = new Vector3d();
   protected final Vector3d launchDirection = new Vector3d();
   @Nullable
   protected final float[] pitch;
   protected final double spread;
   protected final boolean joinFlock;
   protected final String spawnState;
   protected final String spawnSubState;
   protected int spawnsLeft;
   protected int maxTries;
   protected float yaw0;
   protected float yawIncrement;
   protected double spawnDelay;
   protected Ref<EntityStore> parent;

   public ActionSpawn(@Nonnull BuilderActionSpawn builderActionSpawn, @Nonnull BuilderSupport builderSupport) {
      super(builderActionSpawn);
      this.spawnDirection = builderActionSpawn.getSpawnDirection(builderSupport);
      this.spawnAngle = builderActionSpawn.getSpawnAngle(builderSupport);
      this.fanOut = builderActionSpawn.isFanOut(builderSupport);
      double[] distanceRange = builderActionSpawn.getDistanceRange(builderSupport);
      this.minDistance = distanceRange[0];
      this.maxDistance = distanceRange[1];
      int[] countRange = builderActionSpawn.getCountRange(builderSupport);
      this.minCount = countRange[0];
      this.maxCount = countRange[1];
      double[] delayRange = builderActionSpawn.getDelayRange(builderSupport);
      this.minDelay = delayRange[0];
      this.maxDelay = delayRange[1];
      this.kind = builderActionSpawn.getKind(builderSupport);
      this.flock = builderActionSpawn.getFlock(builderSupport);
      this.roleIndex = NPCPlugin.get().getIndex(this.kind);
      this.launchAtTarget = builderActionSpawn.isLaunchAtTarget(builderSupport);
      this.pitchHigh = builderActionSpawn.isPitchHigh(builderSupport);
      this.spread = builderActionSpawn.getSpread(builderSupport);
      this.joinFlock = builderActionSpawn.isJoinFlock(builderSupport);
      this.pitch = this.launchAtTarget ? new float[2] : null;
      this.spawnState = builderActionSpawn.getSpawnState(builderSupport);
      this.spawnSubState = builderActionSpawn.getSpawnSubState(builderSupport);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (super.canExecute(ref, role, sensorInfo, dt, store) && this.roleIndex >= 0 && this.spawnsLeft <= 0) {
         if (NPCPlugin.get().tryGetCachedValidRole(this.roleIndex) == null) {
            NPCPlugin.get()
               .getLogger()
               .at(Level.SEVERE)
               .log("NPC of type '%s': Unable to spawn NPC of type '%s' from Action Spawn", role.getRoleName(), this.kind);
            this.setOnce();
            this.once = true;
            return false;
         } else {
            return !this.launchAtTarget ? true : sensorInfo != null && sensorInfo.hasPosition();
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      this.spawnsLeft = RandomExtra.randomRange(this.minCount, this.maxCount);
      if (this.spawnsLeft == 0) {
         return true;
      } else {
         this.maxTries = this.spawnsLeft * 5;
         if (this.launchAtTarget) {
            sensorInfo.getPositionProvider().providePosition(this.targetPosition);
         } else if (this.fanOut) {
            if (this.spawnsLeft == 1) {
               this.yaw0 = this.spawnDirection;
            } else {
               this.yaw0 = this.spawnDirection - this.spawnAngle / 2.0F;
               this.yawIncrement = this.spawnAngle / (this.spawnsLeft - 1);
            }
         } else {
            this.yaw0 = this.spawnDirection - this.spawnAngle / 2.0F;
         }

         SpawningContext spawningContext = new SpawningContext();
         this.parent = ref;
         Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(this.roleIndex);
         if ((roleBuilder.isSpawnable() || roleBuilder instanceof ISpawnableWithModel) && spawningContext.setSpawnable((ISpawnableWithModel)roleBuilder)) {
            while (this.spawnsLeft > 0) {
               if (this.trySpawn(ref, spawningContext, store)) {
                  if (--this.spawnsLeft == 0) {
                     return true;
                  }

                  if (this.minDelay > 0.0 || this.maxDelay > 0.0) {
                     break;
                  }

                  spawningContext.newModel();
               }

               if (--this.maxTries == 0) {
                  this.spawnsLeft = 0;
                  return true;
               }
            }

            this.spawnDelay = RandomExtra.randomRange(this.minDelay, this.maxDelay);
            role.addDeferredAction(this::deferredSpawning);
            return true;
         } else {
            this.spawnsLeft = 0;
            return true;
         }
      }
   }

   protected boolean trySpawn(@Nonnull Ref<EntityStore> ref, @Nonnull SpawningContext spawningContext, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
      Vector3d position = transformComponent.getPosition();
      Vector3f bodyRotation = transformComponent.getRotation();
      double x;
      double z;
      double y;
      if (this.launchAtTarget) {
         float eyeHeight = modelComponent != null ? modelComponent.getModel().getEyeHeight() : 0.0F;
         x = position.getX();
         z = position.getZ();
         y = position.getY() + eyeHeight;
      } else {
         double distance = RandomExtra.randomRange(this.minDistance, this.maxDistance);
         float yaw = bodyRotation.getYaw() + this.yaw0;
         if (!this.fanOut) {
            yaw += RandomExtra.randomRange(0.0F, this.spawnAngle);
         }

         x = position.getX() + PhysicsMath.headingX(yaw) * distance;
         z = position.getZ() + PhysicsMath.headingZ(yaw) * distance;
         y = position.getY();
      }

      if (spawningContext.set(world, x, y, z) && spawningContext.canSpawn() == SpawnTestResult.TEST_OK) {
         this.position.assign(spawningContext.xSpawn, spawningContext.ySpawn, spawningContext.zSpawn);
         this.rotation.assign(bodyRotation);
         FlockAsset flockDefinition = this.flock != null ? FlockAsset.getAssetMap().getAsset(this.flock) : null;
         Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get()
            .spawnEntity(store, this.roleIndex, this.position, this.rotation, spawningContext.getModel(), this::postSpawn);
         FlockPlugin.trySpawnFlock(
            (Ref<EntityStore>)npcPair.first(),
            (NPCEntity)npcPair.second(),
            store,
            this.roleIndex,
            this.position,
            this.rotation,
            flockDefinition,
            this::postSpawn
         );
         if (this.fanOut) {
            this.yaw0 = this.yaw0 + this.yawIncrement;
         }

         return true;
      } else {
         return false;
      }
   }

   protected void postSpawn(@Nonnull NPCEntity npcComponent, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      NPCEntity parentNpcComponent = store.getComponent(this.parent, NPCEntity.getComponentType());

      assert parentNpcComponent != null;

      this.joinFlock(ref, store);
      this.launchAtTarget(ref, store);
      if (this.spawnState != null) {
         npcComponent.getRole().getStateSupport().setState(ref, this.spawnState, this.spawnSubState, store);
      }

      npcComponent.setSpawnConfiguration(parentNpcComponent.getSpawnConfiguration());
      NewSpawnStartTickingSystem.queueNewSpawn(ref, store);
   }

   protected void joinFlock(@Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store) {
      if (this.joinFlock) {
         NPCEntity parentNpcComponent = store.getComponent(this.parent, NPCEntity.getComponentType());

         assert parentNpcComponent != null;

         Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(this.parent, store);
         if (flockReference == null) {
            flockReference = FlockPlugin.createFlock(store, parentNpcComponent.getRole());
            FlockMembershipSystems.join(this.parent, flockReference, store);
         }

         FlockMembershipSystems.join(targetRef, flockReference, store);
      }
   }

   protected void launchAtTarget(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      if (this.launchAtTarget) {
         if (this.spread > 0.0) {
            this.targetPosition.add(RandomExtra.randomRange(-this.spread, this.spread), 0.0, RandomExtra.randomRange(-this.spread, this.spread));
         }

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         Role role = npcComponent.getRole();
         Vector3d position = transformComponent.getPosition();
         this.launchDirection.assign(this.targetPosition).subtract(position).normalize();
         double distance = position.distanceTo(this.targetPosition);
         if (role.getActiveMotionController() instanceof MotionControllerFly flyController) {
            double endVelocity = flyController.getMinSpeedAfterForceSquared();
            double acceleration = -flyController.getDampingDeceleration();
            double v0 = Math.sqrt(endVelocity - 2.0 * acceleration * distance);
            this.launchDirection.scale(v0);
            role.forceVelocity(this.launchDirection, null, false);
         } else {
            double height = this.targetPosition.y - position.y;
            double gravity = role.getActiveMotionController().getGravity() * 5.0;
            double throwSpeed = AimingHelper.ensurePossibleThrowSpeed(distance, height, gravity, 1.0);
            if (!AimingHelper.computePitch(distance, height, throwSpeed, gravity, this.pitch)) {
               throw new IllegalStateException(
                  String.format(
                     "Error in computing pitch with distance %s, height %s, and speed %s despite ensuring possible throw speed", distance, height, throwSpeed
                  )
               );
            } else {
               float heading = PhysicsMath.headingFromDirection(this.launchDirection.x, this.launchDirection.z);
               PhysicsMath.vectorFromAngles(heading, this.pitchHigh ? this.pitch[1] : this.pitch[0], this.launchDirection).normalize();
               this.launchDirection.scale(throwSpeed);
               role.forceVelocity(this.launchDirection, null, true);
            }
         }
      }
   }

   protected boolean deferredSpawning(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      this.spawnDelay -= dt;
      if (this.spawnDelay > 0.0) {
         return false;
      } else {
         this.spawnDelay = RandomExtra.randomRange(this.minDelay, this.maxDelay);
         SpawningContext spawningContext = new SpawningContext();
         Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(this.roleIndex);
         if (roleBuilder.isSpawnable() && roleBuilder instanceof ISpawnableWithModel && spawningContext.setSpawnable((ISpawnableWithModel)roleBuilder)) {
            while (!this.trySpawn(ref, spawningContext, store)) {
               this.maxTries--;
               if (this.maxTries <= 0) {
                  this.spawnsLeft = 0;
                  return true;
               }
            }

            this.spawnsLeft--;
            return this.spawnsLeft == 0;
         } else {
            return true;
         }
      }
   }
}
