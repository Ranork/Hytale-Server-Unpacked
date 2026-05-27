package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public final class Vector3iUtil {
   @Nonnull
   public static final BuilderCodec<Vector3i> CODEC = BuilderCodec.builder(Vector3i.class, Vector3i::new)
      .metadata(UIDisplayMode.COMPACT)
      .<Integer>appendInherited(new KeyedCodec<>("X", Codec.INTEGER), (o, i) -> o.x = i, o -> o.x, (o, p) -> o.x = p.x)
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Y", Codec.INTEGER), (o, i) -> o.y = i, o -> o.y, (o, p) -> o.y = p.y)
      .addValidator(Validators.nonNull())
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Z", Codec.INTEGER), (o, i) -> o.z = i, o -> o.z, (o, p) -> o.z = p.z)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   public static final Vector3ic ZERO = new Vector3i(0, 0, 0);
   public static final Vector3ic UP = new Vector3i(0, 1, 0);
   public static final Vector3ic DOWN = new Vector3i(0, -1, 0);
   public static final Vector3ic FORWARD = new Vector3i(0, 0, -1);
   public static final Vector3ic BACKWARD = new Vector3i(0, 0, 1);
   public static final Vector3ic LEFT = new Vector3i(-1, 0, 0);
   public static final Vector3ic RIGHT = new Vector3i(1, 0, 0);
   public static final Vector3ic POS_X = RIGHT;
   public static final Vector3ic POS_Z = BACKWARD;
   public static final Vector3ic POS_Y = UP;
   public static final Vector3ic NEG_X = LEFT;
   public static final Vector3ic NEG_Y = DOWN;
   public static final Vector3ic NEG_Z = FORWARD;
   public static final Vector3ic NORTH = FORWARD;
   public static final Vector3ic SOUTH = BACKWARD;
   public static final Vector3ic EAST = RIGHT;
   public static final Vector3ic WEST = LEFT;
   public static final Vector3ic[] BLOCK_CORNERS = new Vector3ic[]{
      add(UP, FORWARD, LEFT),
      add(UP, FORWARD, RIGHT),
      add(DOWN, FORWARD, LEFT),
      add(DOWN, FORWARD, RIGHT),
      add(UP, BACKWARD, LEFT),
      add(UP, BACKWARD, RIGHT),
      add(DOWN, BACKWARD, LEFT),
      add(DOWN, BACKWARD, RIGHT)
   };
   public static final Vector3ic[] BLOCK_SIDES = new Vector3ic[]{UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT};
   public static final Vector3ic[] BLOCK_EDGES = new Vector3ic[]{
      add(UP, FORWARD),
      add(DOWN, FORWARD),
      add(UP, BACKWARD),
      add(DOWN, BACKWARD),
      add(UP, LEFT),
      add(DOWN, LEFT),
      add(UP, RIGHT),
      add(DOWN, RIGHT),
      add(FORWARD, LEFT),
      add(FORWARD, RIGHT),
      add(BACKWARD, LEFT),
      add(BACKWARD, RIGHT)
   };
   public static final Vector3ic[][] BLOCK_PARTS = new Vector3ic[][]{BLOCK_SIDES, BLOCK_EDGES, BLOCK_CORNERS};
   public static final Vector3ic ALL_ONES = new Vector3i(1, 1, 1);
   public static final Vector3ic MIN = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   public static final Vector3ic MAX = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

   private Vector3iUtil() {
   }

   @Nonnull
   public static Vector3i max(@Nonnull Vector3ic a, @Nonnull Vector3ic b) {
      return a.max(b, new Vector3i());
   }

   @Nonnull
   public static Vector3i min(@Nonnull Vector3ic a, @Nonnull Vector3ic b) {
      return a.min(b, new Vector3i());
   }

   @Nonnull
   private static Vector3i add(@Nonnull Vector3ic one, @Nonnull Vector3ic two) {
      return new Vector3i(one).add(two);
   }

   @Nonnull
   private static Vector3i add(@Nonnull Vector3ic one, @Nonnull Vector3ic two, @Nonnull Vector3ic three) {
      return new Vector3i(one).add(two).add(three);
   }

   public static Vector3d toVector3d(Vector3ic vec) {
      return new Vector3d(vec.x(), vec.y(), vec.z());
   }
}
