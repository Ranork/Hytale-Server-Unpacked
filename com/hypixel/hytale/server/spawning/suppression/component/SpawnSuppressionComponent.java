package com.hypixel.hytale.server.spawning.suppression.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class SpawnSuppressionComponent implements Component<EntityStore> {
   public static final BuilderCodec<SpawnSuppressionComponent> CODEC = BuilderCodec.builder(SpawnSuppressionComponent.class, SpawnSuppressionComponent::new)
      .append(new KeyedCodec<>("SpawnSuppression", Codec.STRING), (suppressor, s) -> suppressor.spawnSuppression = s, suppressor -> suppressor.spawnSuppression)
      .add()
      .build();
   private String spawnSuppression;

   public static ComponentType<EntityStore, SpawnSuppressionComponent> getComponentType() {
      return SpawningPlugin.get().getSpawnSuppressorComponentType();
   }

   public SpawnSuppressionComponent(String spawnSuppression) {
      this.spawnSuppression = spawnSuppression;
   }

   private SpawnSuppressionComponent() {
   }

   public String getSpawnSuppression() {
      return this.spawnSuppression;
   }

   public void setSpawnSuppression(String spawnSuppression) {
      this.spawnSuppression = spawnSuppression;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      SpawnSuppressionComponent suppressor = new SpawnSuppressionComponent();
      suppressor.spawnSuppression = this.spawnSuppression;
      return suppressor;
   }
}
