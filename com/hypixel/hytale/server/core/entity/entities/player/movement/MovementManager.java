package com.hypixel.hytale.server.core.entity.entities.player.movement;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.packets.player.UpdateMovementSettings;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;

public class MovementManager implements Component<EntityStore> {
   public static final BiFunction<PhysicsValues, GameMode, MovementSettings> MASTER_DEFAULT = (physicsValues, gameMode) -> new MovementSettings() {
      {
         this.velocityResistance = 0.242F;
         this.mass = (float)physicsValues.getMass();
         this.dragCoefficient = (float)physicsValues.getDragCoefficient();
         this.invertedGravity = physicsValues.isInvertedGravity();
         this.jumpForce = 11.8F;
         this.swimJumpForce = 10.0F;
         this.jumpBufferDuration = 0.3F;
         this.jumpBufferMaxYVelocity = 3.0F;
         this.acceleration = 0.1F;
         this.airDragMin = 0.96F;
         this.airDragMax = 0.995F;
         this.airDragMinSpeed = 6.0F;
         this.airDragMaxSpeed = 10.0F;
         this.airFrictionMin = 0.02F;
         this.airFrictionMax = 0.045F;
         this.airFrictionMinSpeed = 6.0F;
         this.airFrictionMaxSpeed = 10.0F;
         this.airSpeedMultiplier = 1.0F;
         this.airControlMinSpeed = 0.0F;
         this.airControlMaxSpeed = 3.0F;
         this.airControlMinMultiplier = 0.0F;
         this.airControlMaxMultiplier = 3.13F;
         this.comboAirSpeedMultiplier = 1.05F;
         this.baseSpeed = 5.5F;
         this.horizontalFlySpeed = 10.32F;
         this.verticalFlySpeed = 10.32F;
         this.climbSpeed = 0.035F;
         this.climbSpeedLateral = 0.035F;
         this.climbUpSprintSpeed = 0.045F;
         this.climbDownSprintSpeed = 0.055F;
         this.wishDirectionGravityX = 0.5F;
         this.wishDirectionGravityY = 0.5F;
         this.wishDirectionWeightX = 0.5F;
         this.wishDirectionWeightY = 0.5F;
         this.maxSpeedMultiplier = 1000.0F;
         this.minSpeedMultiplier = 0.1F;
         this.canFly = gameMode == GameMode.Creative;
         this.collisionExpulsionForce = 0.04F;
         this.forwardWalkSpeedMultiplier = 0.3F;
         this.backwardWalkSpeedMultiplier = 0.3F;
         this.strafeWalkSpeedMultiplier = 0.3F;
         this.forwardRunSpeedMultiplier = 1.0F;
         this.backwardRunSpeedMultiplier = 0.65F;
         this.strafeRunSpeedMultiplier = 0.8F;
         this.forwardCrouchSpeedMultiplier = 0.55F;
         this.backwardCrouchSpeedMultiplier = 0.4F;
         this.strafeCrouchSpeedMultiplier = 0.45F;
         this.forwardSprintSpeedMultiplier = 1.65F;
         this.variableJumpFallForce = 35.0F;
         this.fallEffectDuration = 0.6F;
         this.fallJumpForce = 7.0F;
         this.fallMomentumLoss = 0.1F;
         this.autoJumpObstacleEffectDuration = 0.2F;
         this.autoJumpObstacleSpeedLoss = 0.95F;
         this.autoJumpObstacleSprintSpeedLoss = 0.75F;
         this.autoJumpObstacleSprintEffectDuration = 0.1F;
         this.autoJumpObstacleMaxAngle = 45.0F;
         this.autoJumpDisableJumping = true;
         this.minSlideEntrySpeed = 8.5F;
         this.slideExitSpeed = 2.5F;
         this.minFallSpeedToEngageRoll = 21.0F;
         this.maxFallSpeedToEngageRoll = 31.0F;
         this.rollStartSpeedModifier = 2.5F;
         this.rollExitSpeedModifier = 1.5F;
         this.rollTimeToComplete = 0.9F;
      }
   };
   protected MovementSettings defaultSettings;
   protected MovementSettings settings;

   public static ComponentType<EntityStore, MovementManager> getComponentType() {
      return EntityModule.get().getMovementManagerComponentType();
   }

   public MovementManager() {
   }

   public MovementManager(@Nonnull MovementManager other) {
      this();
      this.defaultSettings = new MovementSettings(other.defaultSettings);
      this.settings = new MovementSettings(other.settings);
   }

   public void resetDefaultsAndUpdate(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.refreshDefaultSettings(ref, componentAccessor);
      this.applyDefaultSettings();
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      this.update(playerRefComponent.getPacketHandler());
   }

   public void refreshDefaultSettings(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      int movementConfigIndex = world.getGameplayConfig().getPlayerConfig().getMovementConfigIndex();
      MovementConfig movementConfig = (MovementConfig)((IndexedLookupTableAssetMap)MovementConfig.getAssetStore().getAssetMap()).getAsset(movementConfigIndex);
      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      this.setDefaultSettings(movementConfig.toPacket(), EntityUtils.getPhysicsValues(ref, componentAccessor), playerComponent.getGameMode());
   }

   public void applyDefaultSettings() {
      this.settings = new MovementSettings(this.defaultSettings);
   }

   public void update(@Nonnull PacketHandler playerPacketHandler) {
      playerPacketHandler.writeNoCache(new UpdateMovementSettings(this.getSettings()));
   }

   public MovementSettings getSettings() {
      return this.settings;
   }

   public void setDefaultSettings(MovementSettings settings, @Nonnull PhysicsValues physicsValues, GameMode gameMode) {
      this.defaultSettings = settings;
      this.defaultSettings.mass = (float)physicsValues.getMass();
      this.defaultSettings.dragCoefficient = (float)physicsValues.getDragCoefficient();
      this.defaultSettings.invertedGravity = physicsValues.isInvertedGravity();
      this.defaultSettings.canFly = gameMode == GameMode.Creative;
   }

   public MovementSettings getDefaultSettings() {
      return this.defaultSettings;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MovementManager{defaultSettings=" + this.defaultSettings + ", settings=" + this.settings + "}";
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new MovementManager(this);
   }
}
