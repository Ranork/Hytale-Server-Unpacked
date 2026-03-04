package com.hypixel.hytale.procedurallib.supplier;

@FunctionalInterface
public interface ISeedDoubleRange {
   ISeedDoubleRange DIRECT = (seed, value) -> value;

   double getValue(int var1, double var2);
}
