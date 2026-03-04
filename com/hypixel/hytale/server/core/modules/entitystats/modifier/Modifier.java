package com.hypixel.hytale.server.core.modules.entitystats.modifier;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Modifier implements NetworkSerializable<com.hypixel.hytale.protocol.Modifier> {
   public static final CodecMapCodec<Modifier> CODEC = new CodecMapCodec<>();
   protected static final BuilderCodec<Modifier> BASE_CODEC = BuilderCodec.abstractBuilder(Modifier.class)
      .append(
         new KeyedCodec<>("Target", new EnumCodec<>(Modifier.ModifierTarget.class, EnumCodec.EnumStyle.LEGACY)),
         (regenerating, value) -> regenerating.target = value,
         regenerating -> regenerating.target
      )
      .add()
      .build();
   protected Modifier.ModifierTarget target = Modifier.ModifierTarget.MAX;

   public Modifier() {
   }

   public Modifier(Modifier.ModifierTarget target) {
      this.target = target;
   }

   public abstract float apply(float var1);

   public Modifier.ModifierTarget getTarget() {
      return this.target;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Modifier toPacket() {
      if (!(this instanceof StaticModifier)) {
         throw new UnsupportedOperationException("Only static modifiers supported on the client currently.");
      } else {
         com.hypixel.hytale.protocol.Modifier packet = new com.hypixel.hytale.protocol.Modifier();

         packet.target = switch (this.target) {
            case MIN -> com.hypixel.hytale.protocol.ModifierTarget.Min;
            case MAX -> com.hypixel.hytale.protocol.ModifierTarget.Max;
         };
         return packet;
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Modifier modifier = (Modifier)o;
         return this.target == modifier.target;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.target != null ? this.target.hashCode() : 0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Modifier{target=" + this.target + "}";
   }

   public static enum ModifierTarget {
      MIN,
      MAX;

      public static final Modifier.ModifierTarget[] VALUES = values();
   }
}
