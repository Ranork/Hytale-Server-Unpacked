package com.hypixel.hytale.builtin.creativehub.config;

import com.hypixel.hytale.builtin.creativehub.CreativeHubPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreativeHubEntityConfig implements Component<EntityStore> {
   @Nonnull
   public static final String ID = "CreativeHub";
   @Nonnull
   public static final BuilderCodec<CreativeHubEntityConfig> CODEC = BuilderCodec.builder(CreativeHubEntityConfig.class, CreativeHubEntityConfig::new)
      .appendInherited(
         new KeyedCodec<>("ParentHubWorldUuid", Codec.UUID_STRING),
         (o, i) -> o.parentHubWorldUuid = i,
         o -> o.parentHubWorldUuid,
         (o, p) -> o.parentHubWorldUuid = p.parentHubWorldUuid
      )
      .add()
      .build();
   @Nullable
   private UUID parentHubWorldUuid;

   @Nonnull
   public static ComponentType<EntityStore, CreativeHubEntityConfig> getComponentType() {
      return CreativeHubPlugin.get().getCreativeHubEntityConfigComponentType();
   }

   @Nonnull
   public static CreativeHubEntityConfig ensureAndGet(@Nonnull Holder<EntityStore> holder) {
      ComponentType<EntityStore, CreativeHubEntityConfig> type = getComponentType();
      return holder.ensureAndGetComponent(type);
   }

   @Nullable
   public static CreativeHubEntityConfig get(@Nonnull Holder<EntityStore> holder) {
      ComponentType<EntityStore, CreativeHubEntityConfig> type = getComponentType();
      return holder.getComponent(type);
   }

   @Nullable
   public UUID getParentHubWorldUuid() {
      return this.parentHubWorldUuid;
   }

   public void setParentHubWorldUuid(@Nullable UUID parentHubWorldUuid) {
      this.parentHubWorldUuid = parentHubWorldUuid;
   }

   @Nonnull
   public CreativeHubEntityConfig clone() {
      CreativeHubEntityConfig v = new CreativeHubEntityConfig();
      v.parentHubWorldUuid = this.parentHubWorldUuid;
      return v;
   }
}
