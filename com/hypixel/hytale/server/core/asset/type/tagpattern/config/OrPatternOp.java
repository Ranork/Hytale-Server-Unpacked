package com.hypixel.hytale.server.core.asset.type.tagpattern.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.TagPatternType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class OrPatternOp extends MultiplePatternOp {
   @Nonnull
   public static BuilderCodec<OrPatternOp> CODEC = BuilderCodec.builder(OrPatternOp.class, OrPatternOp::new, MultiplePatternOp.CODEC).build();

   @Override
   public boolean test(Int2ObjectMap<IntSet> tags) {
      for (int i = 0; i < this.patterns.length; i++) {
         if (this.patterns[i].test(tags)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public com.hypixel.hytale.protocol.TagPattern toPacket() {
      com.hypixel.hytale.protocol.TagPattern cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.TagPattern packet = super.toPacket();
         packet.type = TagPatternType.Or;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "OrPatternOp{} " + super.toString();
   }
}
