package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class OriginScanner extends Scanner {
   @Nonnull
   private static final OriginScanner instance = new OriginScanner();
   @Nonnull
   private static final SpaceSize SCAN_SPACE_SIZE = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1));

   private OriginScanner() {
   }

   @Nonnull
   @Override
   public List<Vector3i> scan(@Nonnull Scanner.Context context) {
      Pattern.Context patternContext = new Pattern.Context(context.position, context.materialSpace);
      return context.pattern.matches(patternContext) ? Collections.singletonList(context.position.clone()) : Collections.emptyList();
   }

   @Nonnull
   @Override
   public SpaceSize scanSpace() {
      return SCAN_SPACE_SIZE.clone();
   }

   @Nonnull
   public static OriginScanner getInstance() {
      return instance;
   }
}
