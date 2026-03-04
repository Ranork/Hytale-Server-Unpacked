package com.hypixel.hytale.builtin.buildertools.objimport;

import com.hypixel.hytale.builtin.buildertools.BlockColorIndex;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MeshVoxelizer {
   private MeshVoxelizer() {
   }

   @Nonnull
   public static MeshVoxelizer.VoxelResult voxelize(@Nonnull ObjParser.ObjMesh mesh, int targetHeight, boolean fillSolid) {
      return voxelize(mesh, targetHeight, fillSolid, null, null, null, 0, false);
   }

   @Nonnull
   public static MeshVoxelizer.VoxelResult voxelize(
      @Nonnull ObjParser.ObjMesh mesh, int targetHeight, boolean fillSolid, @Nullable Map<String, Integer> materialToBlockId
   ) {
      return voxelize(mesh, targetHeight, fillSolid, null, materialToBlockId, null, 0, false);
   }

   @Nonnull
   public static MeshVoxelizer.VoxelResult voxelize(
      @Nonnull ObjParser.ObjMesh mesh, int targetHeight, boolean fillSolid, @Nullable Map<String, Integer> materialToBlockId, int defaultBlockId
   ) {
      return voxelize(mesh, targetHeight, fillSolid, null, materialToBlockId, null, defaultBlockId, false);
   }

   @Nonnull
   public static MeshVoxelizer.VoxelResult voxelize(
      @Nonnull ObjParser.ObjMesh mesh,
      int targetHeight,
      boolean fillSolid,
      @Nullable Map<String, BufferedImage> materialTextures,
      @Nullable Map<String, Integer> materialToBlockId,
      @Nullable BlockColorIndex colorIndex,
      int defaultBlockId
   ) {
      return voxelize(mesh, targetHeight, fillSolid, materialTextures, materialToBlockId, colorIndex, defaultBlockId, false);
   }

   @Nonnull
   public static MeshVoxelizer.VoxelResult voxelize(
      @Nonnull ObjParser.ObjMesh mesh,
      int targetHeight,
      boolean fillSolid,
      @Nullable Map<String, BufferedImage> materialTextures,
      @Nullable Map<String, Integer> materialToBlockId,
      @Nullable BlockColorIndex colorIndex,
      int defaultBlockId,
      boolean preserveOrigin
   ) {
      float[] bounds = mesh.getBounds();
      float meshHeight = bounds[4] - bounds[1];
      float meshWidth = bounds[3] - bounds[0];
      float meshDepth = bounds[5] - bounds[2];
      if (meshHeight <= 0.0F) {
         return new MeshVoxelizer.VoxelResult(new boolean[1][1][1], null, 1, 1, 1);
      } else {
         float scale = targetHeight / meshHeight;
         float[][] scaledVertices = new float[mesh.vertices().size()][3];
         int sizeX;
         int sizeY;
         int sizeZ;
         if (preserveOrigin) {
            float scaledMinX = bounds[0] * scale;
            float scaledMaxX = bounds[3] * scale;
            float scaledMinY = bounds[1] * scale;
            float scaledMaxY = bounds[4] * scale;
            float scaledMinZ = bounds[2] * scale;
            float scaledMaxZ = bounds[5] * scale;
            float offsetX = scaledMinX < 0.0F ? -scaledMinX + 1.0F : 1.0F;
            float offsetY = scaledMinY < 0.0F ? -scaledMinY + 1.0F : 1.0F;
            float offsetZ = scaledMinZ < 0.0F ? -scaledMinZ + 1.0F : 1.0F;

            for (int i = 0; i < mesh.vertices().size(); i++) {
               float[] v = mesh.vertices().get(i);
               scaledVertices[i][0] = v[0] * scale + offsetX;
               scaledVertices[i][1] = v[1] * scale + offsetY;
               scaledVertices[i][2] = v[2] * scale + offsetZ;
            }

            sizeX = Math.max(1, (int)Math.ceil(scaledMaxX + offsetX)) + 2;
            sizeY = Math.max(1, (int)Math.ceil(scaledMaxY + offsetY)) + 2;
            sizeZ = Math.max(1, (int)Math.ceil(scaledMaxZ + offsetZ)) + 2;
         } else {
            sizeX = Math.max(1, (int)Math.ceil(meshWidth * scale)) + 2;
            sizeY = Math.max(1, targetHeight) + 2;
            sizeZ = Math.max(1, (int)Math.ceil(meshDepth * scale)) + 2;

            for (int i = 0; i < mesh.vertices().size(); i++) {
               float[] v = mesh.vertices().get(i);
               scaledVertices[i][0] = (v[0] - bounds[0]) * scale + 1.0F;
               scaledVertices[i][1] = (v[1] - bounds[1]) * scale + 1.0F;
               scaledVertices[i][2] = (v[2] - bounds[2]) * scale + 1.0F;
            }
         }

         boolean[][][] shell = new boolean[sizeX][sizeY][sizeZ];
         boolean hasTextures = materialTextures != null && !materialTextures.isEmpty() && colorIndex != null;
         int[][][] blockIds = !hasTextures && materialToBlockId == null && defaultBlockId == 0 ? null : new int[sizeX][sizeY][sizeZ];
         rasterizeSurface(shell, blockIds, scaledVertices, mesh, materialTextures, materialToBlockId, colorIndex, defaultBlockId, sizeX, sizeY, sizeZ);
         if (fillSolid) {
            boolean[][][] solid = floodFillSolid(shell, sizeX, sizeY, sizeZ);
            if (blockIds != null) {
               fillInteriorBlockIds(solid, shell, blockIds, defaultBlockId, sizeX, sizeY, sizeZ);
            }

            return cropToSolidBounds(solid, blockIds, sizeX, sizeY, sizeZ);
         } else {
            return cropToSolidBounds(shell, blockIds, sizeX, sizeY, sizeZ);
         }
      }
   }

   private static int resolveIndex(int index, int count) {
      return index < 0 ? count + index : index;
   }

   private static void rasterizeSurface(
      boolean[][][] voxels,
      @Nullable int[][][] blockIds,
      float[][] vertices,
      ObjParser.ObjMesh mesh,
      @Nullable Map<String, BufferedImage> materialTextures,
      @Nullable Map<String, Integer> materialToBlockId,
      @Nullable BlockColorIndex colorIndex,
      int defaultBlockId,
      int sizeX,
      int sizeY,
      int sizeZ
   ) {
      List<int[]> faces = mesh.faces();
      List<int[]> faceUvIndices = mesh.faceUvIndices();
      List<float[]> uvCoordinates = mesh.uvCoordinates();
      List<String> faceMaterials = mesh.faceMaterials();
      boolean hasTextures = materialTextures != null && !materialTextures.isEmpty() && colorIndex != null;

      for (int faceIdx = 0; faceIdx < faces.size(); faceIdx++) {
         int[] face = faces.get(faceIdx);
         int i0 = resolveIndex(face[0], vertices.length);
         int i1 = resolveIndex(face[1], vertices.length);
         int i2 = resolveIndex(face[2], vertices.length);
         float[] v0 = vertices[i0];
         float[] v1 = vertices[i1];
         float[] v2 = vertices[i2];
         String material = faceIdx < faceMaterials.size() ? faceMaterials.get(faceIdx) : null;
         BufferedImage texture = null;
         int faceBlockId = defaultBlockId;
         if (material != null) {
            if (hasTextures) {
               texture = materialTextures.get(material);
            }

            if (texture == null && materialToBlockId != null) {
               faceBlockId = materialToBlockId.getOrDefault(material, defaultBlockId);
            }
         }

         float[] uv0 = null;
         float[] uv1 = null;
         float[] uv2 = null;
         if (texture != null && faceIdx < faceUvIndices.size()) {
            int[] uvIndices = faceUvIndices.get(faceIdx);
            if (uvIndices != null && uvIndices.length >= 3) {
               int uvCount = uvCoordinates.size();
               int ui0 = resolveIndex(uvIndices[0], uvCount);
               int ui1 = resolveIndex(uvIndices[1], uvCount);
               int ui2 = resolveIndex(uvIndices[2], uvCount);
               if (ui0 >= 0 && ui0 < uvCount) {
                  uv0 = uvCoordinates.get(ui0);
               }

               if (ui1 >= 0 && ui1 < uvCount) {
                  uv1 = uvCoordinates.get(ui1);
               }

               if (ui2 >= 0 && ui2 < uvCount) {
                  uv2 = uvCoordinates.get(ui2);
               }
            }
         }

         rasterizeLine(voxels, blockIds, v0, v1, uv0, uv1, texture, colorIndex, faceBlockId, sizeX, sizeY, sizeZ);
         rasterizeLine(voxels, blockIds, v1, v2, uv1, uv2, texture, colorIndex, faceBlockId, sizeX, sizeY, sizeZ);
         rasterizeLine(voxels, blockIds, v2, v0, uv2, uv0, texture, colorIndex, faceBlockId, sizeX, sizeY, sizeZ);
         rasterizeTriangle(voxels, blockIds, v0, v1, v2, uv0, uv1, uv2, texture, colorIndex, faceBlockId, sizeX, sizeY, sizeZ);
      }
   }

   private static void rasterizeLine(
      boolean[][][] voxels,
      @Nullable int[][][] blockIds,
      float[] a,
      float[] b,
      @Nullable float[] uvA,
      @Nullable float[] uvB,
      @Nullable BufferedImage texture,
      @Nullable BlockColorIndex colorIndex,
      int fallbackBlockId,
      int sizeX,
      int sizeY,
      int sizeZ
   ) {
      float dx = b[0] - a[0];
      float dy = b[1] - a[1];
      float dz = b[2] - a[2];
      float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len < 0.001F) {
         int blockId = sampleBlockId(uvA, texture, colorIndex, fallbackBlockId);
         setVoxel(voxels, blockIds, (int)a[0], (int)a[1], (int)a[2], blockId, sizeX, sizeY, sizeZ);
      } else {
         int steps = (int)Math.ceil(len * 2.0F) + 1;

         for (int i = 0; i <= steps; i++) {
            float t = (float)i / steps;
            float x = a[0] + dx * t;
            float y = a[1] + dy * t;
            float z = a[2] + dz * t;
            float[] uv = interpolateUv(uvA, uvB, t);
            int blockId = sampleBlockId(uv, texture, colorIndex, fallbackBlockId);
            setVoxel(voxels, blockIds, (int)x, (int)y, (int)z, blockId, sizeX, sizeY, sizeZ);
         }
      }
   }

   @Nullable
   private static float[] interpolateUv(@Nullable float[] uvA, @Nullable float[] uvB, float t) {
      return uvA != null && uvB != null ? new float[]{uvA[0] + (uvB[0] - uvA[0]) * t, uvA[1] + (uvB[1] - uvA[1]) * t} : uvA;
   }

   private static int sampleBlockId(@Nullable float[] uv, @Nullable BufferedImage texture, @Nullable BlockColorIndex colorIndex, int fallbackBlockId) {
      if (uv != null && texture != null && colorIndex != null) {
         int alpha = TextureSampler.sampleAlphaAt(texture, uv[0], uv[1]);
         if (alpha < 128) {
            return 0;
         } else {
            int[] rgb = TextureSampler.sampleAt(texture, uv[0], uv[1]);
            int blockId = colorIndex.findClosestBlock(rgb[0], rgb[1], rgb[2]);
            return blockId > 0 ? blockId : fallbackBlockId;
         }
      } else {
         return fallbackBlockId;
      }
   }

   private static void setVoxel(boolean[][][] voxels, @Nullable int[][][] blockIds, int x, int y, int z, int blockId, int sizeX, int sizeY, int sizeZ) {
      if (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ) {
         voxels[x][y][z] = true;
         if (blockIds != null && blockId != 0 && blockIds[x][y][z] == 0) {
            blockIds[x][y][z] = blockId;
         }
      }
   }

   private static void rasterizeTriangle(
      boolean[][][] voxels,
      @Nullable int[][][] blockIds,
      float[] v0,
      float[] v1,
      float[] v2,
      @Nullable float[] uv0,
      @Nullable float[] uv1,
      @Nullable float[] uv2,
      @Nullable BufferedImage texture,
      @Nullable BlockColorIndex colorIndex,
      int fallbackBlockId,
      int sizeX,
      int sizeY,
      int sizeZ
   ) {
      float minX = Math.min(v0[0], Math.min(v1[0], v2[0]));
      float maxX = Math.max(v0[0], Math.max(v1[0], v2[0]));
      float minY = Math.min(v0[1], Math.min(v1[1], v2[1]));
      float maxY = Math.max(v0[1], Math.max(v1[1], v2[1]));
      float minZ = Math.min(v0[2], Math.min(v1[2], v2[2]));
      float maxZ = Math.max(v0[2], Math.max(v1[2], v2[2]));
      int startX = Math.max(0, (int)Math.floor(minX) - 1);
      int endX = Math.min(sizeX - 1, (int)Math.ceil(maxX) + 1);
      int startY = Math.max(0, (int)Math.floor(minY) - 1);
      int endY = Math.min(sizeY - 1, (int)Math.ceil(maxY) + 1);
      int startZ = Math.max(0, (int)Math.floor(minZ) - 1);
      int endZ = Math.min(sizeZ - 1, (int)Math.ceil(maxZ) + 1);
      boolean hasUvSampling = uv0 != null && uv1 != null && uv2 != null && texture != null && colorIndex != null;

      for (int x = startX; x <= endX; x++) {
         for (int y = startY; y <= endY; y++) {
            for (int z = startZ; z <= endZ; z++) {
               float px = x + 0.5F;
               float py = y + 0.5F;
               float pz = z + 0.5F;
               if (pointNearTriangle(px, py, pz, v0, v1, v2, 0.87F)) {
                  int blockId = fallbackBlockId;
                  if (hasUvSampling) {
                     float[] bary = barycentric(px, py, pz, v0, v1, v2);
                     if (bary != null) {
                        float u = bary[0] * uv0[0] + bary[1] * uv1[0] + bary[2] * uv2[0];
                        float v = bary[0] * uv0[1] + bary[1] * uv1[1] + bary[2] * uv2[1];
                        int alpha = TextureSampler.sampleAlphaAt(texture, u, v);
                        if (alpha < 128) {
                           continue;
                        }

                        int[] rgb = TextureSampler.sampleAt(texture, u, v);
                        int sampledId = colorIndex.findClosestBlock(rgb[0], rgb[1], rgb[2]);
                        if (sampledId > 0) {
                           blockId = sampledId;
                        }
                     }
                  }

                  voxels[x][y][z] = true;
                  if (blockIds != null && blockId != 0 && blockIds[x][y][z] == 0) {
                     blockIds[x][y][z] = blockId;
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static float[] barycentric(float px, float py, float pz, float[] v0, float[] v1, float[] v2) {
      float[] e1 = new float[]{v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
      float[] e2 = new float[]{v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
      float nx = e1[1] * e2[2] - e1[2] * e2[1];
      float ny = e1[2] * e2[0] - e1[0] * e2[2];
      float nz = e1[0] * e2[1] - e1[1] * e2[0];
      float ax = Math.abs(nx);
      float ay = Math.abs(ny);
      float az = Math.abs(nz);
      float u0;
      float u1;
      float u2;
      float v0c;
      float v1c;
      float v2c;
      float pu;
      float pv;
      if (ax >= ay && ax >= az) {
         u0 = v0[1];
         v0c = v0[2];
         u1 = v1[1];
         v1c = v1[2];
         u2 = v2[1];
         v2c = v2[2];
         pu = py;
         pv = pz;
      } else if (ay >= ax && ay >= az) {
         u0 = v0[0];
         v0c = v0[2];
         u1 = v1[0];
         v1c = v1[2];
         u2 = v2[0];
         v2c = v2[2];
         pu = px;
         pv = pz;
      } else {
         u0 = v0[0];
         v0c = v0[1];
         u1 = v1[0];
         v1c = v1[1];
         u2 = v2[0];
         v2c = v2[1];
         pu = px;
         pv = py;
      }

      float denom = (v1c - v2c) * (u0 - u2) + (u2 - u1) * (v0c - v2c);
      if (Math.abs(denom) < 1.0E-10F) {
         return null;
      } else {
         float w0 = ((v1c - v2c) * (pu - u2) + (u2 - u1) * (pv - v2c)) / denom;
         float w1 = ((v2c - v0c) * (pu - u2) + (u0 - u2) * (pv - v2c)) / denom;
         float w2 = 1.0F - w0 - w1;
         return new float[]{w0, w1, w2};
      }
   }

   private static boolean pointNearTriangle(float px, float py, float pz, float[] v0, float[] v1, float[] v2, float threshold) {
      float e1x = v1[0] - v0[0];
      float e1y = v1[1] - v0[1];
      float e1z = v1[2] - v0[2];
      float e2x = v2[0] - v0[0];
      float e2y = v2[1] - v0[1];
      float e2z = v2[2] - v0[2];
      float nx = e1y * e2z - e1z * e2y;
      float ny = e1z * e2x - e1x * e2z;
      float nz = e1x * e2y - e1y * e2x;
      float lenSq = nx * nx + ny * ny + nz * nz;
      if (lenSq < 1.0E-12F) {
         return false;
      } else {
         float len = (float)Math.sqrt(lenSq);
         float dpx = px - v0[0];
         float dpy = py - v0[1];
         float dpz = pz - v0[2];
         float dotNP = nx * dpx + ny * dpy + nz * dpz;
         float dist = Math.abs(dotNP) / len;
         if (dist > threshold) {
            return false;
         } else {
            float t = dotNP / lenSq;
            float projX = px - t * nx;
            float projY = py - t * ny;
            float projZ = pz - t * nz;
            return pointInTriangleWithTolerance(projX, projY, projZ, v0, v1, v2, 0.1F);
         }
      }
   }

   private static boolean pointInTriangleWithTolerance(float px, float py, float pz, float[] v0, float[] v1, float[] v2, float tolerance) {
      float vax = v1[0] - v0[0];
      float vay = v1[1] - v0[1];
      float vaz = v1[2] - v0[2];
      float vbx = v2[0] - v0[0];
      float vby = v2[1] - v0[1];
      float vbz = v2[2] - v0[2];
      float vpx = px - v0[0];
      float vpy = py - v0[1];
      float vpz = pz - v0[2];
      float d00 = vax * vax + vay * vay + vaz * vaz;
      float d01 = vax * vbx + vay * vby + vaz * vbz;
      float d11 = vbx * vbx + vby * vby + vbz * vbz;
      float d20 = vpx * vax + vpy * vay + vpz * vaz;
      float d21 = vpx * vbx + vpy * vby + vpz * vbz;
      float denom = d00 * d11 - d01 * d01;
      if (Math.abs(denom) < 1.0E-12F) {
         return false;
      } else {
         float u = (d11 * d20 - d01 * d21) / denom;
         float v = (d00 * d21 - d01 * d20) / denom;
         return u >= -tolerance && v >= -tolerance && u + v <= 1.0F + tolerance;
      }
   }

   private static boolean[][][] floodFillSolid(boolean[][][] shell, int sizeX, int sizeY, int sizeZ) {
      int dx = sizeX + 2;
      int dy = sizeY + 2;
      int dz = sizeZ + 2;
      int plane = dx * dy;
      int total = plane * dz;
      boolean[] visited = new boolean[total];
      int[] queue = new int[total];
      int qh = 0;
      int qt = 0;
      visited[0] = true;
      queue[qt++] = 0;

      while (qh < qt) {
         int idx = queue[qh++];
         int x = idx % dx;
         int y = idx / dx % dy;
         int z = idx / plane;
         if (x + 1 < dx && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x + 1, y, z, dx, plane, qt)) {
            qt++;
         }

         if (x - 1 >= 0 && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x - 1, y, z, dx, plane, qt)) {
            qt++;
         }

         if (y + 1 < dy && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x, y + 1, z, dx, plane, qt)) {
            qt++;
         }

         if (y - 1 >= 0 && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x, y - 1, z, dx, plane, qt)) {
            qt++;
         }

         if (z + 1 < dz && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x, y, z + 1, dx, plane, qt)) {
            qt++;
         }

         if (z - 1 >= 0 && tryEnqueue(shell, sizeX, sizeY, sizeZ, visited, queue, x, y, z - 1, dx, plane, qt)) {
            qt++;
         }
      }

      boolean[][][] solid = new boolean[sizeX][sizeY][sizeZ];

      for (int xx = 0; xx < sizeX; xx++) {
         for (int yx = 0; yx < sizeY; yx++) {
            for (int zx = 0; zx < sizeZ; zx++) {
               int ex = xx + 1;
               int ey = yx + 1;
               int ez = zx + 1;
               int eIdx = ex + ey * dx + ez * plane;
               solid[xx][yx][zx] = !visited[eIdx];
            }
         }
      }

      return solid;
   }

   private static boolean tryEnqueue(
      boolean[][][] shell, int sizeX, int sizeY, int sizeZ, boolean[] visited, int[] queue, int ex, int ey, int ez, int dx, int plane, int writeIndex
   ) {
      int idx = ex + ey * dx + ez * plane;
      if (visited[idx]) {
         return false;
      } else {
         int x = ex - 1;
         int y = ey - 1;
         int z = ez - 1;
         if (x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ && shell[x][y][z]) {
            return false;
         } else {
            visited[idx] = true;
            queue[writeIndex] = idx;
            return true;
         }
      }
   }

   private static MeshVoxelizer.VoxelResult cropToSolidBounds(boolean[][][] voxels, @Nullable int[][][] blockIds, int sizeX, int sizeY, int sizeZ) {
      int minX = sizeX;
      int minY = sizeY;
      int minZ = sizeZ;
      int maxX = -1;
      int maxY = -1;
      int maxZ = -1;

      for (int x = 0; x < sizeX; x++) {
         for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
               if (voxels[x][y][z]) {
                  if (x < minX) {
                     minX = x;
                  }

                  if (y < minY) {
                     minY = y;
                  }

                  if (z < minZ) {
                     minZ = z;
                  }

                  if (x > maxX) {
                     maxX = x;
                  }

                  if (y > maxY) {
                     maxY = y;
                  }

                  if (z > maxZ) {
                     maxZ = z;
                  }
               }
            }
         }
      }

      if (maxX >= minX && maxY >= minY && maxZ >= minZ) {
         int outX = maxX - minX + 1;
         int outY = maxY - minY + 1;
         int outZ = maxZ - minZ + 1;
         boolean[][][] out = new boolean[outX][outY][outZ];
         int[][][] outBlockIds = blockIds != null ? new int[outX][outY][outZ] : null;

         for (int x = 0; x < outX; x++) {
            for (int y = 0; y < outY; y++) {
               System.arraycopy(voxels[minX + x][minY + y], minZ, out[x][y], 0, outZ);
               if (outBlockIds != null && blockIds != null) {
                  System.arraycopy(blockIds[minX + x][minY + y], minZ, outBlockIds[x][y], 0, outZ);
               }
            }
         }

         return new MeshVoxelizer.VoxelResult(out, outBlockIds, outX, outY, outZ);
      } else {
         return new MeshVoxelizer.VoxelResult(new boolean[1][1][1], null, 1, 1, 1);
      }
   }

   private static void fillInteriorBlockIds(boolean[][][] solid, boolean[][][] shell, int[][][] blockIds, int defaultBlockId, int sizeX, int sizeY, int sizeZ) {
      for (int x = 0; x < sizeX; x++) {
         for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
               if (solid[x][y][z] && !shell[x][y][z] && blockIds[x][y][z] == 0) {
                  int bestId = findNearestSurfaceBlockId(blockIds, shell, x, y, z, sizeX, sizeY, sizeZ);
                  blockIds[x][y][z] = bestId != 0 ? bestId : defaultBlockId;
               }
            }
         }
      }
   }

   private static int findNearestSurfaceBlockId(int[][][] blockIds, boolean[][][] shell, int cx, int cy, int cz, int sizeX, int sizeY, int sizeZ) {
      for (int radius = 1; radius <= 5; radius++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  int nx = cx + dx;
                  int ny = cy + dy;
                  int nz = cz + dz;
                  if (nx >= 0 && nx < sizeX && ny >= 0 && ny < sizeY && nz >= 0 && nz < sizeZ && shell[nx][ny][nz] && blockIds[nx][ny][nz] != 0) {
                     return blockIds[nx][ny][nz];
                  }
               }
            }
         }
      }

      return 0;
   }

   public record VoxelResult(boolean[][][] voxels, @Nullable int[][][] blockIds, int sizeX, int sizeY, int sizeZ) {
      public int countSolid() {
         int count = 0;

         for (int x = 0; x < this.sizeX; x++) {
            for (int y = 0; y < this.sizeY; y++) {
               for (int z = 0; z < this.sizeZ; z++) {
                  if (this.voxels[x][y][z]) {
                     count++;
                  }
               }
            }
         }

         return count;
      }

      public int getBlockId(int x, int y, int z) {
         if (this.blockIds == null) {
            return 0;
         } else {
            return x >= 0 && x < this.sizeX && y >= 0 && y < this.sizeY && z >= 0 && z < this.sizeZ ? this.blockIds[x][y][z] : 0;
         }
      }
   }
}
