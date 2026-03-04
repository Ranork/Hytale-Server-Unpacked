package com.hypixel.hytale.builtin.instances.config;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstanceEntityConfig implements Component<EntityStore> {
   @Nonnull
   public static final String ID = "Instance";
   @Nonnull
   public static final BuilderCodec<InstanceEntityConfig> CODEC = BuilderCodec.builder(InstanceEntityConfig.class, InstanceEntityConfig::new)
      .appendInherited(
         new KeyedCodec<>("ReturnPoint", WorldReturnPoint.CODEC), (o, i) -> o.returnPoint = i, o -> o.returnPoint, (o, p) -> o.returnPoint = p.returnPoint
      )
      .add()
      .build();
   private WorldReturnPoint returnPoint;
   private transient WorldReturnPoint returnPointOverride;

   @Nonnull
   public static ComponentType<EntityStore, InstanceEntityConfig> getComponentType() {
      return InstancesPlugin.get().getInstanceEntityConfigComponentType();
   }

   @Nonnull
   public static InstanceEntityConfig ensureAndGet(@Nonnull Holder<EntityStore> holder) {
      ComponentType<EntityStore, InstanceEntityConfig> type = getComponentType();
      return holder.ensureAndGetComponent(type);
   }

   @Nullable
   public static InstanceEntityConfig removeAndGet(@Nonnull Holder<EntityStore> holder) {
      ComponentType<EntityStore, InstanceEntityConfig> type = getComponentType();
      InstanceEntityConfig component = holder.getComponent(type);
      holder.removeComponent(type);
      return component;
   }

   public WorldReturnPoint getReturnPoint() {
      return this.returnPoint;
   }

   public void setReturnPoint(WorldReturnPoint returnPoint) {
      this.returnPoint = returnPoint;
   }

   public WorldReturnPoint getReturnPointOverride() {
      return this.returnPointOverride;
   }

   public void setReturnPointOverride(WorldReturnPoint returnPointOverride) {
      this.returnPointOverride = returnPointOverride;
   }

   @Nonnull
   public InstanceEntityConfig clone() {
      InstanceEntityConfig config = new InstanceEntityConfig();
      config.returnPoint = config.returnPoint.clone();
      config.returnPointOverride = config.returnPointOverride.clone();
      return config;
   }
}
