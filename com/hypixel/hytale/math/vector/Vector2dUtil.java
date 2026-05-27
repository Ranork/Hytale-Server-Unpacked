package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.codec.Vector2dArrayCodec;
import javax.annotation.Nonnull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public final class Vector2dUtil {
   @Nonnull
   public static final BuilderCodec<Vector2d> CODEC = BuilderCodec.builder(Vector2d.class, Vector2d::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Double>appendInherited(new KeyedCodec<>("X", Codec.DOUBLE), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Double>appendInherited(new KeyedCodec<>("Y", Codec.DOUBLE), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   @Deprecated
   public static final Vector2dArrayCodec AS_ARRAY_CODEC = new Vector2dArrayCodec();
   public static final Vector2dc ZERO = new Vector2d(0.0, 0.0);
   public static final Vector2dc UP = new Vector2d(0.0, 1.0);
   public static final Vector2dc POS_Y = UP;
   public static final Vector2dc DOWN = new Vector2d(0.0, -1.0);
   public static final Vector2dc NEG_Y = DOWN;
   public static final Vector2dc RIGHT = new Vector2d(1.0, 0.0);
   public static final Vector2dc POS_X = RIGHT;
   public static final Vector2dc LEFT = new Vector2d(-1.0, 0.0);
   public static final Vector2dc NEG_X = LEFT;
   public static final Vector2dc ALL_ONES = new Vector2d(1.0, 1.0);
   public static final Vector2dc[] DIRECTIONS = new Vector2dc[]{UP, DOWN, LEFT, RIGHT};

   private Vector2dUtil() {
   }
}
