package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.shape.Cylinder;
import com.hypixel.hytale.math.shape.Ellipsoid;
import com.hypixel.hytale.math.shape.OriginShape;
import com.hypixel.hytale.math.shape.Shape;
import com.hypixel.hytale.math.vector.Vector3d;

public class ShapeCodecs {
   public static final CodecMapCodec<Shape> SHAPE = new CodecMapCodec<>();
   public static final BuilderCodec<Box> BOX = BuilderCodec.builder(Box.class, Box::new)
      .addField(new KeyedCodec<>("Min", Vector3d.CODEC), (shape, min) -> shape.min.assign(min), shape -> shape.min)
      .addField(new KeyedCodec<>("Max", Vector3d.CODEC), (shape, max) -> shape.max.assign(max), shape -> shape.max)
      .build();
   public static final BuilderCodec<Ellipsoid> ELLIPSOID = BuilderCodec.builder(Ellipsoid.class, Ellipsoid::new)
      .addField(new KeyedCodec<>("RadiusX", Codec.DOUBLE), (shape, radius) -> shape.radiusX = radius, shape -> shape.radiusX)
      .addField(new KeyedCodec<>("RadiusY", Codec.DOUBLE), (shape, radius) -> shape.radiusY = radius, shape -> shape.radiusY)
      .addField(new KeyedCodec<>("RadiusZ", Codec.DOUBLE), (shape, radius) -> shape.radiusZ = radius, shape -> shape.radiusZ)
      .addField(new KeyedCodec<>("Radius", Codec.DOUBLE), Ellipsoid::assign, shape -> null)
      .build();
   public static final BuilderCodec<Cylinder> CYLINDER = BuilderCodec.builder(Cylinder.class, Cylinder::new)
      .addField(new KeyedCodec<>("Height", Codec.DOUBLE), (shape, height) -> shape.height = height, shape -> shape.height)
      .addField(new KeyedCodec<>("RadiusX", Codec.DOUBLE), (shape, radiusX) -> shape.radiusX = radiusX, shape -> shape.radiusX)
      .addField(new KeyedCodec<>("RadiusZ", Codec.DOUBLE), (shape, radiusZ) -> shape.radiusZ = radiusZ, shape -> shape.radiusZ)
      .addField(new KeyedCodec<>("Radius", Codec.DOUBLE), Cylinder::assign, shape -> null)
      .build();
   public static final BuilderCodec<OriginShape<Shape>> ORIGIN_SHAPE = BuilderCodec.builder(OriginShape.class, OriginShape::new)
      .addField(new KeyedCodec<>("Origin", Vector3d.CODEC), (shape, origin) -> shape.origin.assign(origin), shape -> shape.origin)
      .addField(new KeyedCodec<>("Shape", SHAPE), (shape, childShape) -> shape.shape = (S)childShape, shape -> shape.shape)
      .build();

   static {
      SHAPE.register("Box", Box.class, BOX);
      SHAPE.register("Ellipsoid", Ellipsoid.class, ELLIPSOID);
      SHAPE.register("Cylinder", Cylinder.class, CYLINDER);
      SHAPE.register("OriginShape", OriginShape.class, ORIGIN_SHAPE);
   }
}
