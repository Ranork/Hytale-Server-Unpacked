package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public abstract class EntityUIComponent
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EntityUIComponent>>,
   NetworkSerializable<com.hypixel.hytale.protocol.EntityUIComponent> {
   public static final AssetCodecMapCodec<String, EntityUIComponent> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   public static final BuilderCodec<EntityUIComponent> ABSTRACT_CODEC = AssetBuilderCodec.abstractBuilder(EntityUIComponent.class)
      .append(new KeyedCodec<>("HitboxOffset", ProtocolCodecs.VECTOR2F), (config, v) -> config.hitboxOffset = v, config -> config.hitboxOffset)
      .documentation("Offset from the centre of the entity's hitbox to display this component.")
      .add()
      .build();
   protected String id;
   protected AssetExtraInfo.Data data;
   private Vector2f hitboxOffset = new Vector2f(0.0F, 0.0F);
   private transient SoftReference<com.hypixel.hytale.protocol.EntityUIComponent> cachedPacket;
   private static AssetStore<String, EntityUIComponent, IndexedLookupTableAssetMap<String, EntityUIComponent>> ASSET_STORE;

   public static AssetStore<String, EntityUIComponent, IndexedLookupTableAssetMap<String, EntityUIComponent>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(EntityUIComponent.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, EntityUIComponent> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, EntityUIComponent>)getAssetStore().getAssetMap();
   }

   protected EntityUIComponent() {
   }

   @Nonnull
   public static EntityUIComponent getUnknownFor(String id) {
      return new EntityUIComponent.Unknown(id);
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public final com.hypixel.hytale.protocol.EntityUIComponent toPacket() {
      com.hypixel.hytale.protocol.EntityUIComponent cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.EntityUIComponent packet = this.generatePacket();
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   protected com.hypixel.hytale.protocol.EntityUIComponent generatePacket() {
      com.hypixel.hytale.protocol.EntityUIComponent packet = new com.hypixel.hytale.protocol.EntityUIComponent();
      packet.hitboxOffset = this.hitboxOffset;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityUIComponentConfig{data=" + this.data + ", id='" + this.id + "', hitboxOffset='" + this.hitboxOffset + "'}";
   }

   private static class Unknown extends EntityUIComponent {
      public Unknown(String id) {
         this.id = id;
      }

      @Nonnull
      @Override
      protected com.hypixel.hytale.protocol.EntityUIComponent generatePacket() {
         com.hypixel.hytale.protocol.EntityUIComponent packet = super.generatePacket();
         packet.unknown = true;
         return packet;
      }
   }
}
