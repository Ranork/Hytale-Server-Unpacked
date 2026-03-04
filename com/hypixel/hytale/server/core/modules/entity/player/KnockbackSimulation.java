package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KnockbackSimulation implements Component<EntityStore> {
   public static final float KNOCKBACK_SIMULATION_TIME = 0.5F;
   public static final float BLEND_DELAY = 0.2F;
   private final Vector3d requestedVelocity = new Vector3d();
   private final Vector3d clientLastPosition = new Vector3d();
   private final Vector3d clientPosition = new Vector3d();
   private final Vector3d relativeMovement = new Vector3d();
   private final Vector3d simPosition = new Vector3d();
   private final Vector3d simVelocity = new Vector3d();
   @Nullable
   private ChangeVelocityType requestedVelocityChangeType = null;
   private MovementStates clientMovementStates;
   private float remainingTime = 0.5F;
   private boolean hadWishMovement = false;
   private boolean clientFinished = false;
   private boolean wasJumping = false;
   private int jumpCombo = 0;
   private boolean wasOnGround = false;
   private float tickBuffer = 0.0F;
   private final Vector3d movementOffset = new Vector3d();
   private final CollisionResult collisionResult = new CollisionResult();
   private final Vector3d checkPosition = new Vector3d();
   private final Vector3d tempPosition = new Vector3d();

   public static ComponentType<EntityStore, KnockbackSimulation> getComponentType() {
      return EntityModule.get().getKnockbackSimulationComponentType();
   }

   public float getTickBuffer() {
      return this.tickBuffer;
   }

   public void setTickBuffer(float tickBuffer) {
      this.tickBuffer = tickBuffer;
   }

   @Nonnull
   public Vector3d getRequestedVelocity() {
      return this.requestedVelocity;
   }

   public void addRequestedVelocity(@Nonnull Vector3d velocity) {
      if (this.requestedVelocityChangeType == null || this.requestedVelocityChangeType == ChangeVelocityType.Add) {
         this.requestedVelocityChangeType = ChangeVelocityType.Add;
      }

      this.requestedVelocity.add(velocity);
   }

   public void setRequestedVelocity(@Nonnull Vector3d velocity) {
      if (this.requestedVelocityChangeType == null || this.requestedVelocityChangeType == ChangeVelocityType.Add) {
         this.requestedVelocityChangeType = ChangeVelocityType.Set;
      }

      this.requestedVelocity.assign(velocity);
   }

   @Nullable
   public ChangeVelocityType getRequestedVelocityChangeType() {
      return this.requestedVelocityChangeType;
   }

   public void setRequestedVelocityChangeType(ChangeVelocityType requestedVelocityChangeType) {
      this.requestedVelocityChangeType = requestedVelocityChangeType;
   }

   @Nonnull
   public Vector3d getClientLastPosition() {
      return this.clientLastPosition;
   }

   @Nonnull
   public Vector3d getClientPosition() {
      return this.clientPosition;
   }

   @Nonnull
   public Vector3d getRelativeMovement() {
      return this.relativeMovement;
   }

   @Nonnull
   public Vector3d getSimPosition() {
      return this.simPosition;
   }

   @Nonnull
   public Vector3d getSimVelocity() {
      return this.simVelocity;
   }

   public float getRemainingTime() {
      return this.remainingTime;
   }

   public void setRemainingTime(float remainingTime) {
      this.remainingTime = remainingTime;
   }

   public void reset() {
      this.remainingTime = 0.5F;
   }

   public boolean consumeWasJumping() {
      boolean tmp = this.wasJumping;
      this.wasJumping = false;
      return tmp;
   }

   public void setWasJumping(boolean wasJumping) {
      this.wasJumping = wasJumping;
   }

   public boolean hadWishMovement() {
      return this.hadWishMovement;
   }

   public void setHadWishMovement(boolean hadWishMovement) {
      this.hadWishMovement = hadWishMovement;
   }

   public boolean isClientFinished() {
      return this.clientFinished;
   }

   public void setClientFinished(boolean clientFinished) {
      this.clientFinished = clientFinished;
   }

   public int getJumpCombo() {
      return this.jumpCombo;
   }

   public void setJumpCombo(int jumpCombo) {
      this.jumpCombo = jumpCombo;
   }

   public boolean wasOnGround() {
      return this.wasOnGround;
   }

   public void setWasOnGround(boolean wasOnGround) {
      this.wasOnGround = wasOnGround;
   }

   public MovementStates getClientMovementStates() {
      return this.clientMovementStates;
   }

   public void setClientMovementStates(MovementStates clientMovementStates) {
      this.clientMovementStates = clientMovementStates;
   }

   @Nonnull
   public Vector3d getMovementOffset() {
      return this.movementOffset;
   }

   @Nonnull
   public CollisionResult getCollisionResult() {
      return this.collisionResult;
   }

   @Nonnull
   public Vector3d getCheckPosition() {
      return this.checkPosition;
   }

   @Nonnull
   public Vector3d getTempPosition() {
      return this.tempPosition;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      KnockbackSimulation simulation = new KnockbackSimulation();
      simulation.requestedVelocity.assign(this.requestedVelocity);
      return simulation;
   }
}
