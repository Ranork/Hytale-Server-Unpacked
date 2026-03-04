package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public class PairCodec {
   public static class IntegerPair {
      public static final BuilderCodec<PairCodec.IntegerPair> CODEC = BuilderCodec.builder(PairCodec.IntegerPair.class, PairCodec.IntegerPair::new)
         .append(new KeyedCodec<>("Left", Codec.INTEGER), (pair, left) -> pair.left = left, pair -> pair.left)
         .addValidator(Validators.nonNull())
         .add()
         .<Integer>append(new KeyedCodec<>("Right", Codec.INTEGER), (pair, right) -> pair.right = right, pair -> pair.right)
         .addValidator(Validators.nonNull())
         .add()
         .build();
      private Integer left;
      private Integer right;

      public IntegerPair() {
      }

      public IntegerPair(Integer left, Integer right) {
         this.left = left;
         this.right = right;
      }

      @Nonnull
      public Pair<Integer, Integer> toPair() {
         return Pair.of(this.left, this.right);
      }

      @Nonnull
      public static PairCodec.IntegerPair fromPair(@Nonnull Pair<Integer, Integer> pair) {
         return new PairCodec.IntegerPair((Integer)pair.left(), (Integer)pair.right());
      }

      public Integer getLeft() {
         return this.left;
      }

      public Integer getRight() {
         return this.right;
      }
   }

   public static class IntegerStringPair {
      public static final BuilderCodec<PairCodec.IntegerStringPair> CODEC = BuilderCodec.builder(
            PairCodec.IntegerStringPair.class, PairCodec.IntegerStringPair::new
         )
         .append(new KeyedCodec<>("Left", Codec.INTEGER), (pair, left) -> pair.left = left, pair -> pair.left)
         .addValidator(Validators.nonNull())
         .add()
         .<String>append(new KeyedCodec<>("Right", Codec.STRING), (pair, right) -> pair.right = right, pair -> pair.right)
         .addValidator(Validators.nonNull())
         .add()
         .build();
      private Integer left;
      private String right;

      public IntegerStringPair() {
      }

      public IntegerStringPair(Integer left, String right) {
         this.left = left;
         this.right = right;
      }

      @Nonnull
      public Pair<Integer, String> toPair() {
         return Pair.of(this.left, this.right);
      }

      @Nonnull
      public static PairCodec.IntegerStringPair fromPair(@Nonnull Pair<Integer, String> pair) {
         return new PairCodec.IntegerStringPair((Integer)pair.left(), (String)pair.right());
      }

      public Integer getLeft() {
         return this.left;
      }

      public String getRight() {
         return this.right;
      }
   }
}
