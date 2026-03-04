package com.hypixel.hytale.builtin.adventure.farming.states;

import com.hypixel.hytale.builtin.adventure.farming.FarmingPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TilledSoilBlock implements Component<ChunkStore> {
   public static int VERSION = 1;
   @Nonnull
   public static final BuilderCodec<TilledSoilBlock> CODEC = BuilderCodec.builder(TilledSoilBlock.class, TilledSoilBlock::new)
      .versioned()
      .codecVersion(VERSION)
      .append(new KeyedCodec<>("Planted", Codec.BOOLEAN), (state, planted) -> state.planted = planted, state -> state.planted ? Boolean.TRUE : null)
      .add()
      .<Map>append(new KeyedCodec<>("ModifierTimes", new MapCodec<>(Codec.INSTANT, Object2ObjectOpenHashMap::new, false)), (state, times) -> {
         if (times != null) {
            state.wateredUntil = (Instant)times.get("WateredUntil");
         }
      }, state -> null)
      .setVersionRange(0, 0)
      .add()
      .<String[]>append(new KeyedCodec<>("Flags", Codec.STRING_ARRAY), (state, flags) -> {
         if (flags != null) {
            state.fertilized = ArrayUtil.contains(flags, "Fertilized");
            state.externalWater = ArrayUtil.contains(flags, "ExternalWater");
         }
      }, state -> null)
      .setVersionRange(0, 0)
      .add()
      .<Boolean>append(new KeyedCodec<>("Fertilized", Codec.BOOLEAN), (state, v) -> state.fertilized = v, state -> state.fertilized ? Boolean.TRUE : null)
      .setVersionRange(1, VERSION)
      .add()
      .<Boolean>append(
         new KeyedCodec<>("ExternalWater", Codec.BOOLEAN), (state, v) -> state.externalWater = v, state -> state.externalWater ? Boolean.TRUE : null
      )
      .setVersionRange(1, VERSION)
      .add()
      .<Instant>append(new KeyedCodec<>("WateredUntil", Codec.INSTANT), (state, v) -> state.wateredUntil = v, state -> state.wateredUntil)
      .setVersionRange(1, VERSION)
      .add()
      .append(new KeyedCodec<>("DecayTime", Codec.INSTANT), (state, v) -> state.decayTime = v, state -> state.decayTime)
      .add()
      .build();
   protected boolean planted;
   protected boolean fertilized;
   protected boolean externalWater;
   @Nullable
   protected Instant wateredUntil;
   @Nullable
   protected Instant decayTime;

   public static ComponentType<ChunkStore, TilledSoilBlock> getComponentType() {
      return FarmingPlugin.get().getTiledSoilBlockComponentType();
   }

   public TilledSoilBlock() {
   }

   public TilledSoilBlock(boolean planted, boolean fertilized, boolean externalWater, Instant wateredUntil, Instant decayTime) {
      this.planted = planted;
      this.fertilized = fertilized;
      this.externalWater = externalWater;
      this.wateredUntil = wateredUntil;
      this.decayTime = decayTime;
   }

   public boolean isPlanted() {
      return this.planted;
   }

   public void setPlanted(boolean planted) {
      this.planted = planted;
   }

   public void setWateredUntil(@Nullable Instant wateredUntil) {
      this.wateredUntil = wateredUntil;
   }

   @Nullable
   public Instant getWateredUntil() {
      return this.wateredUntil;
   }

   public boolean isFertilized() {
      return this.fertilized;
   }

   public void setFertilized(boolean fertilized) {
      this.fertilized = fertilized;
   }

   public boolean hasExternalWater() {
      return this.externalWater;
   }

   public void setExternalWater(boolean externalWater) {
      this.externalWater = externalWater;
   }

   @Nullable
   public Instant getDecayTime() {
      return this.decayTime;
   }

   public void setDecayTime(@Nullable Instant decayTime) {
      this.decayTime = decayTime;
   }

   @Nullable
   public String computeBlockType(@Nonnull Instant gameTime, @Nonnull BlockType type) {
      boolean watered = this.hasExternalWater() || this.wateredUntil != null && this.wateredUntil.isAfter(gameTime);
      if (this.fertilized && watered) {
         return type.getBlockKeyForState("Fertilized_Watered");
      } else if (this.fertilized) {
         return type.getBlockKeyForState("Fertilized");
      } else {
         return watered ? type.getBlockKeyForState("Watered") : type.getBlockKeyForState("default");
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "TilledSoilBlock{planted="
         + this.planted
         + ", fertilized="
         + this.fertilized
         + ", externalWater="
         + this.externalWater
         + ", wateredUntil="
         + this.wateredUntil
         + ", decayTime="
         + this.decayTime
         + "}";
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new TilledSoilBlock(this.planted, this.fertilized, this.externalWater, this.wateredUntil, this.decayTime);
   }
}
