package com.hypixel.hytale.builtin.adventure.camera.asset.camerashake;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.adventure.camera.asset.CameraShakeConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class CameraShake implements NetworkSerializable<com.hypixel.hytale.protocol.CameraShake>, JsonAssetWithMap<String, IndexedAssetMap<String, CameraShake>> {
   @Nonnull
   public static final AssetBuilderCodec<String, CameraShake> CODEC = AssetBuilderCodec.builder(
         CameraShake.class, CameraShake::new, Codec.STRING, (o, v) -> o.id = v, CameraShake::getId, (o, data) -> o.data = data, o -> o.data
      )
      .appendInherited(
         new KeyedCodec<>("FirstPerson", CameraShakeConfig.CODEC), (o, v) -> o.firstPerson = v, o -> o.firstPerson, (o, p) -> o.firstPerson = p.firstPerson
      )
      .documentation("The camera shake to apply to the first-person camera")
      .addValidator(Validators.nonNull())
      .add()
      .<CameraShakeConfig>appendInherited(
         new KeyedCodec<>("ThirdPerson", CameraShakeConfig.CODEC), (o, v) -> o.thirdPerson = v, o -> o.thirdPerson, (o, p) -> o.thirdPerson = p.thirdPerson
      )
      .documentation("The camera shake to apply to the third-person camera")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(CameraShake.class, CODEC);
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(CameraShake::getAssetStore));
   private static AssetStore<String, CameraShake, IndexedAssetMap<String, CameraShake>> ASSET_STORE;
   protected String id;
   protected AssetExtraInfo.Data data;
   @Nonnull
   protected CameraShakeConfig firstPerson;
   @Nonnull
   protected CameraShakeConfig thirdPerson;

   @Nonnull
   public static AssetStore<String, CameraShake, IndexedAssetMap<String, CameraShake>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(CameraShake.class);
      }

      return ASSET_STORE;
   }

   @Nonnull
   public static IndexedAssetMap<String, CameraShake> getAssetMap() {
      return (IndexedAssetMap<String, CameraShake>)getAssetStore().getAssetMap();
   }

   public CameraShake() {
   }

   public CameraShake(@Nonnull String id) {
      this.id = id;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.CameraShake toPacket() {
      return new com.hypixel.hytale.protocol.CameraShake(this.firstPerson.toPacket(), this.thirdPerson.toPacket());
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CameraShake{id='" + this.id + "', data=" + this.data + ", firstPerson=" + this.firstPerson + ", thirdPerson=" + this.thirdPerson + "}";
   }
}
