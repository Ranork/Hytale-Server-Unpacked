package com.hypixel.hytale.server.core.cosmetics;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ProtocolEmote;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;

public class EmoteAsset implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, EmoteAsset>>, NetworkSerializable<ProtocolEmote> {
   public static final AssetBuilderCodec<String, EmoteAsset> CODEC = AssetBuilderCodec.builder(
         EmoteAsset.class,
         EmoteAsset::new,
         Codec.STRING,
         (emoteAsset, s) -> emoteAsset.id = s,
         emoteAsset -> emoteAsset.id,
         (emoteAsset, data) -> emoteAsset.data = data,
         emoteAsset -> emoteAsset.data
      )
      .append(new KeyedCodec<>("Name", Codec.STRING), (asset, s) -> asset.name = s, asset -> asset.name)
      .documentation("The localization key of the emote name.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .<String>append(new KeyedCodec<>("Animation", Codec.STRING), (asset, s) -> asset.animationPath = s, asset -> asset.animationPath)
      .documentation("The path to the animation file (must be located in HytaleAssets/Common/Characters).")
      .addValidator(Validators.nonNull())
      .addValidator(CommonAssetValidator.ANIMATION_EMOTE)
      .add()
      .<String>append(new KeyedCodec<>("Icon", Codec.STRING), (asset, s) -> asset.iconPath = s, asset -> asset.iconPath)
      .documentation("The path to the icon file (must be located in HytaleAssets/Common/Icons/Emotes).")
      .addValidator(Validators.nonNull())
      .addValidator(CommonAssetValidator.ICON_EMOTE)
      .add()
      .append(new KeyedCodec<>("IsLooping", Codec.BOOLEAN), (asset, b) -> asset.isLooping = b, asset -> asset.isLooping)
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(EmoteAsset::getAssetStore));
   private static AssetStore<String, EmoteAsset, IndexedLookupTableAssetMap<String, EmoteAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String name;
   protected String animationPath;
   protected String iconPath;
   protected boolean isLooping;

   public static AssetStore<String, EmoteAsset, IndexedLookupTableAssetMap<String, EmoteAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(EmoteAsset.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, EmoteAsset> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, EmoteAsset>)getAssetStore().getAssetMap();
   }

   public EmoteAsset() {
   }

   public EmoteAsset(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public ProtocolEmote toPacket() {
      ProtocolEmote packet = new ProtocolEmote();
      packet.id = this.id;
      packet.name = this.name;
      packet.animation = this.animationPath;
      packet.icon = this.iconPath;
      packet.isLooping = this.isLooping;
      return packet;
   }
}
