package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.HeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderHeadMotionAim;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.AimingData;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class HeadMotionAim extends HeadMotionBase implements DebugSupport.DebugFlagsChangeListener {
   public static final double MIN_RANGED_AIMING_DISTANCE = 4.0;
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
   protected final double spread;
   protected final boolean deflection;
   protected final double hitProbability;
   protected final double relativeTurnSpeed;
   protected final AimingData aimingData = new AimingData();
   protected Ref<EntityStore> lastTargetReference;
   protected boolean debugAiming;
   protected final Vector3d startPosition = new Vector3d();
   protected final Vector3d startOffset = new Vector3d();
   protected final Vector3d targetPosition = new Vector3d();
   protected final Vector3d targetOffset = new Vector3d();
   protected final Vector3d relativeVelocity = new Vector3d();
   protected final Vector3d spreadOffset = new Vector3d();

   public HeadMotionAim(@Nonnull BuilderHeadMotionAim builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.spread = builder.getSpread(support);
      this.hitProbability = builder.getHitProbability(support);
      this.deflection = builder.isDeflection(support);
      this.relativeTurnSpeed = builder.getRelativeTurnSpeed(support);
   }

   @Override
   public void preComputeSteering(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, @Nonnull Store<EntityStore> store) {
      if (sensorInfo != null) {
         sensorInfo.passExtraInfo(this.aimingData);
      }
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.aimingData.setHaveAttacked(true);
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role support,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (sensorInfo != null && sensorInfo.hasPosition() && sensorInfo.getPositionProvider() != null) {
         Transform lookVec = TargetUtil.getLook(ref, componentAccessor);
         Vector3d lookPosition = lookVec.getPosition();
         Rotation3f lookRotation = lookVec.getRotation();
         IPositionProvider positionProvider = sensorInfo.getPositionProvider();
         positionProvider.providePosition(this.targetPosition);
         this.startPosition.set(lookPosition);
         this.relativeVelocity.zero();
         Ref<EntityStore> targetRef = positionProvider.getTarget();
         BallisticData ballisticData = this.aimingData.getBallisticData();
         Box boundingBox = Box.ZERO;
         if (targetRef != null) {
            Velocity targetVelocityComponent = componentAccessor.getComponent(targetRef, Velocity.getComponentType());

            assert targetVelocityComponent != null;

            BoundingBox boundingBoxComponent = componentAccessor.getComponent(targetRef, BOUNDING_BOX_COMPONENT_TYPE);
            if (boundingBoxComponent != null) {
               boundingBox = boundingBoxComponent.getBoundingBox();
            }

            if (ballisticData != null) {
               if (this.deflection) {
                  this.relativeVelocity.set(targetVelocityComponent.getVelocity());
               }
            } else {
               double targetY = this.targetPosition.y();
               double startY = this.startPosition.y();
               double minY = targetY + boundingBox.getMin().y;
               double maxY = targetY + boundingBox.getMax().y;
               if (minY > startY) {
                  this.targetPosition.y = minY;
               } else if (maxY < startY) {
                  this.targetPosition.y = maxY;
               } else {
                  this.targetPosition.y = startY;
               }
            }
         }

         boolean isNearTarget = this.startPosition.distanceSquared(this.targetPosition) <= 16.0;
         if (ballisticData != null) {
            this.aimingData.setDepthOffset(ballisticData.getDepthShot(), ballisticData.isPitchAdjustShot());
            if (!isNearTarget) {
               ProjectileComponent.computeStartOffset(
                  ballisticData.isPitchAdjustShot(),
                  ballisticData.getVerticalCenterShot(),
                  ballisticData.getHorizontalCenterShot(),
                  ballisticData.getDepthShot(),
                  lookRotation.yaw(),
                  lookRotation.pitch(),
                  this.startOffset
               );
            } else {
               this.startOffset.zero();
            }

            if (targetRef != null && !targetRef.equals(this.lastTargetReference)) {
               this.lastTargetReference = targetRef;
               this.aimingData.setHaveAttacked(true);
            }

            if (this.aimingData.isHaveAttacked()) {
               ThreadLocalRandom random = ThreadLocalRandom.current();
               this.spreadOffset.zero();
               this.targetOffset.zero();
               if (this.spread > 0.0 && random.nextDouble() > this.hitProbability) {
                  double spread2 = 2.0 * this.spread * this.startPosition.distance(this.targetPosition) / 10.0;
                  this.spreadOffset.set(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).mul(spread2);
               } else {
                  double start = 0.1;
                  double end = 0.9;
                  this.targetOffset
                     .set(
                        NPCPhysicsMath.lerp(boundingBox.getMin().x, boundingBox.getMax().x, random.nextDouble(0.1, 0.9)),
                        NPCPhysicsMath.lerp(boundingBox.getMin().y, boundingBox.getMax().y, random.nextDouble(0.1, 0.9)),
                        NPCPhysicsMath.lerp(boundingBox.getMin().z, boundingBox.getMax().z, random.nextDouble(0.1, 0.9))
                     );
               }

               this.aimingData.setHaveAttacked(false);
            }

            this.targetPosition.add(this.spreadOffset);
            this.targetPosition.add(this.targetOffset);
            this.startPosition.add(this.startOffset);
         } else {
            this.aimingData.setDepthOffset(0.0, false);
         }

         double x = this.targetPosition.x() - this.startPosition.x();
         double y = this.targetPosition.y() - this.startPosition.y();
         double z = this.targetPosition.z() - this.startPosition.z();
         if (isNearTarget && ballisticData != null) {
            float yaw = lookRotation.yaw();
            float pitch = lookRotation.pitch();
            double dotXZ = x * x + z * z;
            if (dotXZ >= 1.0E-4) {
               yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
               double invLen = 1.0 / Math.sqrt(dotXZ);
               double hOffset = ballisticData.getHorizontalCenterShot();
               if (ballisticData.getDepthShot() != 0.0 && !ballisticData.isPitchAdjustShot()) {
                  hOffset += ballisticData.getDepthShot();
               }

               double dx = hOffset * x * invLen;
               double dy = -ballisticData.getVerticalCenterShot();
               double dz = -(hOffset * z * invLen);
               this.startPosition.add(dx, dy, dz);
               x -= dx;
               y -= dy;
               z -= dz;
            }

            double dotXYZ = dotXZ + y * y;
            if (dotXYZ >= 1.0E-4) {
               pitch = PhysicsMath.pitchFromDirection(x, y, z);
            }

            this.aimingData.setOrientation(yaw, pitch);
            this.aimingData.setTarget(targetRef);
         } else if (this.aimingData.computeSolution(x, y, z, this.relativeVelocity.x(), this.relativeVelocity.y(), this.relativeVelocity.z())) {
            this.aimingData.setTarget(targetRef);
         } else {
            double dotXZx = x * x + z * z;
            double dotXYZ = dotXZx + y * y;
            float yawx = dotXZx >= 1.0E-4 ? PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z)) : lookRotation.yaw();
            float pitchx = dotXYZ >= 1.0E-4 ? PhysicsMath.pitchFromDirection(x, y, z) : lookRotation.pitch();
            this.aimingData.setOrientation(yawx, pitchx);
            this.aimingData.setTarget(null);
         }

         if (this.debugAiming) {
            Vector3f color = DebugUtils.COLOR_WHITE;
            if (this.aimingData.haveOrientation()) {
               color = DebugUtils.COLOR_GREEN;
            }

            World world = ref.getStore().getExternalData().getWorld();
            DebugUtils.addSphere(world, this.targetPosition, color, 0.5, 0.1F);
            if (this.startPosition.distance(this.targetPosition) > 1.0E-4) {
               DebugUtils.addArrow(
                  world, this.startPosition, new Vector3d(this.targetPosition).sub(this.startPosition).normalize(), color, 0.1F, DebugUtils.FLAG_FADE
               );
            }
         }

         desiredSteering.clearTranslation();
         desiredSteering.setYaw(this.aimingData.getYaw());
         desiredSteering.setPitch(this.aimingData.getPitch());
         desiredSteering.setRelativeTurnSpeed(this.relativeTurnSpeed);
         return true;
      } else {
         desiredSteering.clear();
         return true;
      }
   }

   @Override
   public void registerWithSupport(Role role) {
      super.registerWithSupport(role);
      role.getDebugSupport().registerDebugFlagsListener(this);
   }

   @Override
   public void onDebugFlagsChanged(EnumSet<RoleDebugFlags> newFlags) {
      this.debugAiming = newFlags.contains(RoleDebugFlags.VisAiming);
   }
}
