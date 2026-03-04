package com.hypixel.hytale.server.core.entity.reference;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PersistentRef {
   public static final BuilderCodec<PersistentRef> CODEC = BuilderCodec.builder(PersistentRef.class, PersistentRef::new)
      .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY), (instance, value) -> instance.uuid = value, instance -> instance.uuid)
      .add()
      .build();
   @Nullable
   protected UUID uuid;
   @Nullable
   protected Ref<EntityStore> reference;

   @Nullable
   public UUID getUuid() {
      return this.uuid;
   }

   public void setUuid(UUID uuid) {
      this.uuid = uuid;
      this.reference = null;
   }

   public void setEntity(Ref<EntityStore> ref, UUID uuid) {
      this.uuid = uuid;
      this.reference = ref;
   }

   public void setEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.uuid = componentAccessor.getComponent(ref, UUIDComponent.getComponentType()).getUuid();
      this.reference = ref;
   }

   public void clear() {
      this.uuid = null;
      this.reference = null;
   }

   public boolean isValid() {
      return this.uuid != null;
   }

   @Nullable
   public Ref<EntityStore> getEntity(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.isValid()) {
         return null;
      } else if (this.reference != null && this.reference.isValid()) {
         return this.reference;
      } else {
         this.reference = componentAccessor.getExternalData().getRefFromUUID(this.uuid);
         if (this.reference != null && !this.validateEntityReference(this.reference, componentAccessor)) {
            this.clear();
         }

         return this.reference;
      }
   }

   protected boolean validateEntityReference(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
      return true;
   }
}
