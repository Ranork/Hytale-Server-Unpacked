package com.hypixel.hytale.server.core.asset.type.portalworld;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

public class PillTag {
   public static final BuilderCodec<PillTag> CODEC = BuilderCodec.builder(PillTag.class, PillTag::new)
      .appendInherited(
         new KeyedCodec<>("TranslationKey", Codec.STRING),
         (pillTag, o) -> pillTag.translationKey = o,
         pillTag -> pillTag.translationKey,
         (pillTag, parent) -> pillTag.translationKey = parent.translationKey
      )
      .documentation("The translation key for the text of this tag.")
      .add()
      .appendInherited(
         new KeyedCodec<>("Color", ProtocolCodecs.COLOR),
         (pillTag, o) -> pillTag.color = o,
         pillTag -> pillTag.color,
         (pillTag, parent) -> pillTag.color = parent.color
      )
      .add()
      .build();
   public static final PillTag[] EMPTY_LIST = new PillTag[0];
   private String translationKey;
   private Color color;

   public String getTranslationKey() {
      return this.translationKey;
   }

   public Message getMessage() {
      return Message.translation(this.translationKey);
   }

   public Color getColor() {
      return this.color;
   }

   @Override
   public String toString() {
      return "PillTag{translationKey='" + this.translationKey + "', color=" + this.color + "}";
   }
}
