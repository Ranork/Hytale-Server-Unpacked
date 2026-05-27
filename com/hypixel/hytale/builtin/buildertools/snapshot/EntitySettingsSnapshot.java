package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDynamicLight;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySettingsSnapshot implements EntitySnapshot<EntitySettingsSnapshot> {
   private static final int NO_COLLISION = -1;
   @Nonnull
   private Ref<EntityStore> ref;
   @Nullable
   private final ColorLight previousLight;
   private final boolean wasPickupEnabled;
   private final int previousCollisionConfigIndex;

   public EntitySettingsSnapshot(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.ref = ref;
      DynamicLight dynamicLight = componentAccessor.getComponent(ref, DynamicLight.getComponentType());
      this.previousLight = dynamicLight != null ? new ColorLight(dynamicLight.getColorLight()) : null;
      this.wasPickupEnabled = !componentAccessor.getArchetype(ref).contains(PreventPickup.getComponentType());
      HitboxCollision hitboxCollision = componentAccessor.getComponent(ref, HitboxCollision.getComponentType());
      this.previousCollisionConfigIndex = hitboxCollision != null ? hitboxCollision.getHitboxCollisionConfigIndex() : -1;
   }

   @Override
   public void updateEntityRef(@Nonnull Ref<EntityStore> oldRef, @Nonnull Ref<EntityStore> newRef) {
      if (this.ref == oldRef) {
         this.ref = newRef;
      }
   }

   public EntitySettingsSnapshot restoreEntity(@Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.ref.isValid()) {
         return null;
      } else {
         EntitySettingsSnapshot inverse = new EntitySettingsSnapshot(this.ref, componentAccessor);
         this.restoreLight(componentAccessor);
         this.restorePickup(componentAccessor);
         this.restoreCollision(componentAccessor);
         return inverse;
      }
   }

   private void restoreLight(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.previousLight == null) {
         componentAccessor.tryRemoveComponent(this.ref, DynamicLight.getComponentType());
         componentAccessor.tryRemoveComponent(this.ref, PersistentDynamicLight.getComponentType());
      } else {
         DynamicLight currentDynamicLight = componentAccessor.getComponent(this.ref, DynamicLight.getComponentType());
         if (currentDynamicLight != null) {
            currentDynamicLight.setColorLight(this.previousLight);
         } else {
            componentAccessor.addComponent(this.ref, DynamicLight.getComponentType(), new DynamicLight(this.previousLight));
         }

         PersistentDynamicLight persistentLight = componentAccessor.getComponent(this.ref, PersistentDynamicLight.getComponentType());
         if (persistentLight != null) {
            persistentLight.setColorLight(this.previousLight);
         } else {
            componentAccessor.addComponent(this.ref, PersistentDynamicLight.getComponentType(), new PersistentDynamicLight(this.previousLight));
         }
      }
   }

   private void restorePickup(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Archetype<EntityStore> archetype = componentAccessor.getArchetype(this.ref);
      if (this.wasPickupEnabled) {
         if (!archetype.contains(Interactable.getComponentType())) {
            componentAccessor.addComponent(this.ref, Interactable.getComponentType());
         }

         componentAccessor.tryRemoveComponent(this.ref, PreventPickup.getComponentType());
         Interactions interactions = componentAccessor.getComponent(this.ref, Interactions.getComponentType());
         if (interactions == null) {
            interactions = new Interactions();
            componentAccessor.addComponent(this.ref, Interactions.getComponentType(), interactions);
         }

         interactions.setInteractionId(InteractionType.Use, "*PickupItem");
         interactions.setInteractionHint("server.interactionHints.pickup");
      } else {
         componentAccessor.tryRemoveComponent(this.ref, Interactable.getComponentType());
         componentAccessor.tryRemoveComponent(this.ref, Interactions.getComponentType());
         if (!archetype.contains(PreventPickup.getComponentType())) {
            componentAccessor.addComponent(this.ref, PreventPickup.getComponentType());
         }
      }
   }

   public static void applyPickupState(@Nonnull Ref<EntityStore> ref, boolean enabled, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Archetype<EntityStore> archetype = componentAccessor.getArchetype(ref);
      if (enabled) {
         if (!archetype.contains(Interactable.getComponentType())) {
            componentAccessor.addComponent(ref, Interactable.getComponentType());
         }

         componentAccessor.tryRemoveComponent(ref, PreventPickup.getComponentType());
         Interactions interactions = componentAccessor.getComponent(ref, Interactions.getComponentType());
         if (interactions == null) {
            interactions = new Interactions();
            componentAccessor.addComponent(ref, Interactions.getComponentType(), interactions);
         }

         interactions.setInteractionId(InteractionType.Use, "*PickupItem");
         interactions.setInteractionHint("server.interactionHints.pickup");
      } else {
         componentAccessor.tryRemoveComponent(ref, Interactable.getComponentType());
         componentAccessor.tryRemoveComponent(ref, Interactions.getComponentType());
         if (!archetype.contains(PreventPickup.getComponentType())) {
            componentAccessor.addComponent(ref, PreventPickup.getComponentType());
         }
      }
   }

   private void restoreCollision(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.previousCollisionConfigIndex == -1) {
         componentAccessor.tryRemoveComponent(this.ref, HitboxCollision.getComponentType());
      } else {
         HitboxCollision currentHitbox = componentAccessor.getComponent(this.ref, HitboxCollision.getComponentType());
         if (currentHitbox != null) {
            currentHitbox.setHitboxCollisionConfigIndex(this.previousCollisionConfigIndex);
         } else {
            HitboxCollisionConfig config = HitboxCollisionConfig.getAssetMap().getAsset(this.previousCollisionConfigIndex);
            if (config != null) {
               componentAccessor.addComponent(this.ref, HitboxCollision.getComponentType(), new HitboxCollision(config));
            }
         }
      }
   }
}
