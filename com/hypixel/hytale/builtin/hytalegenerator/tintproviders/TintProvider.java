package com.hypixel.hytale.builtin.hytalegenerator.tintproviders;

import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import javax.annotation.Nonnull;

public abstract class TintProvider {
   public static final int DEFAULT_TINT = ColorParseUtil.colorToARGBInt(new Color((byte)91, (byte)-98, (byte)40));

   public abstract TintProvider.Result getValue(@Nonnull TintProvider.Context var1);

   @Nonnull
   public static TintProvider noTintProvider() {
      return new ConstantTintProvider(DEFAULT_TINT);
   }

   public static class Context {
      public Vector3i position;
      public WorkerIndexer.Id workerId;

      public Context(@Nonnull Vector3i position, WorkerIndexer.Id workerId) {
         this.position = position;
         this.workerId = workerId;
      }

      public Context(@Nonnull TintProvider.Context other) {
         this.position = other.position;
         this.workerId = other.workerId;
      }
   }

   public static class Result {
      @Nonnull
      public static final TintProvider.Result WITHOUT_VALUE = new TintProvider.Result();
      public final int tint;
      public final boolean hasValue;

      public Result(int tint) {
         this.tint = tint;
         this.hasValue = true;
      }

      public Result() {
         this.tint = TintProvider.DEFAULT_TINT;
         this.hasValue = false;
      }
   }
}
