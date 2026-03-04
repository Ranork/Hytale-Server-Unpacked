package com.hypixel.hytale.math.hitdetection;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Quad4d;
import com.hypixel.hytale.math.shape.Triangle4d;
import com.hypixel.hytale.math.vector.Vector4d;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class HitDetectionExecutor {
   public static final HytaleLogger log = HytaleLogger.forEnclosingClass();
   private static final Vector4d[] VERTEX_POINTS = new Vector4d[]{
      Vector4d.newPosition(0.0, 1.0, 1.0),
      Vector4d.newPosition(0.0, 1.0, 0.0),
      Vector4d.newPosition(1.0, 1.0, 1.0),
      Vector4d.newPosition(1.0, 1.0, 0.0),
      Vector4d.newPosition(0.0, 0.0, 1.0),
      Vector4d.newPosition(0.0, 0.0, 0.0),
      Vector4d.newPosition(1.0, 0.0, 1.0),
      Vector4d.newPosition(1.0, 0.0, 0.0)
   };
   public static final Quad4d[] CUBE_QUADS = new Quad4d[]{
      new Quad4d(VERTEX_POINTS, 0, 1, 3, 2),
      new Quad4d(VERTEX_POINTS, 0, 4, 5, 1),
      new Quad4d(VERTEX_POINTS, 4, 5, 7, 6),
      new Quad4d(VERTEX_POINTS, 2, 3, 7, 6),
      new Quad4d(VERTEX_POINTS, 1, 3, 7, 5),
      new Quad4d(VERTEX_POINTS, 0, 2, 6, 4)
   };
   @Nonnull
   private final Matrix4d pvmMatrix = new Matrix4d();
   @Nonnull
   private final Matrix4d invPvMatrix = new Matrix4d();
   @Nonnull
   private final Vector4d origin = new Vector4d();
   @Nonnull
   private final HitDetectionBuffer buffer = new HitDetectionBuffer();
   private MatrixProvider projectionProvider;
   private MatrixProvider viewProvider;
   private LineOfSightProvider losProvider = LineOfSightProvider.DEFAULT_TRUE;
   private int maxRayTests = 10;

   public Vector4d getHitLocation() {
      return this.buffer.hitPosition;
   }

   @Nonnull
   public HitDetectionExecutor setProjectionProvider(MatrixProvider provider) {
      this.projectionProvider = provider;
      return this;
   }

   @Nonnull
   public HitDetectionExecutor setViewProvider(MatrixProvider provider) {
      this.viewProvider = provider;
      return this;
   }

   @Nonnull
   public HitDetectionExecutor setLineOfSightProvider(LineOfSightProvider losProvider) {
      this.losProvider = losProvider;
      return this;
   }

   @Nonnull
   public HitDetectionExecutor setMaxRayTests(int maxRayTests) {
      this.maxRayTests = maxRayTests;
      return this;
   }

   @Nonnull
   public HitDetectionExecutor setOrigin(double x, double y, double z) {
      this.origin.assign(x, y, z, 1.0);
      return this;
   }

   private void setupMatrices(@Nonnull Matrix4d modelMatrix) {
      Matrix4d projectionMatrix = this.projectionProvider.getMatrix();
      Matrix4d viewMatrix = this.viewProvider.getMatrix();
      this.pvmMatrix.assign(projectionMatrix).multiply(viewMatrix);
      this.invPvMatrix.assign(this.pvmMatrix).invert();
      this.pvmMatrix.multiply(modelMatrix);
   }

   public boolean test(@Nonnull Vector4d point, @Nonnull Matrix4d modelMatrix) {
      this.setupMatrices(modelMatrix);
      return this.testPoint(point);
   }

   public boolean test(@Nonnull Quad4d[] model, @Nonnull Matrix4d modelMatrix) {
      try {
         this.setupMatrices(modelMatrix);
         return this.testModel(model);
      } catch (Throwable var4) {
         ((HytaleLogger.Api)log.at(Level.SEVERE).withCause(var4)).log("Error occured during Hit Detection execution. Dumping parameters!");
         log.at(Level.SEVERE).log("this = %s", this);
         log.at(Level.SEVERE).log("model = %s", Arrays.toString((Object[])model));
         log.at(Level.SEVERE).log("modelMatrix = %s", modelMatrix);
         log.at(Level.SEVERE).log("thread = %s", Thread.currentThread().getName());
         throw var4;
      }
   }

   private boolean testPoint(@Nonnull Vector4d point) {
      this.pvmMatrix.multiply(point, this.buffer.transformedPoint);
      if (!this.buffer.transformedPoint.isInsideFrustum()) {
         return false;
      } else {
         Vector4d hit = this.buffer.transformedPoint;
         this.invPvMatrix.multiply(hit);
         hit.perspectiveTransform();
         return this.losProvider.test(this.origin.x, this.origin.y, this.origin.z, hit.x, hit.y, hit.z);
      }
   }

   private boolean testModel(@Nonnull Quad4d[] model) {
      int testsDone = 0;
      double minDistanceSquared = Double.POSITIVE_INFINITY;

      for (Quad4d quad : model) {
         if (testsDone++ == this.maxRayTests) {
            return false;
         }

         quad.multiply(this.pvmMatrix, this.buffer.transformedQuad);
         if (this.insideFrustum()) {
            Vector4d hit = this.buffer.tempHitPosition;
            if (this.buffer.containsFully) {
               this.buffer.transformedQuad.getRandom(this.buffer.random, hit);
            } else {
               this.buffer.visibleTriangle.getRandom(this.buffer.random, hit);
            }

            this.invPvMatrix.multiply(hit);
            hit.perspectiveTransform();
            double dx = this.origin.x - hit.x;
            double dy = this.origin.y - hit.y;
            double dz = this.origin.z - hit.z;
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (!(distanceSquared >= minDistanceSquared) && this.losProvider.test(this.origin.x, this.origin.y, this.origin.z, hit.x, hit.y, hit.z)) {
               minDistanceSquared = distanceSquared;
               this.buffer.hitPosition.assign(hit);
            }
         }
      }

      return minDistanceSquared != Double.POSITIVE_INFINITY;
   }

   protected boolean insideFrustum() {
      Quad4d quad = this.buffer.transformedQuad;
      if (quad.isFullyInsideFrustum()) {
         this.buffer.containsFully = true;
         return true;
      } else {
         this.buffer.containsFully = false;
         Vector4dBufferList vertices = this.buffer.vertexList1;
         Vector4dBufferList auxillaryList = this.buffer.vertexList2;
         vertices.clear();
         auxillaryList.clear();
         vertices.next().assign(quad.getA());
         vertices.next().assign(quad.getB());
         vertices.next().assign(quad.getC());
         vertices.next().assign(quad.getD());
         if (this.clipPolygonAxis(0) && this.clipPolygonAxis(1) && this.clipPolygonAxis(2)) {
            Vector4d initialVertex = vertices.get(0);
            int i = 1;
            if (i < vertices.size() - 1) {
               Triangle4d triangle = this.buffer.visibleTriangle;
               triangle.assign(initialVertex, vertices.get(i), vertices.get(i + 1));
               return true;
            }
         }

         return false;
      }
   }

   private boolean clipPolygonAxis(int componentIndex) {
      clipPolygonComponent(this.buffer.vertexList1, componentIndex, 1.0, this.buffer.vertexList2);
      this.buffer.vertexList1.clear();
      if (this.buffer.vertexList2.isEmpty()) {
         return false;
      } else {
         clipPolygonComponent(this.buffer.vertexList2, componentIndex, -1.0, this.buffer.vertexList1);
         this.buffer.vertexList2.clear();
         return !this.buffer.vertexList1.isEmpty();
      }
   }

   private static void clipPolygonComponent(
      @Nonnull Vector4dBufferList vertices, int componentIndex, double componentFactor, @Nonnull Vector4dBufferList result
   ) {
      Vector4d previousVertex = vertices.get(vertices.size() - 1);
      double previousComponent = previousVertex.get(componentIndex) * componentFactor;
      boolean previousInside = previousComponent <= previousVertex.w;

      for (int i = 0; i < vertices.size(); i++) {
         Vector4d vertex = vertices.get(i);
         double component = vertex.get(componentIndex) * componentFactor;
         boolean inside = component <= vertex.w;
         if (inside ^ previousInside) {
            double lerp = (previousVertex.w - previousComponent) / (previousVertex.w - previousComponent - (vertex.w - component));
            previousVertex.lerp(vertex, lerp, result.next());
         }

         if (inside) {
            result.next().assign(vertex);
         }

         previousVertex = vertex;
         previousComponent = component;
         previousInside = inside;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "HitDetectionExecutor{pvmMatrix="
         + this.pvmMatrix
         + ", invPvMatrix="
         + this.invPvMatrix
         + ", origin="
         + this.origin
         + ", buffer="
         + this.buffer
         + ", projectionProvider="
         + this.projectionProvider
         + ", viewProvider="
         + this.viewProvider
         + ", losProvider="
         + this.losProvider
         + ", maxRayTests="
         + this.maxRayTests
         + "}";
   }
}
