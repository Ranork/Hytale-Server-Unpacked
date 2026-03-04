package com.hypixel.hytale.server.core.asset.type.tagpattern.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Arrays;
import javax.annotation.Nonnull;

public abstract class MultiplePatternOp extends TagPattern {
   @Nonnull
   public static BuilderCodec<MultiplePatternOp> CODEC = BuilderCodec.abstractBuilder(MultiplePatternOp.class, TagPattern.BASE_CODEC)
      .append(
         new KeyedCodec<>("Patterns", new ArrayCodec<>(TagPattern.CODEC, TagPattern[]::new)),
         (tagPattern, tagPatterns) -> tagPattern.patterns = tagPatterns,
         tagPattern -> tagPattern.patterns
      )
      .addValidator(Validators.nonEmptyArray())
      .add()
      .build();
   protected TagPattern[] patterns;

   public com.hypixel.hytale.protocol.TagPattern toPacket() {
      com.hypixel.hytale.protocol.TagPattern packet = new com.hypixel.hytale.protocol.TagPattern();
      packet.operands = new com.hypixel.hytale.protocol.TagPattern[this.patterns.length];

      for (int i = 0; i < this.patterns.length; i++) {
         packet.operands[i] = this.patterns[i].toPacket();
      }

      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MultiplePatternOp{patterns=" + Arrays.toString((Object[])this.patterns) + "} " + super.toString();
   }
}
