package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.math.vector.Vector3d;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AStarNode implements IWaypoint {
   public static final AStarNode ENTRY_NODE_TAG = new AStarNode(0);
   @Nonnull
   protected final Vector3d position = new Vector3d();
   protected float travelCost;
   protected float estimateToGoal;
   protected float totalCost;
   @Nullable
   protected AStarNode predecessor = null;
   protected int predecessorDirection = -1;
   @Nonnull
   protected final AStarNode[] successors;
   @Nonnull
   protected final float[] stepCost;
   protected AStarNode nextPathNode;
   protected int length;
   protected long positionIndex;
   protected boolean open;

   public AStarNode(int numDirections) {
      this.successors = new AStarNode[numDirections];
      this.stepCost = new float[numDirections];
   }

   public long getPositionIndex() {
      return this.positionIndex;
   }

   @Nonnull
   public AStarNode[] getSuccessors() {
      return this.successors;
   }

   public AStarNode getSuccessor(int index) {
      return this.successors[index];
   }

   public void setSuccessor(int directionIndex, @Nonnull AStarNode node, int inverseDirectionIndex, float cost) {
      this.successors[directionIndex] = node;
      this.stepCost[directionIndex] = cost;
      node.successors[inverseDirectionIndex] = ENTRY_NODE_TAG;
   }

   @Nullable
   public AStarNode getPredecessor() {
      return this.predecessor;
   }

   public AStarNode getNextPathNode() {
      return this.nextPathNode;
   }

   public void setNextNode(AStarNode next, int length) {
      this.nextPathNode = next;
      this.length = length;
   }

   public float getTravelCost() {
      return this.travelCost;
   }

   public float getEstimateToGoal() {
      return this.estimateToGoal;
   }

   public float getTotalCost() {
      return this.totalCost;
   }

   public int getPredecessorDirection() {
      return this.predecessorDirection;
   }

   public void close() {
      this.open = false;
   }

   public boolean isOpen() {
      return this.open;
   }

   public boolean isInvalid() {
      return this.length < 0;
   }

   @Override
   public int getLength() {
      return this.length;
   }

   public AStarNode next() {
      return this.nextPathNode;
   }

   @Nonnull
   @Override
   public Vector3d getPosition() {
      return this.position;
   }

   @Nullable
   public AStarNode advance(int skip) {
      AStarNode node = this;

      while (skip-- > 0 && node != null) {
         node = node.nextPathNode;
      }

      return node;
   }

   @Nonnull
   public AStarNode initAsStartNode(@Nonnull Vector3d position, long positionIndex, float cost, float estimateCost) {
      this.position.assign(position);
      this.positionIndex = positionIndex;
      this.open = true;
      this.estimateToGoal = estimateCost;
      this.travelCost = cost;
      this.totalCost = this.travelCost + this.estimateToGoal;
      this.predecessor = null;
      this.predecessorDirection = -1;
      Arrays.fill(this.successors, null);
      Arrays.fill(this.stepCost, 0.0F);
      this.length = 1;
      return this;
   }

   @Nonnull
   public AStarNode initWithPredecessor(
      @Nonnull AStarNode predecessor,
      int directionIndex,
      @Nonnull Vector3d position,
      long positionIndex,
      int inverseDirectionIndex,
      float travelCost,
      float estimateCost
   ) {
      this.position.assign(position);
      this.positionIndex = positionIndex;
      this.open = true;
      this.estimateToGoal = estimateCost;
      this.travelCost = travelCost;
      this.totalCost = this.travelCost + this.estimateToGoal;
      this.length = predecessor.length + 1;
      Arrays.fill(this.successors, null);
      Arrays.fill(this.stepCost, 0.0F);
      predecessor.setSuccessor(directionIndex, this, inverseDirectionIndex, travelCost - predecessor.travelCost);
      this.predecessor = predecessor;
      this.predecessorDirection = directionIndex;
      return this;
   }

   @Nonnull
   public AStarNode initAsInvalid(@Nonnull Vector3d position, long positionIndex) {
      this.position.assign(position);
      this.positionIndex = positionIndex;
      this.open = false;
      this.estimateToGoal = Float.MAX_VALUE;
      this.travelCost = Float.MAX_VALUE;
      this.totalCost = Float.MAX_VALUE;
      this.predecessor = null;
      this.predecessorDirection = -1;
      Arrays.fill(this.successors, null);
      Arrays.fill(this.stepCost, 0.0F);
      this.length = -1;
      return this;
   }

   public void adjustOptimalPath(AStarNode parentNode, float deltaCost, int direction) {
      this.predecessor = parentNode;
      this.predecessorDirection = direction;
      this.travelCost += deltaCost;
      this.totalCost = this.travelCost + this.estimateToGoal;
      this.length = this.predecessor.length + 1;

      for (int successorDirection = 0; successorDirection < this.successors.length; successorDirection++) {
         AStarNode successor = this.successors[successorDirection];
         if (successor != null && !ENTRY_NODE_TAG.equals(successor)) {
            float delta = this.travelCost + this.stepCost[successorDirection] - successor.travelCost;
            if (delta < 0.0F) {
               successor.adjustOptimalPath(this, deltaCost, successorDirection);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "AStarNode{position="
         + this.position
         + ", travelCost="
         + this.travelCost
         + ", estimateToGoal="
         + this.estimateToGoal
         + ", totalCost="
         + this.totalCost
         + ", predecessorDirection="
         + this.predecessorDirection
         + ", stepCost="
         + Arrays.toString(this.stepCost)
         + ", length="
         + this.length
         + ", positionIndex="
         + this.positionIndex
         + ", open="
         + this.open
         + "}";
   }
}
