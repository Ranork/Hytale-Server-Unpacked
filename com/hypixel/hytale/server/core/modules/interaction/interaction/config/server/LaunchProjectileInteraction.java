package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.gameplay.BrokenPenalties;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticData;
import com.hypixel.hytale.server.core.modules.projectile.config.BallisticDataProvider;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public class LaunchProjectileInteraction extends SimpleInstantInteraction implements BallisticDataProvider {
   @Nonnull
   public static final BuilderCodec<LaunchProjectileInteraction> CODEC = BuilderCodec.builder(
         LaunchProjectileInteraction.class, LaunchProjectileInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Launches a projectile.")
      .<String>appendInherited(
         new KeyedCodec<>("ProjectileId", Codec.STRING), (i, o) -> i.projectileId = o, i -> i.projectileId, (i, p) -> i.projectileId = p.projectileId
      )
      .addValidator(Validators.nonNull())
      .addValidator(Projectile.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String projectileId;

   public String getProjectileId() {
      return this.projectileId;
   }

   @Nullable
   @Override
   public BallisticData getBallisticData() {
      return Projectile.getAssetMap().getAsset(this.projectileId);
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

      assert commandBuffer != null;

      World world = commandBuffer.getExternalData().getWorld();
      Ref<EntityStore> sourceRef = context.getEntity();
      if (EntityUtils.getEntity(sourceRef, commandBuffer) instanceof LivingEntity attackerLivingEntity) {
         Transform lookVec = TargetUtil.getLook(sourceRef, commandBuffer);
         Vector3d lookPosition = lookVec.getPosition();
         Vector3f lookRotation = lookVec.getRotation();
         UUIDComponent sourceUuidComponent = commandBuffer.getComponent(sourceRef, UUIDComponent.getComponentType());
         if (sourceUuidComponent != null) {
            UUID sourceUuid = sourceUuidComponent.getUuid();
            TimeResource timeResource = commandBuffer.getResource(TimeResource.getResourceType());
            Holder<EntityStore> holder = ProjectileComponent.assembleDefaultProjectile(timeResource, this.projectileId, lookPosition, lookRotation);
            ProjectileComponent projectileComponent = holder.getComponent(ProjectileComponent.getComponentType());

            assert projectileComponent != null;

            holder.ensureComponent(Intangible.getComponentType());
            if (projectileComponent.getProjectile() == null) {
               projectileComponent.initialize();
               if (projectileComponent.getProjectile() == null) {
                  return;
               }
            }

            projectileComponent.shoot(
               holder, sourceUuid, lookPosition.getX(), lookPosition.getY(), lookPosition.getZ(), lookRotation.getYaw(), lookRotation.getPitch()
            );
            commandBuffer.addEntity(holder, AddReason.SPAWN);
            ItemStack itemInHand = context.getHeldItem();
            if (itemInHand != null && !itemInHand.isEmpty()) {
               Item item = itemInHand.getItem();
               if (attackerLivingEntity.canDecreaseItemStackDurability(sourceRef, commandBuffer) && !itemInHand.isUnbreakable() && item.getWeapon() != null) {
                  Inventory inventory = attackerLivingEntity.getInventory();
                  ItemContainer section = inventory.getSectionById(context.getHeldItemSectionId());
                  if (section != null) {
                     attackerLivingEntity.updateItemStackDurability(
                        sourceRef, itemInHand, section, context.getHeldItemSlot(), -item.getDurabilityLossOnHit(), commandBuffer
                     );
                  }
               }

               if (itemInHand.isBroken()) {
                  BrokenPenalties brokenPenalties = world.getGameplayConfig().getItemDurabilityConfig().getBrokenPenalties();
                  projectileComponent.applyBrokenPenalty((float)brokenPenalties.getWeapon(1.0));
               }
            }
         }
      }
   }

   @Override
   protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
   }
}
