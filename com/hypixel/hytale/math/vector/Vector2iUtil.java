package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public final class Vector2iUtil {
   @Nonnull
   public static final BuilderCodec<Vector2i> CODEC = BuilderCodec.builder(Vector2i.class, Vector2i::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Integer>appendInherited(new KeyedCodec<>("X", Codec.INTEGER), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Y", Codec.INTEGER), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Vector2ic ZERO = new Vector2i(0, 0);
   public static final Vector2ic UP = new Vector2i(0, 1);
   public static final Vector2ic POS_Y = UP;
   public static final Vector2ic DOWN = new Vector2i(0, -1);
   public static final Vector2ic NEG_Y = DOWN;
   public static final Vector2ic RIGHT = new Vector2i(1, 0);
   public static final Vector2ic POS_X = RIGHT;
   public static final Vector2ic LEFT = new Vector2i(-1, 0);
   public static final Vector2ic NEG_X = LEFT;
   public static final Vector2ic ALL_ONES = new Vector2i(1, 1);
   public static final Vector2ic[] DIRECTIONS = new Vector2ic[]{UP, DOWN, LEFT, RIGHT};

   private Vector2iUtil() {
   }
}
