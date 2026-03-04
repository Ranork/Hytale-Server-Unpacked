package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import javax.annotation.Nonnull;

public abstract class CombatTextUIComponentAnimationEvent
   implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, CombatTextUIComponentAnimationEvent>> {
   public static final AssetCodecMapCodec<String, CombatTextUIComponentAnimationEvent> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   public static final BuilderCodec<CombatTextUIComponentAnimationEvent> ABSTRACT_CODEC = AssetBuilderCodec.abstractBuilder(
         CombatTextUIComponentAnimationEvent.class
      )
      .append(new KeyedCodec<>("StartAt", Codec.FLOAT), (event, f) -> event.startAt = f, event -> event.startAt)
      .documentation("The percentage of the display duration at which this event should begin.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .<Float>append(new KeyedCodec<>("EndAt", Codec.FLOAT), (event, f) -> event.endAt = f, event -> event.endAt)
      .documentation("The percentage of the display duration at which this event should end.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.range(0.0F, 1.0F))
      .add()
      .build();
   protected String id;
   protected AssetExtraInfo.Data data;
   private CombatTextEntityUIAnimationEventType type;
   private float startAt;
   private float endAt;

   public String getId() {
      return this.id;
   }

   @Nonnull
   public CombatTextEntityUIComponentAnimationEvent generatePacket() {
      CombatTextEntityUIComponentAnimationEvent packet = new CombatTextEntityUIComponentAnimationEvent();
      packet.type = this.type;
      packet.startAt = this.startAt;
      packet.endAt = this.endAt;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CombatTextUIComponentAnimationEvent{type=" + this.type + ", startAt=" + this.startAt + ", endAt=" + this.endAt + "} " + super.toString();
   }
}
