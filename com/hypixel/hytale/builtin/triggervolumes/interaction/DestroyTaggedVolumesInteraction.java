package com.hypixel.hytale.builtin.triggervolumes.interaction;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector4d;

public class DestroyTaggedVolumesInteraction extends SimpleInstantInteraction {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<DestroyTaggedVolumesInteraction> CODEC = BuilderCodec.builder(
         DestroyTaggedVolumesInteraction.class, DestroyTaggedVolumesInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Destroys all trigger volumes with a given tag within a radius of the target point.")
      .appendInherited(
         new KeyedCodec<>("MatchKey", Codec.STRING),
         (interaction, matchKey) -> interaction.matchKey = matchKey,
         interaction -> interaction.matchKey,
         (interaction, parent) -> interaction.matchKey = parent.matchKey
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MatchValue", Codec.STRING, false),
         (interaction, matchValue) -> interaction.matchValue = matchValue,
         interaction -> interaction.matchValue,
         (interaction, parent) -> interaction.matchValue = parent.matchValue
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Radius", Codec.DOUBLE, false),
         (interaction, radius) -> interaction.radius = radius,
         interaction -> interaction.radius,
         (interaction, parent) -> interaction.radius = parent.radius
      )
      .add()
      .build();
   private String matchKey;
   private String matchValue;
   private double radius = 50.0;

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Vector4d hitLocation = context.getMetaStore().getIfPresentMetaObject(Interaction.HIT_LOCATION);
      Vector3d center;
      if (hitLocation != null) {
         center = new Vector3d(hitLocation.x(), hitLocation.y(), hitLocation.z());
      } else {
         Ref<EntityStore> owningRef = context.getOwningEntity();
         TransformComponent transform = owningRef.getStore().getComponent(owningRef, TransformComponent.getComponentType());
         if (transform == null) {
            return;
         }

         center = new Vector3d(transform.getPosition());
      }

      String tagFilter = TaggedVolumeEffectUtil.composeTagFilter(this.matchKey, this.matchValue);
      if (tagFilter != null) {
         int tagIndex = AssetRegistry.getTagIndex(tagFilter);
         if (tagIndex == Integer.MIN_VALUE) {
            LOGGER.at(Level.WARNING).log("DestroyTaggedVolumes: unknown tag '%s'", tagFilter);
         } else {
            TriggerVolumesPlugin plugin = TriggerVolumesPlugin.get();
            Store<EntityStore> store = context.getOwningEntity().getStore();
            TriggerVolumeManager manager = store.getResource(plugin.getManagerResourceType());
            if (manager != null) {
               double radiusSq = this.radius * this.radius;
               ArrayList<VolumeEntry> toDestroy = new ArrayList<>();

               for (VolumeEntry entry : manager.getVolumesByTag(tagIndex)) {
                  if (this.radius <= 0.0 || entry.getPosition().distanceSquared(center) <= radiusSq) {
                     toDestroy.add(entry);
                  }
               }

               for (VolumeEntry entryx : toDestroy) {
                  manager.unregister(entryx.getId());
                  manager.notifyViewersRemove(entryx.getId());
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DestroyTaggedVolumesInteraction{matchKey="
         + this.matchKey
         + ", matchValue="
         + this.matchValue
         + ", radius="
         + this.radius
         + "} "
         + super.toString();
   }
}
