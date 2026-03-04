package com.hypixel.hytale.server.core.asset.type.tagpattern.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.TagPatternType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class EqualsTagOp extends TagPattern {
   @Nonnull
   public static BuilderCodec<EqualsTagOp> CODEC = BuilderCodec.builder(EqualsTagOp.class, EqualsTagOp::new, TagPattern.BASE_CODEC)
      .append(new KeyedCodec<>("Tag", Codec.STRING), (singleTagOp, s) -> singleTagOp.tag = s, singleTagOp -> singleTagOp.tag)
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(singleTagOp -> {
         if (singleTagOp.tag != null) {
            singleTagOp.tagIndex = AssetRegistry.getOrCreateTagIndex(singleTagOp.tag);
         }
      })
      .build();
   protected String tag;
   protected int tagIndex;

   public EqualsTagOp(String tag) {
      this.tag = tag;
   }

   protected EqualsTagOp() {
   }

   @Override
   public boolean test(@Nonnull Int2ObjectMap<IntSet> tags) {
      return tags.containsKey(this.tagIndex);
   }

   public com.hypixel.hytale.protocol.TagPattern toPacket() {
      com.hypixel.hytale.protocol.TagPattern cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.TagPattern packet = new com.hypixel.hytale.protocol.TagPattern();
         packet.type = TagPatternType.Equals;
         packet.tagIndex = this.tagIndex;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "EqualsTagOp{tag='" + this.tag + "'} " + super.toString();
   }
}
