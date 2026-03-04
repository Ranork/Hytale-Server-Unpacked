package com.hypixel.hytale.server.core.asset.type.trail.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.protocol.FXRenderMode;
import com.hypixel.hytale.protocol.IntersectionHighlight;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class Trail implements JsonAssetWithMap<String, DefaultAssetMap<String, Trail>>, NetworkSerializable<com.hypixel.hytale.protocol.Trail> {
   public static final AssetBuilderCodec<String, Trail> CODEC = AssetBuilderCodec.builder(
         Trail.class, Trail::new, Codec.STRING, (trail, s) -> trail.id = s, trail -> trail.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("TexturePath", Codec.STRING),
         (trail, s) -> trail.texture = s,
         trail -> trail.texture,
         (trail, parent) -> trail.texture = parent.texture
      )
      .addValidator(Validators.nonNull())
      .addValidator(CommonAssetValidator.TEXTURE_TRAIL)
      .add()
      .appendInherited(
         new KeyedCodec<>("LifeSpan", Codec.INTEGER),
         (trail, i) -> trail.lifeSpan = i,
         trail -> trail.lifeSpan,
         (trail, parent) -> trail.lifeSpan = parent.lifeSpan
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Roll", Codec.DOUBLE),
         (trail, d) -> trail.roll = d.floatValue(),
         trail -> (double)trail.roll,
         (trail, parent) -> trail.roll = parent.roll
      )
      .add()
      .<Edge>appendInherited(
         new KeyedCodec<>("Start", Edge.CODEC), (trail, o) -> trail.start = o, trail -> trail.start, (trail, parent) -> trail.start = parent.start
      )
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<Edge>appendInherited(new KeyedCodec<>("End", Edge.CODEC), (trail, o) -> trail.end = o, trail -> trail.end, (trail, parent) -> trail.end = parent.end)
      .metadata(UIDefaultCollapsedState.UNCOLLAPSED)
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("LightInfluence", Codec.DOUBLE),
         (trail, d) -> trail.lightInfluence = d.floatValue(),
         trail -> (double)trail.lightInfluence,
         (trail, parent) -> trail.lightInfluence = parent.lightInfluence
      )
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .<FXRenderMode>appendInherited(
         new KeyedCodec<>("RenderMode", new EnumCodec<>(FXRenderMode.class)),
         (trail, s) -> trail.renderMode = s,
         trail -> trail.renderMode,
         (trail, parent) -> trail.renderMode = parent.renderMode
      )
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("IntersectionHighlight", ProtocolCodecs.INTERSECTION_HIGHLIGHT),
         (trail, s) -> trail.intersectionHighlight = s,
         trail -> trail.intersectionHighlight,
         (trail, parent) -> trail.intersectionHighlight = parent.intersectionHighlight
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Smooth", Codec.BOOLEAN), (trail, b) -> trail.smooth = b, trail -> trail.smooth, (trail, parent) -> trail.smooth = parent.smooth
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Animation", Animation.CODEC),
         (trail, b) -> trail.animation = b,
         trail -> trail.animation,
         (trail, parent) -> trail.animation = parent.animation
      )
      .add()
      .build();
   private static AssetStore<String, Trail, DefaultAssetMap<String, Trail>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Trail::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String texture;
   @Nonnull
   protected FXRenderMode renderMode = FXRenderMode.BlendLinear;
   protected IntersectionHighlight intersectionHighlight;
   protected int lifeSpan;
   protected float roll;
   protected float lightInfluence;
   protected boolean smooth;
   protected Edge start;
   protected Edge end;
   protected Animation animation;
   protected SoftReference<com.hypixel.hytale.protocol.Trail> cachedPacket;

   public static AssetStore<String, Trail, DefaultAssetMap<String, Trail>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Trail.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, Trail> getAssetMap() {
      return (DefaultAssetMap<String, Trail>)getAssetStore().getAssetMap();
   }

   public Trail(
      String id,
      String texture,
      FXRenderMode renderMode,
      IntersectionHighlight intersectionHighlight,
      int lifeSpan,
      float roll,
      float lightInfluence,
      boolean smooth,
      Edge start,
      Edge end,
      Animation animation
   ) {
      this.id = id;
      this.texture = texture;
      this.renderMode = renderMode;
      this.intersectionHighlight = intersectionHighlight;
      this.lifeSpan = lifeSpan;
      this.roll = roll;
      this.lightInfluence = lightInfluence;
      this.smooth = smooth;
      this.start = start;
      this.end = end;
      this.animation = animation;
   }

   protected Trail() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Trail toPacket() {
      com.hypixel.hytale.protocol.Trail cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.Trail packet = new com.hypixel.hytale.protocol.Trail();
         packet.id = this.id;
         packet.texture = this.texture;
         packet.lifeSpan = this.lifeSpan;
         packet.roll = this.roll;
         packet.lightInfluence = this.lightInfluence;
         packet.renderMode = this.renderMode;
         packet.intersectionHighlight = this.intersectionHighlight;
         packet.smooth = this.smooth;
         if (this.start != null) {
            packet.start = this.start.toPacket();
         }

         if (this.end != null) {
            packet.end = this.end.toPacket();
         }

         if (this.animation != null) {
            Vector2i frameSize = this.animation.getFrameSize();
            if (frameSize != null) {
               packet.frameSize = new com.hypixel.hytale.protocol.Vector2i(frameSize.getX(), frameSize.getY());
            }

            if (this.animation.getFrameRange() != null) {
               packet.frameRange = this.animation.getFrameRange();
            }

            packet.frameLifeSpan = this.animation.getFrameLifeSpan();
         }

         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public String getTexture() {
      return this.texture;
   }

   public FXRenderMode getRenderMode() {
      return this.renderMode;
   }

   public IntersectionHighlight getIntersectionHighlight() {
      return this.intersectionHighlight;
   }

   public int getLifeSpan() {
      return this.lifeSpan;
   }

   public float getRoll() {
      return this.roll;
   }

   public float getLightInfluence() {
      return this.lightInfluence;
   }

   public boolean isSmooth() {
      return this.smooth;
   }

   public Edge getStart() {
      return this.start;
   }

   public Edge getEnd() {
      return this.end;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Trail{id='"
         + this.id
         + "', texture='"
         + this.texture
         + "', renderMode="
         + this.renderMode
         + ", intersectionHighlight="
         + this.intersectionHighlight
         + ", lifeSpan="
         + this.lifeSpan
         + ", roll="
         + this.roll
         + ", lightInfluence="
         + this.lightInfluence
         + ", smooth="
         + this.smooth
         + ", start="
         + this.start
         + ", end="
         + this.end
         + ", animation="
         + this.animation
         + "}";
   }
}
