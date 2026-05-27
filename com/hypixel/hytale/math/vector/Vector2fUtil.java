package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public final class Vector2fUtil {
   @Nonnull
   public static final BuilderCodec<Vector2f> CODEC = BuilderCodec.builder(Vector2f.class, Vector2f::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Float>appendInherited(new KeyedCodec<>("X", Codec.FLOAT), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Float>appendInherited(new KeyedCodec<>("Y", Codec.FLOAT), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Vector2fc ZERO = new Vector2f(0.0F, 0.0F);

   private Vector2fUtil() {
   }
}
