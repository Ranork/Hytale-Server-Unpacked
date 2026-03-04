package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemUtility implements NetworkSerializable<com.hypixel.hytale.protocol.ItemUtility> {
   public static final ItemUtility DEFAULT = new ItemUtility();
   public static final BuilderCodec<ItemUtility> CODEC = BuilderCodec.builder(ItemUtility.class, ItemUtility::new)
      .append(new KeyedCodec<>("Usable", Codec.BOOLEAN), (o, v) -> o.usable = v, o -> o.usable)
      .add()
      .append(new KeyedCodec<>("Compatible", Codec.BOOLEAN), (o, v) -> o.compatible = v, o -> o.compatible)
      .add()
      .<Map>append(
         new KeyedCodec<>("StatModifiers", new MapCodec<>(new ArrayCodec<>(StaticModifier.CODEC, StaticModifier[]::new), HashMap::new)),
         (itemArmor, map) -> itemArmor.rawStatModifiers = map,
         itemArmor -> itemArmor.rawStatModifiers
      )
      .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator().late())
      .add()
      .append(
         new KeyedCodec<>("EntityStatsToClear", Codec.STRING_ARRAY),
         (itemUtility, strings) -> itemUtility.rawEntityStatsToClear = strings,
         itemUtility -> itemUtility.rawEntityStatsToClear
      )
      .add()
      .afterDecode(item -> {
         item.statModifiers = EntityStatsModule.resolveEntityStats(item.rawStatModifiers);
         item.entityStatsToClear = EntityStatsModule.resolveEntityStats(item.rawEntityStatsToClear);
      })
      .build();
   protected boolean usable;
   protected boolean compatible;
   @Nullable
   protected Map<String, StaticModifier[]> rawStatModifiers;
   @Nullable
   protected Int2ObjectMap<StaticModifier[]> statModifiers;
   protected String[] rawEntityStatsToClear;
   @Nullable
   protected int[] entityStatsToClear;

   public boolean isUsable() {
      return this.usable;
   }

   public boolean isCompatible() {
      return this.compatible;
   }

   @Nullable
   public Int2ObjectMap<StaticModifier[]> getStatModifiers() {
      return this.statModifiers;
   }

   public int[] getEntityStatsToClear() {
      return this.entityStatsToClear;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemUtility toPacket() {
      return new com.hypixel.hytale.protocol.ItemUtility(this.usable, this.compatible, this.entityStatsToClear, EntityStatMap.toPacket(this.statModifiers));
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemUtility{usable="
         + this.usable
         + ", compatible="
         + this.compatible
         + ", rawStatModifiers="
         + this.rawStatModifiers
         + ", statModifiers="
         + this.statModifiers
         + ", rawEntityStatsToClear="
         + Arrays.toString((Object[])this.rawEntityStatsToClear)
         + ", entityStatsToClear="
         + Arrays.toString(this.entityStatsToClear)
         + "}";
   }
}
