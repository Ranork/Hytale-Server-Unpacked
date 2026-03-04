package com.hypixel.hytale.math.vector.relative;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector2l;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RelativeVector2l {
   @Nonnull
   public static final BuilderCodec<RelativeVector2l> CODEC = BuilderCodec.builder(RelativeVector2l.class, RelativeVector2l::new)
      .append(new KeyedCodec<>("Vector", Vector2l.CODEC), (o, i) -> o.vector = i, RelativeVector2l::getVector)
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>append(new KeyedCodec<>("Relative", Codec.BOOLEAN), (o, i) -> o.relative = i, RelativeVector2l::isRelative)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private Vector2l vector;
   private boolean relative;

   public RelativeVector2l(@Nonnull Vector2l vector, boolean relative) {
      this.vector = vector;
      this.relative = relative;
   }

   protected RelativeVector2l() {
   }

   @Nonnull
   public Vector2l getVector() {
      return this.vector;
   }

   public boolean isRelative() {
      return this.relative;
   }

   @Nonnull
   public Vector2l resolve(@Nonnull Vector2l vector) {
      return this.relative ? vector.clone().add(vector) : vector.clone();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RelativeVector2l that = (RelativeVector2l)o;
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
      return "RelativeVector2l{vector=" + this.vector + ", relative=" + this.relative + "}";
   }
}
