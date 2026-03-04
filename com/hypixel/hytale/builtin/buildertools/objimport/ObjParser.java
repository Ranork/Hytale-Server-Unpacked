package com.hypixel.hytale.builtin.buildertools.objimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ObjParser {
   private ObjParser() {
   }

   @Nonnull
   public static ObjParser.ObjMesh parse(@Nonnull Path path) throws IOException, ObjParser.ObjParseException {
      List<float[]> vertices = new ArrayList<>();
      List<float[]> uvCoordinates = new ArrayList<>();
      List<int[]> faces = new ArrayList<>();
      List<int[]> faceUvIndices = new ArrayList<>();
      List<String> faceMaterials = new ArrayList<>();
      String mtlLib = null;
      String currentMaterial = null;

      try (BufferedReader reader = Files.newBufferedReader(path)) {
         int lineNum = 0;

         String line;
         while ((line = reader.readLine()) != null) {
            lineNum++;
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
               String[] parts = line.split("\\s+");
               if (parts.length != 0) {
                  String var12 = parts[0];
                  switch (var12) {
                     case "v":
                        parseVertex(parts, vertices, lineNum);
                        break;
                     case "vt":
                        parseUvCoordinate(parts, uvCoordinates, lineNum);
                        break;
                     case "f":
                        int faceCountBefore = faces.size();
                        parseFace(parts, faces, faceUvIndices, uvCoordinates.size(), lineNum);
                        int facesAdded = faces.size() - faceCountBefore;

                        for (int i = 0; i < facesAdded; i++) {
                           faceMaterials.add(currentMaterial);
                        }
                        break;
                     case "mtllib":
                        if (parts.length > 1) {
                           mtlLib = parts[1].trim();
                        }
                        break;
                     case "usemtl":
                        if (parts.length > 1) {
                           currentMaterial = parts[1].trim();
                        }
                  }
               }
            }
         }
      }

      if (vertices.isEmpty()) {
         throw new ObjParser.ObjParseException("OBJ file contains no vertices");
      } else if (faces.isEmpty()) {
         throw new ObjParser.ObjParseException("OBJ file contains no faces");
      } else {
         return new ObjParser.ObjMesh(vertices, uvCoordinates, faces, faceUvIndices, faceMaterials, mtlLib);
      }
   }

   private static void parseVertex(String[] parts, List<float[]> vertices, int lineNum) throws ObjParser.ObjParseException {
      if (parts.length < 4) {
         throw new ObjParser.ObjParseException("Invalid vertex at line " + lineNum + ": expected at least 3 coordinates");
      } else {
         try {
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float z = Float.parseFloat(parts[3]);
            vertices.add(new float[]{x, y, z});
         } catch (NumberFormatException var6) {
            throw new ObjParser.ObjParseException("Invalid vertex coordinates at line " + lineNum);
         }
      }
   }

   private static void parseUvCoordinate(String[] parts, List<float[]> uvCoordinates, int lineNum) throws ObjParser.ObjParseException {
      if (parts.length < 3) {
         throw new ObjParser.ObjParseException("Invalid UV coordinate at line " + lineNum + ": expected at least 2 values");
      } else {
         try {
            float u = Float.parseFloat(parts[1]);
            float v = Float.parseFloat(parts[2]);
            uvCoordinates.add(new float[]{u, v});
         } catch (NumberFormatException var5) {
            throw new ObjParser.ObjParseException("Invalid UV coordinates at line " + lineNum);
         }
      }
   }

   private static void parseFace(String[] parts, List<int[]> faces, List<int[]> faceUvIndices, int uvCount, int lineNum) throws ObjParser.ObjParseException {
      if (parts.length < 4) {
         throw new ObjParser.ObjParseException("Invalid face at line " + lineNum + ": expected at least 3 vertices");
      } else {
         int[] vertexIndices = new int[parts.length - 1];
         int[] uvIndices = new int[parts.length - 1];
         boolean hasUvs = false;

         for (int i = 1; i < parts.length; i++) {
            String vertexData = parts[i];
            String[] components = vertexData.split("/");

            try {
               int vIndex = Integer.parseInt(components[0]);
               vertexIndices[i - 1] = vIndex > 0 ? vIndex - 1 : vIndex;
            } catch (NumberFormatException var13) {
               throw new ObjParser.ObjParseException("Invalid face vertex index at line " + lineNum);
            }

            if (components.length >= 2 && !components[1].isEmpty()) {
               try {
                  int uvIndex = Integer.parseInt(components[1]);
                  uvIndices[i - 1] = uvIndex > 0 ? uvIndex - 1 : uvIndex + uvCount;
                  hasUvs = true;
               } catch (NumberFormatException var12) {
                  uvIndices[i - 1] = -1;
               }
            } else {
               uvIndices[i - 1] = -1;
            }
         }

         if (vertexIndices.length == 3) {
            faces.add(vertexIndices);
            faceUvIndices.add(hasUvs ? uvIndices : null);
         } else if (vertexIndices.length == 4) {
            faces.add(new int[]{vertexIndices[0], vertexIndices[1], vertexIndices[2]});
            faces.add(new int[]{vertexIndices[0], vertexIndices[2], vertexIndices[3]});
            if (hasUvs) {
               faceUvIndices.add(new int[]{uvIndices[0], uvIndices[1], uvIndices[2]});
               faceUvIndices.add(new int[]{uvIndices[0], uvIndices[2], uvIndices[3]});
            } else {
               faceUvIndices.add(null);
               faceUvIndices.add(null);
            }
         } else {
            for (int i = 1; i < vertexIndices.length - 1; i++) {
               faces.add(new int[]{vertexIndices[0], vertexIndices[i], vertexIndices[i + 1]});
               if (hasUvs) {
                  faceUvIndices.add(new int[]{uvIndices[0], uvIndices[i], uvIndices[i + 1]});
               } else {
                  faceUvIndices.add(null);
               }
            }
         }
      }
   }

   public record ObjMesh(
      List<float[]> vertices, List<float[]> uvCoordinates, List<int[]> faces, List<int[]> faceUvIndices, List<String> faceMaterials, @Nullable String mtlLib
   ) {
      public float[] getBounds() {
         float minX = Float.MAX_VALUE;
         float minY = Float.MAX_VALUE;
         float minZ = Float.MAX_VALUE;
         float maxX = -Float.MAX_VALUE;
         float maxY = -Float.MAX_VALUE;
         float maxZ = -Float.MAX_VALUE;

         for (float[] v : this.vertices) {
            minX = Math.min(minX, v[0]);
            minY = Math.min(minY, v[1]);
            minZ = Math.min(minZ, v[2]);
            maxX = Math.max(maxX, v[0]);
            maxY = Math.max(maxY, v[1]);
            maxZ = Math.max(maxZ, v[2]);
         }

         return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
      }

      public float getHeight() {
         float[] bounds = this.getBounds();
         return bounds[4] - bounds[1];
      }

      public boolean hasMaterials() {
         return this.mtlLib != null && !this.faceMaterials.isEmpty() && this.faceMaterials.stream().anyMatch(m -> m != null);
      }

      public boolean hasUvCoordinates() {
         return !this.uvCoordinates.isEmpty() && this.faceUvIndices.stream().anyMatch(uv -> uv != null);
      }

      public void transformZUpToYUp() {
         for (float[] v : this.vertices) {
            float y = v[1];
            float z = v[2];
            v[1] = z;
            v[2] = -y;
         }
      }

      public void transformXUpToYUp() {
         for (float[] v : this.vertices) {
            float x = v[0];
            float y = v[1];
            v[0] = y;
            v[1] = x;
         }
      }
   }

   public static class ObjParseException extends Exception {
      public ObjParseException(String message) {
         super(message);
      }
   }
}
