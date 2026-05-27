package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class Vector3fUtil {
   @Nonnull
   public static final BuilderCodec<Vector3f> CODEC = BuilderCodec.builder(Vector3f.class, Vector3f::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Float>appendInherited(new KeyedCodec<>("X", Codec.FLOAT), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Y", Codec.FLOAT), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Z", Codec.FLOAT), (o, i) -> o.z = i, o -> o.z, (o, p) -> o.z = p.z)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Vector3fc ZERO = new Vector3f(0.0F, 0.0F, 0.0F);

   private Vector3fUtil() {
   }
}
