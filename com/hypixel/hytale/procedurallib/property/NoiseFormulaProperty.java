package com.hypixel.hytale.procedurallib.property;

import javax.annotation.Nonnull;

public class NoiseFormulaProperty implements NoiseProperty {
   protected final NoiseProperty property;
   protected final NoiseFormulaProperty.NoiseFormula.Formula formula;

   public NoiseFormulaProperty(NoiseProperty property, NoiseFormulaProperty.NoiseFormula.Formula formula) {
      this.property = property;
      this.formula = formula;
   }

   public NoiseProperty getProperty() {
      return this.property;
   }

   public NoiseFormulaProperty.NoiseFormula.Formula getFormula() {
      return this.formula;
   }

   @Override
   public double get(int seed, double x, double y) {
      return this.formula.eval(this.property.get(seed, x, y));
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      return this.formula.eval(this.property.get(seed, x, y, z));
   }

   @Nonnull
   @Override
   public String toString() {
      return "NoiseFormulaProperty{property=" + this.property + ", formula=" + this.formula + "}";
   }

   public static enum NoiseFormula {
      NORMAL(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "NormalFormula{}";
         }
      }),
      INVERTED(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedFormula{}";
         }
      }),
      SQUARED(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return noise * noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "SquaredFormula{}";
         }
      }),
      INVERTED_SQUARED(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - noise * noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedSquaredFormula{}";
         }
      }),
      SQRT(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return Math.sqrt(noise);
         }

         @Nonnull
         @Override
         public String toString() {
            return "SqrtFormula{}";
         }
      }),
      INVERTED_SQRT(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - Math.sqrt(noise);
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedSqrtFormula{}";
         }
      }),
      RIDGED(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return Math.abs(noise - 0.5);
         }

         @Nonnull
         @Override
         public String toString() {
            return "RidgedFormula{}";
         }
      }),
      INVERTED_RIDGED(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - Math.abs(noise - 0.5);
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedRidgedFormula{}";
         }
      }),
      RIDGED_SQRT(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return Math.sqrt(Math.abs(noise - 0.5));
         }

         @Nonnull
         @Override
         public String toString() {
            return "RidgedSqrtFormula{}";
         }
      }),
      INVERTED_RIDGED_SQRT(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - Math.sqrt(Math.abs(noise - 0.5));
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedRidgedSqrtFormula{}";
         }
      }),
      RIDGED_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return Math.abs(noise * 2.0 - 1.0);
         }

         @Nonnull
         @Override
         public String toString() {
            return "RidgedFixFormula{}";
         }
      }),
      INVERTED_RIDGED_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - Math.abs(noise * 2.0 - 1.0);
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedRidgedFixFormula{}";
         }
      }),
      RIDGED_SQRT_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return Math.sqrt(Math.abs(noise * 2.0 - 1.0));
         }

         @Nonnull
         @Override
         public String toString() {
            return "RidgedSqrtFixFormula{}";
         }
      }),
      INVERTED_RIDGED_SQRT_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            return 1.0 - Math.sqrt(Math.abs(noise * 2.0 - 1.0));
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedRidgedSqrtFixFormula{}";
         }
      }),
      RIDGED_SQUARED_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            noise = Math.abs(noise * 2.0 - 1.0);
            return noise * noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "RidgedSquaredFixFormula{}";
         }
      }),
      INVERTED_RIDGED_SQUARED_FIX(new NoiseFormulaProperty.NoiseFormula.Formula() {
         @Override
         public double eval(double noise) {
            noise = Math.abs(noise * 2.0 - 1.0);
            return 1.0 - noise * noise;
         }

         @Nonnull
         @Override
         public String toString() {
            return "InvertedRidgedSquaredFixFormula{}";
         }
      });

      public final NoiseFormulaProperty.NoiseFormula.Formula formula;

      private NoiseFormula(NoiseFormulaProperty.NoiseFormula.Formula formula) {
         this.formula = formula;
      }

      public NoiseFormulaProperty.NoiseFormula.Formula getFormula() {
         return this.formula;
      }

      @FunctionalInterface
      public interface Formula {
         double eval(double var1);
      }
   }
}
