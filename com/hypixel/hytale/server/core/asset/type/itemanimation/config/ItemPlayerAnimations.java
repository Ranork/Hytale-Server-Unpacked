package com.hypixel.hytale.server.core.asset.type.itemanimation.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.protocol.ItemAnimation;
import com.hypixel.hytale.protocol.WiggleWeights;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemPullbackConfig;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class ItemPlayerAnimations
   implements JsonAssetWithMap<String, DefaultAssetMap<String, ItemPlayerAnimations>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemPlayerAnimations> {
   public static final String DEFAULT_ID = "Default";
   public static final BuilderCodec<WiggleWeights> WIGGLE_WEIGHTS_CODEC = BuilderCodec.builder(WiggleWeights.class, WiggleWeights::new)
      .addField(new KeyedCodec<>("X", Codec.DOUBLE), (wiggleWeight, d) -> wiggleWeight.x = d.floatValue(), wiggleWeight -> (double)wiggleWeight.x)
      .addField(
         new KeyedCodec<>("XDeceleration", Codec.DOUBLE),
         (wiggleWeight, d) -> wiggleWeight.xDeceleration = d.floatValue(),
         wiggleWeight -> (double)wiggleWeight.xDeceleration
      )
      .addField(new KeyedCodec<>("Y", Codec.DOUBLE), (wiggleWeight, d) -> wiggleWeight.y = d.floatValue(), wiggleWeight -> (double)wiggleWeight.y)
      .addField(
         new KeyedCodec<>("YDeceleration", Codec.DOUBLE),
         (wiggleWeight, d) -> wiggleWeight.yDeceleration = d.floatValue(),
         wiggleWeight -> (double)wiggleWeight.yDeceleration
      )
      .addField(new KeyedCodec<>("Z", Codec.DOUBLE), (wiggleWeight, d) -> wiggleWeight.z = d.floatValue(), wiggleWeight -> (double)wiggleWeight.z)
      .addField(
         new KeyedCodec<>("ZDeceleration", Codec.DOUBLE),
         (wiggleWeight, d) -> wiggleWeight.zDeceleration = d.floatValue(),
         wiggleWeight -> (double)wiggleWeight.zDeceleration
      )
      .addField(new KeyedCodec<>("Roll", Codec.DOUBLE), (wiggleWeight, d) -> wiggleWeight.roll = d.floatValue(), wiggleWeight -> (double)wiggleWeight.roll)
      .addField(
         new KeyedCodec<>("RollDeceleration", Codec.DOUBLE),
         (wiggleWeight, d) -> wiggleWeight.rollDeceleration = d.floatValue(),
         wiggleWeight -> (double)wiggleWeight.rollDeceleration
      )
      .addField(new KeyedCodec<>("Pitch", Codec.DOUBLE), (wiggleWeight, d) -> wiggleWeight.pitch = d.floatValue(), wiggleWeight -> (double)wiggleWeight.pitch)
      .addField(
         new KeyedCodec<>("PitchDeceleration", Codec.DOUBLE),
         (wiggleWeight, d) -> wiggleWeight.pitchDeceleration = d.floatValue(),
         wiggleWeight -> (double)wiggleWeight.pitchDeceleration
      )
      .build();
   public static final AssetBuilderCodec<String, ItemPlayerAnimations> CODEC = AssetBuilderCodec.builder(
         ItemPlayerAnimations.class,
         ItemPlayerAnimations::new,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Animations", new MapCodec<>(ProtocolCodecs.ITEM_ANIMATION_CODEC, HashMap::new)),
         (itemPlayerAnimations, map) -> itemPlayerAnimations.animations = MapUtil.combineUnmodifiable(itemPlayerAnimations.animations, map),
         itemPlayerAnimations -> itemPlayerAnimations.animations,
         (itemPlayerAnimations, parent) -> itemPlayerAnimations.animations = parent.animations
      )
      .addValidator(Validators.nonNull())
      .add()
      .<WiggleWeights>appendInherited(
         new KeyedCodec<>("WiggleWeights", WIGGLE_WEIGHTS_CODEC),
         (itemPlayerAnimations, map) -> itemPlayerAnimations.wiggleWeights = map,
         itemPlayerAnimations -> itemPlayerAnimations.wiggleWeights,
         (itemPlayerAnimations, parent) -> itemPlayerAnimations.wiggleWeights = parent.wiggleWeights
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("Camera", CameraSettings.CODEC),
         (itemPlayerAnimations, o) -> itemPlayerAnimations.camera = o,
         itemPlayerAnimations -> itemPlayerAnimations.camera,
         (itemPlayerAnimations, parent) -> itemPlayerAnimations.camera = parent.camera
      )
      .add()
      .<ItemPullbackConfig>appendInherited(
         new KeyedCodec<>("PullbackConfig", ItemPullbackConfig.CODEC),
         (itemPlayerAnimations, s) -> itemPlayerAnimations.pullbackConfig = s,
         itemPlayerAnimations -> itemPlayerAnimations.pullbackConfig,
         (itemPlayerAnimations, parent) -> itemPlayerAnimations.pullbackConfig = parent.pullbackConfig
      )
      .documentation("Overrides the offset of first person arms when close to obstacles")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("UseFirstPersonOverrides", Codec.BOOLEAN),
         (itemPlayerAnimations, s) -> itemPlayerAnimations.useFirstPersonOverrides = s,
         itemPlayerAnimations -> itemPlayerAnimations.useFirstPersonOverrides,
         (itemPlayerAnimations, parent) -> itemPlayerAnimations.useFirstPersonOverrides = parent.useFirstPersonOverrides
      )
      .documentation("Determines whether or not to use FirstPersonOverride animations within ItemAnimations")
      .add()
      .build();
   public static final Codec<String> CHILD_CODEC = new ContainedAssetCodec<>(ItemPlayerAnimations.class, CODEC);
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ItemPlayerAnimations::getAssetStore));
   private static AssetStore<String, ItemPlayerAnimations, DefaultAssetMap<String, ItemPlayerAnimations>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Map<String, ItemAnimation> animations = Collections.emptyMap();
   protected WiggleWeights wiggleWeights;
   protected CameraSettings camera;
   protected ItemPullbackConfig pullbackConfig;
   protected boolean useFirstPersonOverrides;
   private SoftReference<com.hypixel.hytale.protocol.ItemPlayerAnimations> cachedPacket;

   public static AssetStore<String, ItemPlayerAnimations, DefaultAssetMap<String, ItemPlayerAnimations>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ItemPlayerAnimations.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ItemPlayerAnimations> getAssetMap() {
      return (DefaultAssetMap<String, ItemPlayerAnimations>)getAssetStore().getAssetMap();
   }

   public ItemPlayerAnimations(
      String id,
      Map<String, ItemAnimation> animations,
      WiggleWeights wiggleWeights,
      CameraSettings camera,
      ItemPullbackConfig pullbackConfig,
      boolean useFirstPersonOverrides
   ) {
      this.id = id;
      this.animations = animations;
      this.wiggleWeights = wiggleWeights;
      this.camera = camera;
      this.pullbackConfig = pullbackConfig;
      this.useFirstPersonOverrides = useFirstPersonOverrides;
   }

   protected ItemPlayerAnimations() {
   }

   public String getId() {
      return this.id;
   }

   public Map<String, ItemAnimation> getAnimations() {
      return this.animations;
   }

   public WiggleWeights getWiggleWeights() {
      return this.wiggleWeights;
   }

   public CameraSettings getCamera() {
      return this.camera;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemPlayerAnimations toPacket() {
      com.hypixel.hytale.protocol.ItemPlayerAnimations cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemPlayerAnimations packet = new com.hypixel.hytale.protocol.ItemPlayerAnimations();
         packet.id = this.id;
         packet.animations = this.animations;
         packet.wiggleWeights = this.wiggleWeights;
         if (this.camera != null) {
            packet.camera = this.camera.toPacket();
         }

         if (this.pullbackConfig != null) {
            packet.pullbackConfig = this.pullbackConfig.toPacket();
         }

         packet.useFirstPersonOverride = this.useFirstPersonOverrides;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemPlayerAnimations{id='"
         + this.id
         + "', animations="
         + this.animations
         + ", wiggleWeights="
         + this.wiggleWeights
         + ", camera="
         + this.camera
         + ", pullbackConfig="
         + this.pullbackConfig
         + ", useFirstPersonOverrides="
         + this.useFirstPersonOverrides
         + "}";
   }
}
