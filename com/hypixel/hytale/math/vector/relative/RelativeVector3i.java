package com.hypixel.hytale.math.vector.relative;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RelativeVector3i {
   @Nonnull
   public static final BuilderCodec<RelativeVector3i> CODEC = BuilderCodec.builder(RelativeVector3i.class, RelativeVector3i::new)
      .append(new KeyedCodec<>("Vector", Vector3i.CODEC), (o, i) -> o.vector = i, RelativeVector3i::getVector)
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>append(new KeyedCodec<>("Relative", Codec.BOOLEAN), (o, i) -> o.relative = i, RelativeVector3i::isRelative)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private Vector3i vector;
   private boolean relative;

   public RelativeVector3i(@Nonnull Vector3i vector, boolean relative) {
      this.vector = vector;
      this.relative = relative;
   }

   protected RelativeVector3i() {
   }

   @Nonnull
   public Vector3i getVector() {
      return this.vector;
   }

   public boolean isRelative() {
      return this.relative;
   }

   @Nonnull
   public Vector3i resolve(@Nonnull Vector3i vector) {
      return this.relative ? vector.clone().add(vector) : vector.clone();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RelativeVector3i that = (RelativeVector3i)o;
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
      return "RelativeVector3i{vector=" + this.vector + ", relative=" + this.relative + "}";
   }
}
