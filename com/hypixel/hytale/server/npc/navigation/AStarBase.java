package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.function.BiToFloatFunction;
import com.hypixel.hytale.function.function.ToFloatFunction;
import com.hypixel.hytale.function.predicate.BiFloatPredicate;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AStarBase {
   public static final double FULL_STEP_THRESHOLD = 0.9999999;
   public static final double REQUIRED_TARGET_DISTANCE = 9.999999994736442E-8;
   public static final double HALF_STEP_THRESHOLD = 0.49999995;
   public static final double ON_GRID_THRESHOLD = 0.01;
   protected static final int INDEX_FRACTIONAL_BITS = 1;
   protected static final int POSITION_BITS = 11;
   protected static final int POSITION_OFFSET = 1024;
   protected static final int POSITION_MASK = 2047;
   protected int maxPathLength = 200;
   protected int openNodesLimit = 80;
   protected int totalNodesLimit = 400;
   protected boolean canMoveDiagonal = true;
   protected boolean optimizedBuildPath = true;
   protected boolean isAvoidingBlockDamage;
   protected boolean isRelaxedMoveConstraints;
   protected final Vector3d startPosition = new Vector3d();
   protected AStarEvaluator evaluator;
   protected double positionToIndexOffsetX;
   protected double positionToIndexOffsetY;
   protected double positionToIndexOffsetZ;
   protected long indexToPositionOffsetX;
   protected long indexToPositionOffsetY;
   protected long indexToPositionOffsetZ;
   protected long startPositionIndex;
   protected boolean is2D;
   protected boolean projectedX;
   protected boolean projectedY;
   protected boolean projectedZ;
   protected final Vector3d searchDirectionsWorldNormal = new Vector3d();
   protected boolean searchDirectionIsDiagonalMoves;
   protected boolean searchDirectionIs2D;
   protected Vector3d[] searchDirections;
   protected double[] searchDirectionDistances;
   protected int[] inverseSearchDirections;
   protected int normalsPerDirection;
   protected int[] normalDirections;
   protected AStarNodePool nodePool;
   protected final List<AStarNode> openNodes = new ObjectArrayList();
   protected final Long2ObjectMap<AStarNode> visitedBlocks = new Long2ObjectOpenHashMap();
   protected int iterations;
   @Nullable
   protected AStarNode path;
   protected AStarBase.Progress progress;
   protected final Vector3d pathEnd = new Vector3d();
   protected final Vector3d tempPositionVector = new Vector3d();
   protected final Vector3d tempDirectionVector = new Vector3d();

   public void setCanMoveDiagonal(boolean canMoveDiagonal) {
      this.canMoveDiagonal = canMoveDiagonal;
   }

   public void setMaxPathLength(int maxPathLength) {
      this.maxPathLength = maxPathLength;
   }

   public void setOpenNodesLimit(int openNodesLimit) {
      this.openNodesLimit = openNodesLimit;
   }

   public void setTotalNodesLimit(int totalNodesLimit) {
      this.totalNodesLimit = totalNodesLimit;
   }

   public void setStartPosition(@Nonnull Vector3d position) {
      this.startPosition.assign(position);
   }

   @Nonnull
   public Vector3d getStartPosition() {
      return this.startPosition;
   }

   public void setOptimizedBuildPath(boolean optimizedBuildPath) {
      this.optimizedBuildPath = optimizedBuildPath;
   }

   public AStarEvaluator getEvaluator() {
      return this.evaluator;
   }

   @Nonnull
   public List<AStarNode> getOpenNodes() {
      return this.openNodes;
   }

   public int getOpenCount() {
      return this.openNodes.size();
   }

   @Nonnull
   public Long2ObjectMap<AStarNode> getVisitedBlocks() {
      return this.visitedBlocks;
   }

   public long getStartPositionIndex() {
      return this.startPositionIndex;
   }

   @Nullable
   public AStarNode getPath() {
      return this.path;
   }

   @Nullable
   public Vector3d getPosition() {
      return this.path != null ? this.path.getPosition() : null;
   }

   public int getLength() {
      return this.path != null ? this.path.getLength() : 0;
   }

   public int getIterations() {
      return this.iterations;
   }

   @Nullable
   public Vector3d getEndPosition() {
      return this.path != null ? this.pathEnd : null;
   }

   public void clearPath() {
      this.path = null;
      if (!this.visitedBlocks.isEmpty()) {
         Long2ObjectMaps.fastForEach(this.visitedBlocks, nodeEntry -> this.nodePool.deallocate((AStarNode)nodeEntry.getValue()));
         this.visitedBlocks.clear();
      }

      this.openNodes.clear();
      this.setProgress(AStarBase.Progress.UNSTARTED);
   }

   public AStarBase.Progress initComputePath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d start,
      AStarEvaluator evaluator,
      @Nonnull MotionController motionController,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull AStarNodePoolProvider nodePoolProvider,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.clearPath();
      this.iterations = 0;
      this.evaluator = evaluator;
      this.startPosition.assign(start);
      this.isAvoidingBlockDamage = probeMoveData.isAvoidingBlockDamage;
      this.isRelaxedMoveConstraints = probeMoveData.isRelaxedMoveConstraints;
      long startBlockX = MathUtil.fastFloor(this.startPosition.x);
      long startBlockY = MathUtil.fastFloor(this.startPosition.y);
      long startBlockZ = MathUtil.fastFloor(this.startPosition.z);
      long twoX = 2L * startBlockX;
      long twoY = 2L * startBlockY;
      long twoZ = 2L * startBlockZ;
      double positionToIndexOffset = -1023.25;
      this.positionToIndexOffsetX = twoX + positionToIndexOffset;
      this.positionToIndexOffsetY = twoY + positionToIndexOffset;
      this.positionToIndexOffsetZ = twoZ + positionToIndexOffset;
      this.indexToPositionOffsetX = twoX + 1L - 1024L;
      this.indexToPositionOffsetY = twoY + 1L - 1024L;
      this.indexToPositionOffsetZ = twoZ + 1L - 1024L;
      this.startPositionIndex = this.positionToIndex(this.startPosition);
      Vector3d componentSelector = motionController.getComponentSelector();
      this.is2D = motionController.is2D();
      this.projectedX = this.is2D && componentSelector.x == 0.0;
      this.projectedY = this.is2D && componentSelector.y == 0.0;
      this.projectedZ = this.is2D && componentSelector.z == 0.0;
      if (this.searchDirections == null
         || this.searchDirectionIs2D != this.is2D
         || this.searchDirectionIsDiagonalMoves != this.canMoveDiagonal
         || !this.searchDirectionsWorldNormal.equals(motionController.getWorldNormal())) {
         this.searchDirectionIsDiagonalMoves = this.canMoveDiagonal;
         this.searchDirectionIs2D = this.is2D;
         this.searchDirectionsWorldNormal.assign(motionController.getWorldNormal());
         int searchDirectionCount = this.is2D ? (this.canMoveDiagonal ? 8 : 4) : (this.canMoveDiagonal ? 26 : 6);
         this.searchDirections = new Vector3d[searchDirectionCount];
         this.searchDirectionDistances = new double[searchDirectionCount];
         int directionIndex = 0;

         for (double x = -1.0; x <= 1.0; x++) {
            if (!this.projectedX || x == 0.0) {
               for (double y = -1.0; y <= 1.0; y++) {
                  if (!this.projectedY || y == 0.0) {
                     for (double z = -1.0; z <= 1.0; z++) {
                        if ((!this.projectedZ || z == 0.0) && (x != 0.0 || y != 0.0 || z != 0.0)) {
                           Vector3d direction = new Vector3d(x, y, z);
                           this.searchDirections[directionIndex] = direction;
                           this.searchDirectionDistances[directionIndex] = direction.length();
                           directionIndex++;
                        }
                     }
                  }
               }
            }
         }

         this.inverseSearchDirections = new int[searchDirectionCount];

         for (int i = 0; i < this.inverseSearchDirections.length; i++) {
            this.inverseSearchDirections[i] = -1;
         }

         for (int i = 0; i < this.searchDirections.length - 1; i++) {
            if (this.inverseSearchDirections[i] == -1) {
               this.tempDirectionVector.assign(this.searchDirections[i]).negate();

               for (int j = i + 1; j < this.searchDirections.length; j++) {
                  if (this.searchDirections[j].equals(this.tempDirectionVector)) {
                     this.inverseSearchDirections[i] = j;
                     this.inverseSearchDirections[j] = i;
                     break;
                  }
               }

               if (this.inverseSearchDirections[i] == -1) {
                  throw new IllegalStateException("Can't find inverse search direction");
               }
            }
         }

         if (this.is2D) {
            this.normalsPerDirection = 1;
            this.normalDirections = new int[this.normalsPerDirection * searchDirectionCount];
            directionIndex = 0;

            for (int ix = 0; ix < this.searchDirections.length; ix++) {
               Vector3d direction = this.searchDirections[ix];
               int endIndex = directionIndex + this.normalsPerDirection;
               Vector3d oneNormal = null;

               for (int jx = 0; jx < this.searchDirections.length; jx++) {
                  Vector3d otherDirection = this.searchDirections[jx];
                  if (ix != jx && direction.dot(otherDirection) == 0.0 && (oneNormal == null || oneNormal.dot(otherDirection) == 0.0)) {
                     this.normalDirections[directionIndex++] = jx;
                     if (directionIndex == endIndex) {
                        break;
                     }

                     oneNormal = otherDirection;
                  }
               }

               if (directionIndex != endIndex) {
                  throw new IllegalStateException("Can't find correct number of normals");
               }
            }
         }

         this.nodePool = nodePoolProvider.getPool(searchDirectionCount);
      }

      probeMoveData.setSaveSegments(false);
      this.tempPositionVector
         .assign(this.projectedX ? start.x : startBlockX + 0.5, this.projectedY ? start.y : startBlockY + 0.5, this.projectedZ ? start.z : startBlockZ + 0.5);
      Vector3d position = this.canAdvance(ref, this.startPosition, this.tempPositionVector, motionController, probeMoveData, componentAccessor);
      if (position != null) {
         this.addStartNode(this.startPosition, position, motionController);
         probeMoveData.setSaveSegments(true);
         return this.setProgress(AStarBase.Progress.COMPUTING);
      } else {
         this.tempPositionVector.x = this.projectedX ? start.x : MathUtil.fastFloor(2.0 * start.x) / 2.0;
         this.tempPositionVector.y = this.projectedY ? start.y : MathUtil.fastFloor(2.0 * start.y) / 2.0;
         this.tempPositionVector.z = this.projectedZ ? start.z : MathUtil.fastFloor(2.0 * start.z) / 2.0;

         for (double xx = this.projectedX ? 0.0 : 0.5; xx >= 0.0; xx -= 0.5) {
            for (double yx = this.projectedY ? 0.0 : 0.5; yx >= 0.0; yx -= 0.5) {
               for (double zx = this.projectedZ ? 0.0 : 0.5; zx >= 0.0; zx -= 0.5) {
                  this.tempDirectionVector.assign(xx, yx, zx).add(this.tempPositionVector);
                  position = this.canAdvance(ref, this.startPosition, this.tempDirectionVector, motionController, probeMoveData, componentAccessor);
                  if (position != null) {
                     this.addStartNode(this.startPosition, position, motionController);
                  }
               }
            }
         }

         if (this.openNodes.isEmpty()) {
            double startX = this.tempPositionVector.x + (this.projectedX ? 0 : -1);
            double endX = this.tempPositionVector.x + (this.projectedX ? 0 : 1);
            double startY = this.tempPositionVector.y + (this.projectedY ? 0 : -1);
            double endY = this.tempPositionVector.y + (this.projectedY ? 0 : 1);
            double startZ = this.tempPositionVector.z + (this.projectedZ ? 0 : -1);
            double endZ = this.tempPositionVector.z + (this.projectedZ ? 0 : 1);

            for (double xx = startX; xx <= endX; xx += 0.5) {
               for (double yx = startY; yx <= endY; yx += 0.5) {
                  for (double zxx = startZ; zxx <= endZ; zxx += 0.5) {
                     this.tempDirectionVector.assign(xx, yx, zxx);
                     position = this.canAdvance(ref, this.startPosition, this.tempDirectionVector, motionController, probeMoveData, componentAccessor);
                     if (position != null) {
                        this.addStartNode(this.startPosition, position, motionController);
                     }
                  }
               }
            }
         }

         probeMoveData.setSaveSegments(true);
         return this.setProgress(this.openNodes.isEmpty() ? AStarBase.Progress.ABORTED : AStarBase.Progress.COMPUTING);
      }
   }

   public AStarBase.Progress computePath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      @Nonnull ProbeMoveData probeMoveData,
      int nodesToProcess,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.progress != AStarBase.Progress.COMPUTING) {
         return this.progress;
      } else {
         probeMoveData.isAvoidingBlockDamage = this.isAvoidingBlockDamage;
         probeMoveData.isRelaxedMoveConstraints = this.isRelaxedMoveConstraints;

         while (!this.openNodes.isEmpty() && nodesToProcess-- > 0) {
            int idx = this.openNodes.size() - 1;
            AStarNode node = this.openNodes.get(idx);
            node.close();
            if (this.evaluator.isGoalReached(ref, this, node, motionController, componentAccessor)) {
               this.buildPath(node);
               return this.setProgress(AStarBase.Progress.ACCOMPLISHED);
            }

            this.openNodes.remove(idx);
            this.iterations++;
            if (node.getLength() < this.maxPathLength) {
               Vector3d nodePosition = node.getPosition();
               AStarNode[] successors = node.getSuccessors();
               int searchDirectionCount = this.searchDirections.length;

               label89:
               for (int directionIndex = 0; directionIndex < searchDirectionCount; directionIndex++) {
                  if (successors[directionIndex] == null) {
                     double directionLength = this.searchDirectionDistances[directionIndex];
                     probeMoveData.setPosition(nodePosition).setDirection(this.searchDirections[directionIndex]);
                     double distance = motionController.probeMove(ref, probeMoveData, componentAccessor);
                     double halfThreshold = directionLength * 0.49999995;
                     if (!(distance < halfThreshold)) {
                        double halfDistance = directionLength * 0.5;
                        probeMoveData.computePosition(halfDistance, probeMoveData.targetPosition);
                        long halfPositionIndex = this.positionToIndex(probeMoveData.targetPosition);
                        if (halfPositionIndex != -1L) {
                           AStarNode otherNode = (AStarNode)this.visitedBlocks.get(halfPositionIndex);
                           if (otherNode != null) {
                              this.updateNode(node, directionIndex, otherNode, motionController);
                           } else {
                              if (this.is2D) {
                                 int firstNormalIndex = directionIndex * this.normalsPerDirection;

                                 for (int normalIndex = firstNormalIndex; normalIndex < firstNormalIndex + this.normalsPerDirection; normalIndex++) {
                                    Vector3d normal = this.searchDirections[this.normalDirections[normalIndex]];
                                    long diagonalVertexIndex = this.addOffsetToIndex(halfPositionIndex, (long)normal.x, (long)normal.y, (long)normal.z);
                                    AStarNode diagonalNode = (AStarNode)this.visitedBlocks.get(diagonalVertexIndex);
                                    if (diagonalNode != null) {
                                       AStarNode otherDiagonalNode = diagonalNode.getSuccessor(this.inverseSearchDirections[normalIndex]);
                                       if (otherDiagonalNode != null) {
                                          continue label89;
                                       }
                                    }
                                 }
                              }

                              Vector3d destination = distance >= directionLength * 0.9999999 ? probeMoveData.probePosition : probeMoveData.targetPosition;
                              this.addOrUpdateNode(node, directionIndex, destination, motionController, componentAccessor);
                           }
                        }
                     }
                  }
               }

               if (this.openNodesLimit > 0 && this.openNodes.size() >= this.openNodesLimit) {
                  return this.setProgress(AStarBase.Progress.TERMINATED_OPEN_NODE_LIMIT_EXCEEDED);
               }

               if (this.totalNodesLimit > 0 && this.visitedBlocks.size() >= this.totalNodesLimit) {
                  return this.setProgress(AStarBase.Progress.TERMINATED_TOTAL_NODE_LIMIT_EXCEEDED);
               }
            }
         }

         return this.setProgress(this.openNodes.isEmpty() ? AStarBase.Progress.TERMINATED : AStarBase.Progress.COMPUTING);
      }
   }

   public AStarBase.Progress getProgress() {
      return this.progress;
   }

   public boolean isComputing() {
      return this.progress == AStarBase.Progress.COMPUTING;
   }

   public float buildLongestPath() {
      AStarNode node = this.buildBestPath(AStarNode::getTravelCost, (oldV, v) -> v > oldV, 0.0F);
      return node == null ? 0.0F : node.getTravelCost();
   }

   public float buildFurthestPath() {
      AStarNode node = this.buildBestPath(n -> (float)n.getPosition().distanceSquaredTo(this.startPosition), (oldV, v) -> v > oldV, 0.0F);
      return node == null ? 0.0F : node.getTravelCost();
   }

   @Nullable
   public AStarNode buildBestPath(@Nonnull ToFloatFunction<AStarNode> weight, @Nonnull BiFloatPredicate predicate, float initialValue) {
      if (this.path != null) {
         return null;
      } else {
         AStarNode node = this.findBestVisitedNode(weight, predicate, initialValue);
         if (node == null) {
            return null;
         } else {
            this.buildPath(node);
            return node;
         }
      }
   }

   @Nullable
   public AStarNode findBestVisitedNode(@Nonnull ToFloatFunction<AStarNode> weight, @Nonnull BiFloatPredicate predicate, float initialValue) {
      if (this.visitedBlocks.isEmpty()) {
         return null;
      } else {
         ObjectIterator<Entry<AStarNode>> iterator = Long2ObjectMaps.fastIterator(this.visitedBlocks);
         float value = initialValue;
         AStarNode resultNode = null;

         while (iterator.hasNext()) {
            AStarNode node = (AStarNode)((Entry)iterator.next()).getValue();
            float v = weight.applyAsFloat(node);
            if (predicate.test(value, v)) {
               value = v;
               resultNode = node;
            }
         }

         return resultNode;
      }
   }

   @Nullable
   public <T> AStarNode buildBestPath(@Nonnull BiToFloatFunction<AStarNode, T> weight, @Nonnull BiFloatPredicate predicate, float initialValue, T obj) {
      if (this.path != null) {
         return null;
      } else {
         AStarNode node = this.findBestVisitedNode(weight, predicate, initialValue, obj);
         if (node == null) {
            return null;
         } else {
            this.buildPath(node);
            return node;
         }
      }
   }

   @Nullable
   public <T> AStarNode findBestVisitedNode(@Nonnull BiToFloatFunction<AStarNode, T> weight, @Nonnull BiFloatPredicate predicate, float initialValue, T obj) {
      if (this.visitedBlocks.isEmpty()) {
         return null;
      } else {
         ObjectIterator<Entry<AStarNode>> iterator = Long2ObjectMaps.fastIterator(this.visitedBlocks);
         float value = initialValue;
         AStarNode resultNode = null;

         while (iterator.hasNext()) {
            AStarNode node = (AStarNode)((Entry)iterator.next()).getValue();
            float v = weight.applyAsFloat(node, obj);
            if (predicate.test(value, v)) {
               value = v;
               resultNode = node;
            }
         }

         return resultNode;
      }
   }

   @Nonnull
   public AStarDebugBase createDebugHelper(@Nonnull HytaleLogger logger) {
      return new AStarDebugBase(this, logger);
   }

   public static long indexFromXYZ(long dx, long dy, long dz) {
      return dx >= 0L && dx <= 2047L && dy >= 0L && dy <= 2047L && dz >= 0L && dz <= 2047L ? (dx << 22) + (dy << 11) + dz : -1L;
   }

   public static int zFromIndex(long index) {
      return (int)(index & 2047L);
   }

   public static int yFromIndex(long index) {
      return zFromIndex(index >> 11);
   }

   public static int xFromIndex(long index) {
      return zFromIndex(index >> 22);
   }

   @Nonnull
   public static String positionIndexToString(long index) {
      double x = (xFromIndex(index) - 1024) * 0.5 + 0.5;
      double y = (yFromIndex(index) - 1024) * 0.5 + 0.5;
      double z = (zFromIndex(index) - 1024) * 0.5 + 0.5;
      return "[" + x + "/" + y + "/" + z + "]";
   }

   protected AStarBase.Progress setProgress(AStarBase.Progress progress) {
      this.progress = progress;
      return this.progress;
   }

   @Nullable
   protected Vector3d canAdvance(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d startPosition,
      @Nonnull Vector3d destination,
      @Nonnull MotionController motionController,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      probeMoveData.setPosition(startPosition).setTargetPosition(destination);
      return probeMoveData.canAdvance(ref, motionController, 0.9999999, componentAccessor) ? probeMoveData.probePosition : null;
   }

   protected void addStartNode(Vector3d startPosition, @Nonnull Vector3d position, @Nonnull MotionController motionController) {
      long positionIndex = this.positionToIndex(position);
      float cost = this.measureWalkCost(startPosition, position, motionController);
      AStarNode node = this.nodePool.allocate().initAsStartNode(position, positionIndex, cost, this.evaluator.estimateToGoal(this, position, motionController));
      this.addOpenNode(node, positionIndex);
   }

   protected void addOpenNode(
      @Nonnull AStarNode parentNode, int directionIndex, @Nonnull Vector3d position, long positionIndex, float cost, MotionController motionController
   ) {
      int inverseSearchDirection = this.inverseSearchDirections[directionIndex];
      AStarNode node = this.nodePool
         .allocate()
         .initWithPredecessor(
            parentNode, directionIndex, position, positionIndex, inverseSearchDirection, cost, this.evaluator.estimateToGoal(this, position, motionController)
         );
      this.addOpenNode(node, positionIndex);
   }

   protected void addOpenNode(@Nonnull AStarNode node, long index) {
      int predecessor = this.openNodes.size() - 1;
      float totalCost = node.getTotalCost();

      while (predecessor >= 0 && this.openNodes.get(predecessor).getTotalCost() < totalCost) {
         predecessor--;
      }

      this.openNodes.add(predecessor + 1, node);
      this.visitedBlocks.put(index, node);
   }

   protected void updateNode(@Nonnull AStarNode node, int directionIndex, @Nonnull AStarNode targetNode, @Nonnull MotionController motionController) {
      if (!targetNode.isInvalid()) {
         float stepCost = this.measureWalkCost(node.getPosition(), targetNode.getPosition(), motionController);
         this.updateNodeCost(node, directionIndex, targetNode, stepCost);
      }
   }

   protected void addOrUpdateNode(
      @Nonnull AStarNode node,
      int directionIndex,
      @Nonnull Vector3d position,
      @Nonnull MotionController motionController,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      long positionIndex = this.positionToIndex(position);
      if (positionIndex != -1L && positionIndex != node.positionIndex) {
         AStarNode targetNode = (AStarNode)this.visitedBlocks.get(positionIndex);
         if (targetNode == null) {
            if (!motionController.isValidPosition(position, componentAccessor)) {
               this.visitedBlocks.put(positionIndex, this.nodePool.allocate().initAsInvalid(position, this.positionToIndex(position)));
            } else {
               float travelCostToNode = node.getTravelCost() + this.measureWalkCost(node.getPosition(), position, motionController);
               this.addOpenNode(node, directionIndex, position, positionIndex, travelCostToNode, motionController);
            }
         } else if (!targetNode.isInvalid()) {
            float stepCost = this.measureWalkCost(node.getPosition(), position, motionController);
            this.updateNodeCost(node, directionIndex, targetNode, stepCost);
         }
      }
   }

   protected void updateNodeCost(@Nonnull AStarNode node, int directionIndex, @Nonnull AStarNode targetNode, float stepCost) {
      float travelCostToNode = node.getTravelCost() + stepCost;
      float delta = travelCostToNode - targetNode.getTravelCost();
      if (delta < 0.0F) {
         targetNode.adjustOptimalPath(node, delta, directionIndex);
         node.setSuccessor(directionIndex, targetNode, this.inverseSearchDirections[directionIndex], stepCost);
      } else {
         node.successors[directionIndex] = AStarNode.ENTRY_NODE_TAG;
         targetNode.successors[this.inverseSearchDirections[directionIndex]] = AStarNode.ENTRY_NODE_TAG;
      }
   }

   protected long positionToIndex(@Nonnull Vector3d position) {
      long dx = MathUtil.fastFloor(position.x * 2.0 - this.positionToIndexOffsetX);
      long dy = MathUtil.fastFloor(position.y * 2.0 - this.positionToIndexOffsetY);
      long dz = MathUtil.fastFloor(position.z * 2.0 - this.positionToIndexOffsetZ);
      return indexFromXYZ(dx, dy, dz);
   }

   protected float measureWalkCost(Vector3d fromPosition, Vector3d toPosition, @Nonnull MotionController motionController) {
      return (float)motionController.waypointDistance(fromPosition, toPosition);
   }

   protected void buildPath(@Nullable AStarNode endNode) {
      if (endNode == null) {
         this.path = null;
      } else {
         this.pathEnd.assign(endNode.getPosition());
         if (this.optimizedBuildPath) {
            AStarNode node = endNode;
            int length = 1;
            int lastDirection = -1;

            for (AStarNode lastCorner = null; node != null; node = node.getPredecessor()) {
               node.setNextNode(lastCorner, length);
               if (node.getPredecessorDirection() != lastDirection) {
                  lastDirection = node.getPredecessorDirection();
                  lastCorner = node;
                  length++;
               }

               this.path = node;
            }
         } else {
            int length = 0;

            for (AStarNode nextNode = null; endNode != null; endNode = endNode.getPredecessor()) {
               endNode.setNextNode(nextNode, ++length);
               this.path = endNode;
               nextNode = endNode;
            }
         }
      }
   }

   protected long addOffsetToIndex(long index, long xSteps, long ySteps, long zSteps) {
      long x = xFromIndex(index) + xSteps;
      long y = yFromIndex(index) + ySteps;
      long z = zFromIndex(index) + zSteps;
      return indexFromXYZ(x, y, z);
   }

   public static enum Progress {
      UNSTARTED,
      ABORTED,
      COMPUTING,
      ACCOMPLISHED,
      TERMINATED,
      TERMINATED_OPEN_NODE_LIMIT_EXCEEDED,
      TERMINATED_TOTAL_NODE_LIMIT_EXCEEDED;
   }
}
