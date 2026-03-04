package com.hypixel.hytale.math.vector.relative;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector2i;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RelativeVector2i {
   @Nonnull
   public static final BuilderCodec<RelativeVector2i> CODEC = BuilderCodec.builder(RelativeVector2i.class, RelativeVector2i::new)
      .append(new KeyedCodec<>("Vector", Vector2i.CODEC), (o, i) -> o.vector = i, RelativeVector2i::getVector)
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>append(new KeyedCodec<>("Relative", Codec.BOOLEAN), (o, i) -> o.relative = i, RelativeVector2i::isRelative)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private Vector2i vector;
   private boolean relative;

   public RelativeVector2i(@Nonnull Vector2i vector, boolean relative) {
      this.vector = vector;
      this.relative = relative;
   }

   protected RelativeVector2i() {
   }

   @Nonnull
   public Vector2i getVector() {
      return this.vector;
   }

   public boolean isRelative() {
      return this.relative;
   }

   @Nonnull
   public Vector2i resolve(@Nonnull Vector2i vector) {
      return this.relative ? vector.clone().add(vector) : vector.clone();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RelativeVector2i that = (RelativeVector2i)o;
         return this.relative != that.relative ? false : Objects.equals(this.vector, that.vector);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.vector != null ? this.vector.hashCode() : 0;
      return 31 * result + (this.relative ? 1 : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "RelativeVector2i{vector=" + this.vector + ", relative=" + this.relative + "}";
   }
}
