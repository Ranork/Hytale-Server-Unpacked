package com.hypixel.hytale.server.core.asset.type.tagpattern.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.TagPatternType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class NotPatternOp extends TagPattern {
   @Nonnull
   public static BuilderCodec<NotPatternOp> CODEC = BuilderCodec.builder(NotPatternOp.class, NotPatternOp::new, TagPattern.BASE_CODEC)
      .append(new KeyedCodec<>("Pattern", TagPattern.CODEC), (singleTagOp, s) -> singleTagOp.pattern = s, singleTagOp -> singleTagOp.pattern)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected TagPattern pattern;

   @Override
   public boolean test(Int2ObjectMap<IntSet> tags) {
      return !this.pattern.test(tags);
   }

   public com.hypixel.hytale.protocol.TagPattern toPacket() {
      com.hypixel.hytale.protocol.TagPattern cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.TagPattern packet = new com.hypixel.hytale.protocol.TagPattern();
         packet.type = TagPatternType.Not;
         packet.not = this.pattern.toPacket();
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "NotPatternOp{pattern=" + this.pattern + "} " + super.toString();
   }
}
