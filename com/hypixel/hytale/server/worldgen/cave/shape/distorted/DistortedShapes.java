package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class DistortedShapes {
   public static final DistortedShape.Factory CYLINDER = new DistortedCylinderShape.Factory();
   public static final DistortedShape.Factory ELLIPSE = new DistortedEllipsoidShape.Factory();
   public static final DistortedShape.Factory PIPE = new DistortedPipeShape.Factory();
   private static final Map<String, DistortedShape.Factory> SHAPES = new ConcurrentHashMap<>();

   private DistortedShapes() {
   }

   public static void register(String name, DistortedShape.Factory factory) {
      SHAPES.putIfAbsent(name, factory);
   }

   public static void forEach(BiConsumer<String, DistortedShape.Factory> consumer) {
      SHAPES.forEach(consumer);
   }

   public static void forEachName(Consumer<String> consumer) {
      SHAPES.keySet().forEach(consumer);
   }

   public static void forEachShape(Consumer<DistortedShape.Factory> consumer) {
      SHAPES.values().forEach(consumer);
   }

   @Nonnull
   public static DistortedShape.Factory getDefault() {
      return PIPE;
   }

   @Nonnull
   public static DistortedShape.Factory getOrDefault(String name) {
      DistortedShape.Factory factory = SHAPES.get(name);
      return factory == null ? getDefault() : factory;
   }

   @Nullable
   public static DistortedShape.Factory getByName(String name) {
      return SHAPES.get(name);
   }

   static {
      register(CaveNodeShapeEnum.PIPE.name(), PIPE);
      register(CaveNodeShapeEnum.CYLINDER.name(), CYLINDER);
      register(CaveNodeShapeEnum.ELLIPSOID.name(), ELLIPSE);
   }
}
