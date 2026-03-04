package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.packets.entities.ApplyKnockback;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class KnockbackPredictionSystems {
   public static boolean DEBUG_KNOCKBACK_POSITION = false;
   public static final float DEFAULT_BLOCK_DRAG = 0.82F;
   public static final float AIR_DENSITY = 0.001225F;
   public static final float COLLISION_PADDING = 1.0E-4F;
   public static final float MAX_CYCLE_MOVEMENT = 0.25F;
   public static final float TIME_STEP = 0.016666668F;
   public static final int MAX_JUMP_COMBOS = 3;

   public static class CaptureKnockbackInput extends EntityTickingSystem<EntityStore> {
      private static final Query<EntityStore> QUERY = Query.and(PlayerInput.getComponentType(), KnockbackSimulation.getComponentType());
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.BEFORE, PlayerSystems.ProcessPlayerInput.class));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         KnockbackSimulation knockbackSimulationComponent = archetypeChunk.getComponent(index, KnockbackSimulation.getComponentType());

         assert knockbackSimulationComponent != null;

         PlayerInput playerInputComponent = archetypeChunk.getComponent(index, PlayerInput.getComponentType());

         assert playerInputComponent != null;

         List<PlayerInput.InputUpdate> queue = playerInputComponent.getMovementUpdateQueue();
         Vector3d client = knockbackSimulationComponent.getClientPosition();
         Vector3d clientLast = knockbackSimulationComponent.getClientLastPosition();
         Vector3d relativeMovement = knockbackSimulationComponent.getRelativeMovement();
         clientLast.assign(client);
         boolean hasWishMovement = false;
         if (!queue.isEmpty()) {
            for (int i = 0; i < queue.size(); i++) {
               PlayerInput.InputUpdate update = queue.get(i);
               if (update instanceof PlayerInput.AbsoluteMovement abs) {
                  client.assign(abs.getX(), abs.getY(), abs.getZ());
               } else if (update instanceof PlayerInput.RelativeMovement rel) {
                  client.add(rel.getX(), rel.getY(), rel.getZ());
               } else if (update instanceof PlayerInput.WishMovement wish) {
                  hasWishMovement = true;
                  relativeMovement.assign(wish.getX(), wish.getY(), wish.getZ());
               } else {
                  if (!(update instanceof PlayerInput.SetMovementStates)) {
                     continue;
                  }

                  MovementStates movementStates = ((PlayerInput.SetMovementStates)update).movementStates();
                  if (movementStates.jumping) {
                     knockbackSimulationComponent.setWasJumping(true);
                  }

                  knockbackSimulationComponent.setClientMovementStates(movementStates);
               }

               queue.remove(i);
               i--;
            }

            if (!hasWishMovement) {
               relativeMovement.assign(client).subtract(clientLast);
               if (knockbackSimulationComponent.hadWishMovement()) {
                  knockbackSimulationComponent.setClientFinished(true);
               }
            } else {
               knockbackSimulationComponent.setHadWishMovement(true);
            }
         }
      }
   }

   public static class ClearOnRemove extends RefSystem<EntityStore> {
      private static final ComponentType<EntityStore, KnockbackSimulation> KNOCKBACK_SIMULATION_COMPONENT_TYPE = KnockbackSimulation.getComponentType();

      @Override
      public Query<EntityStore> getQuery() {
         return KNOCKBACK_SIMULATION_COMPONENT_TYPE;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         store.removeComponent(ref, KNOCKBACK_SIMULATION_COMPONENT_TYPE);
      }
   }

   public static class ClearOnTeleport extends RefChangeSystem<EntityStore, Teleport> {
      private static final ComponentType<EntityStore, Teleport> TELEPORT_COMPONENT_TYPE = Teleport.getComponentType();
      private static final ComponentType<EntityStore, KnockbackSimulation> KNOCKBACK_SIMULATION_COMPONENT_TYPE = KnockbackSimulation.getComponentType();

      @Override
      public Query<EntityStore> getQuery() {
         return KNOCKBACK_SIMULATION_COMPONENT_TYPE;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Teleport> componentType() {
         return TELEPORT_COMPONENT_TYPE;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.removeComponent(ref, KNOCKBACK_SIMULATION_COMPONENT_TYPE);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Teleport oldComponent,
         @Nonnull Teleport newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   private static enum CollisionAxis {
      X,
      Y,
      Z;
   }

   public static class InitKnockback extends RefChangeSystem<EntityStore, KnockbackSimulation> {
      private static final Query<EntityStore> QUERY = Query.and(
         Player.getComponentType(), TransformComponent.getComponentType(), KnockbackSimulation.getComponentType(), MovementStatesComponent.getComponentType()
      );

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, KnockbackSimulation> componentType() {
         return KnockbackSimulation.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull KnockbackSimulation component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         component.getClientPosition().assign(transformComponent.getPosition());
         component.getSimPosition().assign(transformComponent.getPosition());
         MovementStatesComponent movementStatesComponent = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());

         assert movementStatesComponent != null;

         component.setClientMovementStates(new MovementStates(movementStatesComponent.getMovementStates()));
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         KnockbackSimulation oldComponent,
         @Nonnull KnockbackSimulation newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull KnockbackSimulation knockbackSimulationComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         Vector3d clientPosition = knockbackSimulationComponent.getClientPosition();
         playerComponent.moveTo(ref, clientPosition.x, clientPosition.y, clientPosition.z, commandBuffer);
         MovementStatesComponent movementStatesComponent = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());

         assert movementStatesComponent != null;

         movementStatesComponent.setMovementStates(knockbackSimulationComponent.getClientMovementStates());
      }
   }

   @Deprecated
   public static class SimulateKnockback extends EntityTickingSystem<EntityStore> {
      private static final ComponentType<EntityStore, BoundingBox> BOUNDING_BOX_COMPONENT_TYPE = BoundingBox.getComponentType();
      private static final Query<EntityStore> QUERY = Query.and(
         Player.getComponentType(),
         TransformComponent.getComponentType(),
         KnockbackSimulation.getComponentType(),
         BOUNDING_BOX_COMPONENT_TYPE,
         MovementStatesComponent.getComponentType(),
         MovementManager.getComponentType(),
         PlayerRef.getComponentType()
      );
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.AFTER, PlayerSystems.ProcessPlayerInput.class));

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         KnockbackSimulation knockbackSimulationComponent = archetypeChunk.getComponent(index, KnockbackSimulation.getComponentType());

         assert knockbackSimulationComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());

         assert playerComponent != null;

         World world = store.getExternalData().getWorld();
         float time = knockbackSimulationComponent.getRemainingTime();
         time -= dt;
         knockbackSimulationComponent.setRemainingTime(time);
         if (!(time < 0.0F) && !archetypeChunk.getArchetype().contains(DeathComponent.getComponentType())) {
            if (KnockbackPredictionSystems.DEBUG_KNOCKBACK_POSITION) {
               Vector3d particlePosition = knockbackSimulationComponent.getClientPosition();
               SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
               ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
               playerSpatialResource.getSpatialStructure().collect(particlePosition, 75.0, results);
               Color color = knockbackSimulationComponent.hadWishMovement() ? new Color((byte)-1, (byte)0, (byte)0) : new Color((byte)0, (byte)0, (byte)-1);
               ParticleUtil.spawnParticleEffect("Example_Simple", particlePosition, 0.0F, 0.0F, 0.0F, 1.0F, color, results, commandBuffer);
            }

            knockbackSimulationComponent.setTickBuffer(knockbackSimulationComponent.getTickBuffer() + dt);
            MovementStates clientStates = knockbackSimulationComponent.getClientMovementStates();

            while (knockbackSimulationComponent.getTickBuffer() >= 0.016666668F) {
               knockbackSimulationComponent.setTickBuffer(knockbackSimulationComponent.getTickBuffer() - 0.016666668F);
               Vector3d rel = knockbackSimulationComponent.getRelativeMovement();
               Vector3d requestedVelocity = knockbackSimulationComponent.getRequestedVelocity();
               Vector3d simPos = knockbackSimulationComponent.getSimPosition();
               Vector3d velocity = knockbackSimulationComponent.getSimVelocity();
               MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, MovementStatesComponent.getComponentType());

               assert movementStatesComponent != null;

               MovementStates movementStates = movementStatesComponent.getMovementStates();
               MovementManager movementManagerComponent = archetypeChunk.getComponent(index, MovementManager.getComponentType());

               assert movementManagerComponent != null;

               MovementSettings movementManagerSettings = movementManagerComponent.getSettings();
               BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, BOUNDING_BOX_COMPONENT_TYPE);

               assert boundingBoxComponent != null;

               Box hitBox = boundingBoxComponent.getBoundingBox();
               if (clientStates.flying) {
                  knockbackSimulationComponent.setRemainingTime(0.0F);
                  return;
               }

               if (clientStates.climbing && !clientStates.onGround) {
                  knockbackSimulationComponent.setRemainingTime(0.0F);
                  return;
               }

               if (clientStates.swimming) {
                  knockbackSimulationComponent.setRemainingTime(0.0F);
                  return;
               }

               int invertedGravityModifier = movementManagerSettings.invertedGravity ? 1 : -1;
               double terminalVelocity = invertedGravityModifier
                  * PhysicsMath.getTerminalVelocity(
                     movementManagerSettings.mass, 0.001225F, Math.abs(hitBox.width() * hitBox.depth()), movementManagerSettings.dragCoefficient
                  );
               double gravityStep = invertedGravityModifier * PhysicsMath.getAcceleration(velocity.y, terminalVelocity) * 0.016666668F;
               if (velocity.y < terminalVelocity && gravityStep > 0.0) {
                  velocity.y = Math.min(velocity.y + gravityStep, terminalVelocity);
               } else if (velocity.y > terminalVelocity && gravityStep < 0.0) {
                  velocity.y = Math.max(velocity.y + gravityStep, terminalVelocity);
               }

               if (movementStates.onGround) {
                  movementStates.falling = false;
                  if (knockbackSimulationComponent.wasOnGround() && knockbackSimulationComponent.consumeWasJumping()) {
                     velocity.y = movementManagerSettings.jumpForce;
                     movementStates.onGround = false;
                     knockbackSimulationComponent.setJumpCombo(Math.min(knockbackSimulationComponent.getJumpCombo() + 1, 3));
                  }
               } else {
                  Vector3d checkPosition = simPos.clone();
                  checkPosition.y += 0.1F;
                  movementStates.falling = velocity.y < 0.0 && CollisionModule.get().validatePosition(world, hitBox, checkPosition, new CollisionResult()) != 0;
                  if (movementStates.falling) {
                     movementStates.jumping = false;
                     movementStates.swimJumping = false;
                  }
               }

               if (knockbackSimulationComponent.getJumpCombo() != 0
                  && (movementStates.onGround && knockbackSimulationComponent.wasOnGround() || velocity.x == 0.0 || velocity.z == 0.0)) {
                  knockbackSimulationComponent.setJumpCombo(0);
               }

               float friction = this.computeMoveForce(knockbackSimulationComponent, movementStates, movementManagerSettings);
               if (!movementStates.flying && knockbackSimulationComponent.getRequestedVelocityChangeType() != null) {
                  switch (knockbackSimulationComponent.getRequestedVelocityChangeType()) {
                     case Add:
                        velocity.add(
                           requestedVelocity.x * 0.18F * movementManagerSettings.velocityResistance,
                           requestedVelocity.y,
                           requestedVelocity.z * 0.18F * movementManagerSettings.velocityResistance
                        );
                        break;
                     case Set:
                        velocity.assign(requestedVelocity);
                  }

                  PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  playerRefComponent.getPacketHandler()
                     .write(
                        new ApplyKnockback(
                           PositionUtil.toPositionPacket(transformComponent.getPosition()),
                           (float)requestedVelocity.x,
                           (float)requestedVelocity.y,
                           (float)requestedVelocity.z,
                           knockbackSimulationComponent.getRequestedVelocityChangeType()
                        )
                     );
               }

               requestedVelocity.assign(0.0);
               knockbackSimulationComponent.setRequestedVelocityChangeType(null);
               Vector3d movementOffset = knockbackSimulationComponent.getMovementOffset();
               movementOffset.assign(0.0);
               if (knockbackSimulationComponent.hadWishMovement()) {
                  float converter = this.convertWishMovement(knockbackSimulationComponent, movementStates, movementManagerSettings);
                  velocity.addScaled(rel, converter);
               } else {
                  movementOffset.addScaled(rel, friction);
                  rel.assign(0.0);
               }

               movementOffset.addScaled(velocity, 0.016666668F);
               this.applyMovementOffset(world, hitBox, knockbackSimulationComponent, movementStates, movementOffset);
               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               if (time < 0.2F) {
                  Vector3d move = Vector3d.lerp(knockbackSimulationComponent.getClientPosition(), simPos, time / 0.2F);
                  playerComponent.moveTo(ref, move.x, move.y, move.z, commandBuffer);
               } else {
                  playerComponent.moveTo(ref, simPos.x, simPos.y, simPos.z, commandBuffer);
               }
            }
         } else {
            commandBuffer.removeComponent(archetypeChunk.getReferenceTo(index), KnockbackSimulation.getComponentType());
         }
      }

      private float convertWishMovement(
         @Nonnull KnockbackSimulation simulation, @Nonnull MovementStates movementStates, @Nonnull MovementSettings movementSettings
      ) {
         MovementStates clientStates = simulation.getClientMovementStates();
         if (clientStates == null) {
            clientStates = movementStates;
         }

         Vector3d velocity = simulation.getSimVelocity();
         float horizontalSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         float drag = 0.0F;
         float friction = 1.0F;
         if (!movementStates.flying && !movementStates.climbing) {
            drag = !movementStates.onGround && !movementStates.swimming
               ? convertToNewRange(
                  horizontalSpeed, movementSettings.airDragMinSpeed, movementSettings.airDragMaxSpeed, movementSettings.airDragMin, movementSettings.airDragMax
               )
               : 0.82F;
            friction = !movementStates.onGround && !movementStates.swimming
               ? convertToNewRange(
                  horizontalSpeed,
                  movementSettings.airFrictionMinSpeed,
                  movementSettings.airFrictionMaxSpeed,
                  movementSettings.airFrictionMax,
                  movementSettings.airFrictionMin
               )
               : 1.0F - drag;
         }

         float clientDrag = 0.0F;
         float clientFriction = 1.0F;
         if (!clientStates.flying && !clientStates.climbing) {
            clientDrag = !clientStates.onGround && !clientStates.swimming
               ? convertToNewRange(
                  horizontalSpeed, movementSettings.airDragMinSpeed, movementSettings.airDragMaxSpeed, movementSettings.airDragMin, movementSettings.airDragMax
               )
               : 0.82F;
            clientFriction = !clientStates.onGround && !clientStates.swimming
               ? convertToNewRange(
                  horizontalSpeed,
                  movementSettings.airFrictionMinSpeed,
                  movementSettings.airFrictionMaxSpeed,
                  movementSettings.airFrictionMax,
                  movementSettings.airFrictionMin
               )
               : 1.0F - clientDrag;
         }

         return friction / clientFriction;
      }

      private float computeMoveForce(
         @Nonnull KnockbackSimulation simulation, @Nonnull MovementStates movementStates, @Nonnull MovementSettings movementSettings
      ) {
         float drag = 0.0F;
         float friction = 1.0F;
         Vector3d velocity = simulation.getSimVelocity();
         float horizontalSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         if (!movementStates.flying && !movementStates.climbing) {
            drag = !movementStates.onGround && !movementStates.swimming
               ? convertToNewRange(
                  horizontalSpeed, movementSettings.airDragMinSpeed, movementSettings.airDragMaxSpeed, movementSettings.airDragMin, movementSettings.airDragMax
               )
               : 0.82F;
            friction = !movementStates.onGround && !movementStates.swimming
               ? convertToNewRange(
                     horizontalSpeed,
                     movementSettings.airFrictionMinSpeed,
                     movementSettings.airFrictionMaxSpeed,
                     movementSettings.airFrictionMax,
                     movementSettings.airFrictionMin
                  )
                  / (1.0F - drag)
               : 1.0F;
         }

         velocity.x *= drag;
         velocity.z *= drag;
         return friction;
      }

      private static float convertToNewRange(float value, float oldMinRange, float oldMaxRange, float newMinRange, float newMaxRange) {
         if (newMinRange != newMaxRange && oldMinRange != oldMaxRange) {
            float newValue = (value - oldMinRange) * (newMaxRange - newMinRange) / (oldMaxRange - oldMinRange) + newMinRange;
            return MathUtil.clamp(newValue, Math.min(newMinRange, newMaxRange), Math.max(newMinRange, newMaxRange));
         } else {
            return newMinRange;
         }
      }

      public void applyMovementOffset(
         @Nonnull World world,
         @Nonnull Box hitBox,
         @Nonnull KnockbackSimulation simulation,
         @Nonnull MovementStates movementStates,
         @Nonnull Vector3d movementOffset
      ) {
         int moveCycles = (int)Math.ceil(movementOffset.length() / 0.25);
         Vector3d cycleMovementOffset = moveCycles == 1 ? movementOffset : movementOffset.clone().scale(1.0F / moveCycles);
         simulation.setWasOnGround(movementStates.onGround);

         for (int i = 0; i < moveCycles; i++) {
            this.doMoveCycle(world, hitBox, simulation, movementStates, cycleMovementOffset);
         }

         if (movementStates.onGround) {
            simulation.getSimVelocity().y = 0.0;
         }
      }

      private void doMoveCycle(
         @Nonnull World world, @Nonnull Box hitBox, @Nonnull KnockbackSimulation simulation, @Nonnull MovementStates movementStates, @Nonnull Vector3d offset
      ) {
         Vector3d simPos = simulation.getSimPosition();
         Vector3d velocity = simulation.getSimVelocity();
         Vector3d checkPosition = simulation.getCheckPosition();
         checkPosition.assign(simPos);
         CollisionResult collisionResult = simulation.getCollisionResult();
         collisionResult.reset();
         checkPosition.y = checkPosition.y + offset.y;
         boolean hasCollidedY = this.checkCollision(
            simulation, world, hitBox, checkPosition, offset, KnockbackPredictionSystems.CollisionAxis.Y, collisionResult
         );
         if (movementStates.onGround && offset.y < 0.0) {
            if (!hasCollidedY) {
               movementStates.onGround = false;
            } else {
               checkPosition.y = checkPosition.y - offset.y;
            }
         } else if (hasCollidedY) {
            if (offset.y <= 0.0) {
               movementStates.onGround = true;
               checkPosition.y = checkPosition.y - offset.y;
            } else {
               movementStates.onGround = false;
               checkPosition.y = checkPosition.y - offset.y;
            }

            velocity.y = 0.0;
         } else {
            movementStates.onGround = false;
         }

         if (offset.x != 0.0) {
            checkPosition.x = checkPosition.x + offset.x;
            collisionResult.reset();
            boolean hasCollidedX = this.checkCollision(
               simulation, world, hitBox, checkPosition, offset, KnockbackPredictionSystems.CollisionAxis.Y, collisionResult
            );
            if (hasCollidedX) {
               checkPosition.x = checkPosition.x - offset.x;
            }
         }

         if (offset.z != 0.0) {
            checkPosition.z = checkPosition.z + offset.z;
            collisionResult.reset();
            boolean hasCollidedZ = this.checkCollision(
               simulation, world, hitBox, checkPosition, offset, KnockbackPredictionSystems.CollisionAxis.Y, collisionResult
            );
            if (hasCollidedZ) {
               checkPosition.z = checkPosition.z - offset.z;
            }
         }

         simPos.assign(checkPosition);
      }

      private boolean checkCollision(
         @Nonnull KnockbackSimulation simulation,
         @Nonnull World world,
         @Nonnull Box hitBox,
         @Nonnull Vector3d position,
         Vector3d moveOffset,
         KnockbackPredictionSystems.CollisionAxis axis,
         @Nonnull CollisionResult result
      ) {
         Vector3d tempPosition = simulation.getTempPosition();
         tempPosition.assign(position);
         tempPosition.add(0.0, 1.0E-4F, 0.0);
         return CollisionModule.get().validatePosition(world, hitBox, tempPosition, result) != 0;
      }
   }
}
