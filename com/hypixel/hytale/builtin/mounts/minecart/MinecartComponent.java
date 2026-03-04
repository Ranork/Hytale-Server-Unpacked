package com.hypixel.hytale.builtin.mounts.minecart;

import com.hypixel.hytale.builtin.mounts.MountPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nullable;

public class MinecartComponent implements Component<EntityStore> {
   public static final BuilderCodec<MinecartComponent> CODEC = BuilderCodec.builder(MinecartComponent.class, MinecartComponent::new)
      .append(new KeyedCodec<>("SourceItem", Codec.STRING), (o, v) -> o.sourceItem = v, o -> o.sourceItem)
      .add()
      .build();
   private int numberOfHits = 0;
   @Nullable
   private Instant lastHit;
   private String sourceItem = "Rail_Kart";

   public static ComponentType<EntityStore, MinecartComponent> getComponentType() {
      return MountPlugin.getInstance().getMinecartComponentType();
   }

   private MinecartComponent() {
   }

   public MinecartComponent(String sourceItem) {
      this.sourceItem = sourceItem;
   }

   public int getNumberOfHits() {
      return this.numberOfHits;
   }

   public void setNumberOfHits(int numberOfHits) {
      this.numberOfHits = numberOfHits;
   }

   @Nullable
   public Instant getLastHit() {
      return this.lastHit;
   }

   public void setLastHit(@Nullable Instant lastHit) {
      this.lastHit = lastHit;
   }

   public String getSourceItem() {
      return this.sourceItem;
   }

   public void setSourceItem(String sourceItem) {
      this.sourceItem = sourceItem;
   }

   @Override
   public Component<EntityStore> clone() {
      return new MinecartComponent(this.sourceItem);
   }
}
