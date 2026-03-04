package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AppliedForce;
import com.hypixel.hytale.protocol.ApplyForceState;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.RaycastMode;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplyForceInteraction extends SimpleInteraction {
   @Nonnull
   public static final BuilderCodec<ApplyForceInteraction> CODEC = BuilderCodec.builder(
         ApplyForceInteraction.class, ApplyForceInteraction::new, SimpleInteraction.CODEC
      )
      .documentation("Applies a force to the user, optionally waiting for a condition to met before continuing.")
      .<Vector3d>appendInherited(new KeyedCodec<>("Direction", Vector3d.CODEC), (o, i) -> o.forces[0].direction = i.normalize(), o -> null, (o, p) -> {})
      .documentation("The direction of the force to apply.")
      .add()
      .<Boolean>appendInherited(new KeyedCodec<>("AdjustVertical", Codec.BOOLEAN), (o, i) -> o.forces[0].adjustVertical = i, o -> null, (o, p) -> {})
      .documentation("Whether the force should be adjusted based on the vertical look of the user.")
      .add()
      .<Double>appendInherited(new KeyedCodec<>("Force", Codec.DOUBLE), (o, i) -> o.forces[0].force = i, o -> null, (o, p) -> {})
      .documentation("The size of the force to apply.")
      .add()
      .<ApplyForceInteraction.Force[]>appendInherited(
         new KeyedCodec<>("Forces", new ArrayCodec<>(ApplyForceInteraction.Force.CODEC, ApplyForceInteraction.Force[]::new)),
         (o, i) -> o.forces = i,
         o -> o.forces,
         (o, p) -> o.forces = p.forces
      )
      .documentation("A collection of forces to apply to the user.\nReplaces `Direction`, `AdjustVertical` and `Force` if used.")
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Duration", Codec.FLOAT), (o, f) -> o.duration = f, o -> o.duration, (o, p) -> o.duration = p.duration)
      .addValidator(Validators.greaterThanOrEqual(0.0F))
      .documentation("The duration for which force should be continuously applied. If 0, force is applied on first run.")
      .add()
      .<FloatRange>appendInherited(
         new KeyedCodec<>("VerticalClamp", FloatRange.CODEC), (o, i) -> o.verticalClamp = i, o -> o.verticalClamp, (o, p) -> o.verticalClamp = p.verticalClamp
      )
      .documentation("The angles in degrees to clamp the look angle to when adjusting the force")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("WaitForGround", Codec.BOOLEAN), (o, i) -> o.waitForGround = i, o -> o.waitForGround, (o, p) -> o.waitForGround = p.waitForGround
      )
      .documentation("Determines whether or not on ground should be checked")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("WaitForCollision", Codec.BOOLEAN),
         (o, i) -> o.waitForCollision = i,
         o -> o.waitForCollision,
         (o, p) -> o.waitForCollision = p.waitForCollision
      )
      .documentation("Determines whether or not collision should be checked")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RaycastDistance", Codec.FLOAT),
         (o, i) -> o.raycastDistance = i,
         o -> o.raycastDistance,
         (o, p) -> o.raycastDistance = p.raycastDistance
      )
      .documentation("The distance of the raycast for the collision check")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("RaycastHeightOffset", Codec.FLOAT),
         (o, i) -> o.raycastHeightOffset = i,
         o -> o.raycastHeightOffset,
         (o, p) -> o.raycastHeightOffset = p.raycastHeightOffset
      )
      .documentation("The height offset of the raycast for the collision check (default 0)")
      .add()
      .<RaycastMode>appendInherited(
         new KeyedCodec<>("RaycastMode", new EnumCodec<>(RaycastMode.class)),
         (o, i) -> o.raycastMode = i,
         o -> o.raycastMode,
         (o, p) -> o.raycastMode = p.raycastMode
      )
      .documentation("The type of raycast performed for the collision check")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("GroundCheckDelay", Codec.FLOAT),
         (o, i) -> o.groundCheckDelay = i,
         o -> o.groundCheckDelay,
         (o, p) -> o.groundCheckDelay = p.groundCheckDelay
      )
      .documentation("The delay in seconds before checking if on ground")
      .add()
      .<Float>appendInherited(
         new KeyedCodec<>("CollisionCheckDelay", Codec.FLOAT),
         (o, i) -> o.collisionCheckDelay = i,
         o -> o.collisionCheckDelay,
         (o, p) -> o.collisionCheckDelay = p.collisionCheckDelay
      )
      .documentation("The delay in seconds before checking entity collision")
      .add()
      .<ChangeVelocityType>appendInherited(
         new KeyedCodec<>("ChangeVelocityType", ProtocolCodecs.CHANGE_VELOCITY_TYPE_CODEC),
         (o, i) -> o.changeVelocityType = i,
         o -> o.changeVelocityType,
         (o, p) -> o.changeVelocityType = p.changeVelocityType
      )
      .documentation("Configures how the velocity gets applied to the user.")
      .add()
      .<VelocityConfig>appendInherited(
         new KeyedCodec<>("VelocityConfig", VelocityConfig.CODEC),
         (o, i) -> o.velocityConfig = i,
         o -> o.velocityConfig,
         (o, p) -> o.velocityConfig = p.velocityConfig
      )
      .documentation("Specific configuration options that control how the velocity is affected.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("GroundNext", Interaction.CHILD_ASSET_CODEC),
         (interaction, s) -> interaction.groundInteraction = s,
         interaction -> interaction.groundInteraction,
         (interaction, parent) -> interaction.groundInteraction = parent.groundInteraction
      )
      .documentation("The interaction to run if on-ground is apparent.")
      .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CollisionNext", Interaction.CHILD_ASSET_CODEC),
         (interaction, s) -> interaction.collisionInteraction = s,
         interaction -> interaction.collisionInteraction,
         (interaction, parent) -> interaction.collisionInteraction = parent.collisionInteraction
      )
      .documentation("The interaction to run if collision is apparent.")
      .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   private static final int LABEL_COUNT = 3;
   private static final int NEXT_LABEL_INDEX = 0;
   private static final int GROUND_LABEL_INDEX = 1;
   private static final int COLLISION_LABEL_INDEX = 2;
   private static final float SPATIAL_STRUCTURE_RADIUS = 1.5F;
   @Nonnull
   private ChangeVelocityType changeVelocityType = ChangeVelocityType.Set;
   @Nonnull
   private ApplyForceInteraction.Force[] forces = new ApplyForceInteraction.Force[]{new ApplyForceInteraction.Force()};
   private float duration = 0.0F;
   private boolean waitForGround = true;
   private boolean waitForCollision = false;
   private float groundCheckDelay = 0.1F;
   private float collisionCheckDelay = 0.0F;
   private float raycastDistance = 1.5F;
   private float raycastHeightOffset = 0.0F;
   @Nonnull
   private RaycastMode raycastMode = RaycastMode.FollowMotion;
   @Nullable
   private VelocityConfig velocityConfig = null;
   @Nullable
   private FloatRange verticalClamp = null;
   @Nullable
   private String groundInteraction = null;
   @Nullable
   private String collisionInteraction = null;

   @Nonnull
   @Override
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   @Override
   protected void tick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      InteractionSyncData contextState = context.getState();
      if (firstRun) {
         contextState.state = InteractionState.NotFinished;
      } else {
         InteractionSyncData clientState = context.getClientState();

         assert clientState != null;

         ApplyForceState applyForceState = clientState.applyForceState;
         switch (applyForceState) {
            case Ground:
               contextState.state = InteractionState.Finished;
               context.jump(context.getLabel(1));
               break;
            case Collision:
               contextState.state = InteractionState.Finished;
               context.jump(context.getLabel(2));
               break;
            case Timer:
               contextState.state = InteractionState.Finished;
               context.jump(context.getLabel(0));
               break;
            default:
               contextState.state = InteractionState.NotFinished;
         }

         super.tick0(firstRun, time, type, context, cooldownHandler);
      }
   }

   @Override
   protected void simulateTick0(
      boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler
   ) {
      InteractionSyncData contextState = context.getState();
      Ref<EntityStore> entityRef = context.getEntity();
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      Store<EntityStore> entityStore = commandBuffer.getStore();
      if (!firstRun && (!(this.duration > 0.0F) || !(time < this.duration))) {
         MovementStatesComponent movementStatesComponent = entityStore.getComponent(entityRef, MovementStatesComponent.getComponentType());

         assert movementStatesComponent != null;

         TransformComponent transformComponent = entityStore.getComponent(entityRef, TransformComponent.getComponentType());

         assert transformComponent != null;

         MovementStates entityMovementStates = movementStatesComponent.getMovementStates();
         SpatialResource<Ref<EntityStore>, EntityStore> networkSendableSpatialComponent = entityStore.getResource(
            EntityModule.get().getNetworkSendableSpatialResourceType()
         );
         SpatialStructure<Ref<EntityStore>> spatialStructure = networkSendableSpatialComponent.getSpatialStructure();
         ObjectList<Ref<EntityStore>> entities = SpatialResource.getThreadLocalReferenceList();
         spatialStructure.collect(transformComponent.getPosition(), 1.5, entities);
         boolean checkGround = time >= this.groundCheckDelay;
         boolean onGround = checkGround
            && this.waitForGround
            && (entityMovementStates.onGround || entityMovementStates.inFluid || entityMovementStates.climbing);
         boolean checkCollision = time >= this.collisionCheckDelay;
         boolean collided = checkCollision && this.waitForCollision && entities.size() > 1;
         boolean instantlyComplete = this.runTime <= 0.0F && !this.waitForGround && !this.waitForCollision;
         boolean timerFinished = instantlyComplete || this.runTime > 0.0F && time >= this.runTime;
         contextState.applyForceState = ApplyForceState.Waiting;
         if (onGround) {
            contextState.applyForceState = ApplyForceState.Ground;
            contextState.state = InteractionState.Finished;
            context.jump(context.getLabel(1));
         } else if (collided) {
            contextState.applyForceState = ApplyForceState.Collision;
            contextState.state = InteractionState.Finished;
            context.jump(context.getLabel(2));
         } else if (timerFinished) {
            contextState.applyForceState = ApplyForceState.Timer;
            contextState.state = InteractionState.Finished;
            context.jump(context.getLabel(0));
         } else {
            contextState.state = InteractionState.NotFinished;
         }

         super.simulateTick0(firstRun, time, type, context, cooldownHandler);
      } else {
         HeadRotation headRotationComponent = entityStore.getComponent(entityRef, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Velocity velocityComponent = entityStore.getComponent(entityRef, Velocity.getComponentType());

         assert velocityComponent != null;

         Vector3f entityHeadRotation = headRotationComponent.getRotation();
         ChangeVelocityType velocityType = this.changeVelocityType;

         for (ApplyForceInteraction.Force force : this.forces) {
            Vector3d forceDirection = force.direction.clone();
            if (force.adjustVertical) {
               float lookX = entityHeadRotation.x;
               if (this.verticalClamp != null) {
                  lookX = MathUtil.clamp(
                     lookX, this.verticalClamp.getInclusiveMin() * (float) (Math.PI / 180.0), this.verticalClamp.getInclusiveMax() * (float) (Math.PI / 180.0)
                  );
               }

               forceDirection.rotateX(lookX);
            }

            forceDirection.scale(force.force);
            forceDirection.rotateY(entityHeadRotation.y);
            switch (velocityType) {
               case Add:
                  velocityComponent.addInstruction(forceDirection, null, ChangeVelocityType.Add);
                  break;
               case Set:
                  velocityComponent.addInstruction(forceDirection, null, ChangeVelocityType.Set);
            }

            velocityType = ChangeVelocityType.Add;
         }

         contextState.state = InteractionState.NotFinished;
      }
   }

   @Override
   public void compile(@Nonnull OperationsBuilder builder) {
      Label[] labels = new Label[3];

      for (int i = 0; i < labels.length; i++) {
         labels[i] = builder.createUnresolvedLabel();
      }

      builder.addOperation(this, labels);
      Label endLabel = builder.createUnresolvedLabel();
      resolve(builder, this.next, labels[0], endLabel);
      resolve(builder, this.groundInteraction == null ? this.next : this.groundInteraction, labels[1], endLabel);
      resolve(builder, this.collisionInteraction == null ? this.next : this.collisionInteraction, labels[2], endLabel);
      builder.resolveLabel(endLabel);
   }

   private static void resolve(@Nonnull OperationsBuilder builder, @Nullable String id, @Nonnull Label label, @Nonnull Label endLabel) {
      builder.resolveLabel(label);
      if (id != null) {
         Interaction interaction = Interaction.getInteractionOrUnknown(id);
         interaction.compile(builder);
      }

      builder.jump(endLabel);
   }

   @Override
   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   @Override
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ApplyForceInteraction();
   }

   @Override
   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ApplyForceInteraction p = (com.hypixel.hytale.protocol.ApplyForceInteraction)packet;
      p.changeVelocityType = this.changeVelocityType;
      p.forces = Arrays.stream(this.forces).map(ApplyForceInteraction.Force::toPacket).toArray(AppliedForce[]::new);
      p.duration = this.duration;
      p.waitForGround = this.waitForGround;
      p.waitForCollision = this.waitForCollision;
      p.groundCheckDelay = this.groundCheckDelay;
      p.collisionCheckDelay = this.collisionCheckDelay;
      p.velocityConfig = this.velocityConfig == null ? null : this.velocityConfig.toPacket();
      if (this.verticalClamp != null) {
         p.verticalClamp = new com.hypixel.hytale.protocol.FloatRange(
            this.verticalClamp.getInclusiveMin() * (float) (Math.PI / 180.0), this.verticalClamp.getInclusiveMax() * (float) (Math.PI / 180.0)
         );
      }

      p.collisionNext = Interaction.getInteractionIdOrUnknown(this.collisionInteraction == null ? this.next : this.collisionInteraction);
      p.groundNext = Interaction.getInteractionIdOrUnknown(this.groundInteraction == null ? this.next : this.groundInteraction);
      p.raycastDistance = this.raycastDistance;
      p.raycastHeightOffset = this.raycastHeightOffset;
      p.raycastMode = this.raycastMode;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ApplyForceInteraction{changeVelocityType=" + this.changeVelocityType + ", waitForGround=" + this.waitForGround + "} " + super.toString();
   }

   public static class Force implements NetworkSerializable<AppliedForce> {
      public static final BuilderCodec<ApplyForceInteraction.Force> CODEC = BuilderCodec.builder(
            ApplyForceInteraction.Force.class, ApplyForceInteraction.Force::new
         )
         .appendInherited(new KeyedCodec<>("Direction", Vector3d.CODEC), (o, i) -> o.direction = i, o -> o.direction, (o, p) -> o.direction = p.direction)
         .documentation("The direction of the force to apply.")
         .addValidator(Validators.nonNull())
         .add()
         .<Boolean>appendInherited(
            new KeyedCodec<>("AdjustVertical", Codec.BOOLEAN),
            (o, i) -> o.adjustVertical = i,
            o -> o.adjustVertical,
            (o, p) -> o.adjustVertical = p.adjustVertical
         )
         .documentation("Whether the force should be adjusted based on the vertical look of the user.")
         .add()
         .<Double>appendInherited(new KeyedCodec<>("Force", Codec.DOUBLE), (o, i) -> o.force = i, o -> o.force, (o, p) -> o.force = p.force)
         .documentation("The size of the force to apply.")
         .add()
         .afterDecode(o -> o.direction.normalize())
         .build();
      @Nonnull
      private Vector3d direction = Vector3d.UP;
      private boolean adjustVertical = false;
      private double force = 1.0;

      @Nonnull
      public AppliedForce toPacket() {
         return new AppliedForce(
            new com.hypixel.hytale.protocol.Vector3f((float)this.direction.x, (float)this.direction.y, (float)this.direction.z),
            this.adjustVertical,
            (float)this.force
         );
      }
   }
}
