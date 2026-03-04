package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class OffsetPattern extends Pattern {
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Vector3i offset;
   @Nonnull
   private final SpaceSize readSpaceSize;
   @Nonnull
   private final Vector3i rChildPosition;
   @Nonnull
   private final Pattern.Context rChildContext;

   public OffsetPattern(@Nonnull Pattern pattern, @Nonnull Vector3i offset) {
      this.pattern = pattern;
      this.offset = offset;
      this.readSpaceSize = pattern.readSpace().moveBy(offset);
      this.rChildPosition = new Vector3i();
      this.rChildContext = new Pattern.Context();
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      this.rChildPosition.assign(context.position).add(this.offset);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;
      return this.pattern.matches(this.rChildContext);
   }

   @Nonnull
   @Override
   public SpaceSize readSpace() {
      return this.readSpaceSize.clone();
   }
}
