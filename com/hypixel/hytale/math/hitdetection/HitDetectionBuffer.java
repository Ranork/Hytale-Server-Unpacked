package com.hypixel.hytale.math.hitdetection;

import com.hypixel.hytale.math.shape.Quad4d;
import com.hypixel.hytale.math.shape.Triangle4d;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector4d;
import java.util.Random;

public class HitDetectionBuffer {
   private static final int VECTOR_BUFFER_SIZE = 16;
   public Random random = new FastRandom();
   public Vector4d hitPosition = new Vector4d();
   public Vector4d tempHitPosition = new Vector4d();
   public Quad4d transformedQuad;
   public Vector4d transformedPoint = new Vector4d();
   public Triangle4d visibleTriangle;
   public Vector4dBufferList vertexList1;
   public Vector4dBufferList vertexList2;
   public boolean containsFully;

   public HitDetectionBuffer() {
      this.transformedQuad = new Quad4d();
      this.visibleTriangle = new Triangle4d();
      this.vertexList1 = new Vector4dBufferList(16);
      this.vertexList2 = new Vector4dBufferList(16);
      this.containsFully = false;
   }
}
