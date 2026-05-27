package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import org.joml.Vector3L;

public final class Vector3LUtil {
   @Nonnull
   public static final BuilderCodec<Vector3L> CODEC = BuilderCodec.builder(Vector3L.class, Vector3L::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Long>appendInherited(new KeyedCodec<>("X", Codec.LONG), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Long>appendInherited(new KeyedCodec<>("Y", Codec.LONG), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .<Long>appendInherited(new KeyedCodec<>("Z", Codec.LONG), (o, i) -> o.z = i, o -> o.z, (o, p) -> o.z = p.z)
      .addValidator(Validators.nonNull())
      .add()
      .build();

   private Vector3LUtil() {
   }
}
