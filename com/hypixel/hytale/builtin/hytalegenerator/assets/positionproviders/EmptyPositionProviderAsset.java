package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyPositionProviderAsset extends PositionProviderAsset {
   public static final EmptyPositionProviderAsset INSTANCE = new EmptyPositionProviderAsset();

   @Override
   public PositionProvider build(@NonNullDecl PositionProviderAsset.Argument argument) {
      return EmptyPositionProvider.INSTANCE;
   }
}
