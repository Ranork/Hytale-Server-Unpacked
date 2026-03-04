package com.hypixel.hytale.builtin.hytalegenerator.props.prefab;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import javax.annotation.Nonnull;

public class PrefabMoldingConfiguration {
   public final Scanner moldingScanner;
   public final Pattern moldingPattern;
   public final MoldingDirection moldingDirection;
   public final boolean moldChildren;

   public PrefabMoldingConfiguration(Scanner moldingScanner, Pattern moldingPattern, MoldingDirection moldingDirection, boolean moldChildren) {
      this.moldingScanner = moldingScanner;
      this.moldingPattern = moldingPattern;
      this.moldingDirection = moldingDirection;
      this.moldChildren = moldChildren;
   }

   @Nonnull
   public static PrefabMoldingConfiguration none() {
      return new PrefabMoldingConfiguration(null, null, MoldingDirection.NONE, false);
   }
}
