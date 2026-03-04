package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AStarDebugBase {
   public static final char CENTER = ' ';
   public static final char CROSS = '┼';
   public static final char HLINE = '─';
   public static final char VLINE = '│';
   public static final char OPEN_NODE = '◆';
   public static final char CLOSED_NODE = '◇';
   public static final char CLOSED_PATH_NODE = '◯';
   public static final char OPEN_PATH_NODE = '◉';
   public static final char BLOCKED_NODE = '×';
   public static final char START_POSITION = '@';
   public static final char END_POSITION = 'Ω';
   public static final String BORDER_PATTERN = "─┼".repeat(1025);
   public static final String CENTER_PATTERN = " │".repeat(1025);
   protected AStarBase aStarBase;
   protected HytaleLogger logger;
   protected HytaleLogger.Api loggerInfo;

   public AStarDebugBase(AStarBase base, @Nonnull HytaleLogger logger) {
      this.aStarBase = base;
      this.logger = logger;
      this.loggerInfo = logger.at(Level.INFO);
   }

   public void dumpOpens(MotionController controller) {
      int openCount = this.aStarBase.getOpenCount();
      List<AStarNode> openNodes = this.aStarBase.getOpenNodes();
      int maxLength = -1;

      for (int i = 0; i < openCount; i++) {
         int length = openNodes.get(i).getLength();
         if (length > maxLength) {
            maxLength = length;
         }
      }

      this.loggerInfo
         .log(
            "== A* iter=%s opens=%s total=%s maxLength=%s %s",
            this.aStarBase.getIterations(),
            openCount,
            this.aStarBase.getVisitedBlocks().size(),
            maxLength,
            this.getExtraLogString(controller)
         );

      for (int ix = 0; ix < openCount; ix++) {
         this.loggerInfo.log("%2d %s", ix, openNodes.get(ix).toString());
      }
   }

   public void dumpPath() {
      AStarNode node = this.aStarBase.getPath();
      this.loggerInfo
         .log("== A* Path iter=%s opens=%s total=%s", this.aStarBase.getIterations(), this.aStarBase.getOpenCount(), this.aStarBase.getVisitedBlocks().size());

      while (node != null) {
         this.loggerInfo.log("%s", node.toString());
         node = node.getNextPathNode();
      }
   }

   public void dumpMap(boolean drawPath, MotionController controller) {
      int openCount = this.aStarBase.getOpenCount();
      List<AStarNode> openNodes = this.aStarBase.getOpenNodes();
      AStarNode start = null;
      boolean finalPath = false;
      if (drawPath) {
         AStarNode path = this.aStarBase.getPath();
         if (path != null) {
            start = path;
            finalPath = true;
         } else if (openCount > 0) {
            start = openNodes.get(openCount - 1);
         }
      }

      this.dumpMap(start, finalPath, controller);
   }

   public void dumpMap(@Nullable AStarNode pathNode, boolean isFinalPath, MotionController controller) {
      long startPositionIndex = this.aStarBase.getStartPositionIndex();
      Long2ObjectMap<AStarNode> visitedBlocks = this.aStarBase.getVisitedBlocks();
      int s = AStarBase.xFromIndex(startPositionIndex);
      int e = this.getDumpMapRegionX(s);
      int minX;
      int maxX;
      if (s < e) {
         minX = s;
         maxX = e;
      } else {
         minX = e;
         maxX = s;
      }

      s = AStarBase.zFromIndex(startPositionIndex);
      e = this.getDumpMapRegionZ(s);
      int minZ;
      int maxZ;
      if (s < e) {
         minZ = s;
         maxZ = e;
      } else {
         minZ = e;
         maxZ = s;
      }

      ObjectIterator<Entry<AStarNode>> fastIterator = Long2ObjectMaps.fastIterator(visitedBlocks);

      while (fastIterator.hasNext()) {
         AStarNode node = (AStarNode)((Entry)fastIterator.next()).getValue();
         int x = AStarBase.xFromIndex(node.getPositionIndex());
         int z = AStarBase.zFromIndex(node.getPositionIndex());
         if (x < minX) {
            minX = x;
         }

         if (x > maxX) {
            maxX = x;
         }

         if (z < minZ) {
            minZ = z;
         }

         if (z > maxZ) {
            maxZ = z;
         }
      }

      int rows = maxZ - minZ + 1;
      int columns = maxX - minX + 1;
      int offset = minX & 1;
      boolean evenStart = (minZ & 1) == 0;
      String first = "'" + (evenStart ? CENTER_PATTERN : BORDER_PATTERN).substring(offset, offset + columns) + "'";
      String second = "'" + (evenStart ? BORDER_PATTERN : CENTER_PATTERN).substring(offset, offset + columns) + "'";
      StringBuilder[] map = new StringBuilder[rows];

      for (int i = 0; i < rows; i += 2) {
         map[i] = new StringBuilder(first);
         if (i + 1 < rows) {
            map[i + 1] = new StringBuilder(second);
         }
      }

      fastIterator = Long2ObjectMaps.fastIterator(visitedBlocks);

      while (fastIterator.hasNext()) {
         AStarNode nodex = (AStarNode)((Entry)fastIterator.next()).getValue();
         this.plot(nodex.getPositionIndex(), (char)(nodex.isInvalid() ? '×' : (nodex.isOpen() ? '◆' : '◇')), map, minX, minZ);
      }

      int openCount = this.aStarBase.getOpenCount();
      int maxLength;
      if (pathNode != null) {
         maxLength = pathNode.getLength();

         while (pathNode != null) {
            this.plot(pathNode.getPositionIndex(), (char)(pathNode.isOpen() ? '◉' : '◯'), map, minX, minZ);
            pathNode = isFinalPath ? pathNode.getNextPathNode() : pathNode.getPredecessor();
         }
      } else {
         List<AStarNode> openNodes = this.aStarBase.getOpenNodes();
         int index = openCount;
         maxLength = 0;

         while (--index >= 0) {
            int pos = openCount - index;
            if (pos > 51) {
               break;
            }

            this.plot(openNodes.get(index).getPositionIndex(), (char)(pos >= 26 ? pos - 26 + 97 : pos + 65), map, minX, minZ);
         }
      }

      this.plot(startPositionIndex, '@', map, minX, minZ);
      this.drawMapFinish(map, minX, minZ);
      this.loggerInfo
         .log(
            "== A* iter=%s, opens=%s total=%s maxLength=%s %s",
            this.aStarBase.getIterations(),
            openCount,
            visitedBlocks.size(),
            maxLength,
            this.getExtraLogString(controller)
         );

      for (StringBuilder stringBuilder : map) {
         this.loggerInfo.log(stringBuilder.toString());
      }
   }

   protected void plot(long positionIndex, char character, @Nonnull StringBuilder[] map, int minX, int minZ) {
      int row = AStarBase.zFromIndex(positionIndex) - minZ;
      int column = AStarBase.xFromIndex(positionIndex) - minX + 1;
      map[row].setCharAt(column, character);
   }

   protected void drawMapFinish(StringBuilder[] map, int minX, int minZ) {
   }

   protected int getDumpMapRegionZ(int def) {
      return def;
   }

   protected int getDumpMapRegionX(int def) {
      return def;
   }

   @Nonnull
   protected String getExtraLogString(MotionController controller) {
      return "";
   }
}
