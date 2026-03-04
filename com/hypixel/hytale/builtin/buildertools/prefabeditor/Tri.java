package com.hypixel.hytale.builtin.buildertools.prefabeditor;

public class Tri<A, B, C> {
   private final A left;
   private final B middle;
   private final C right;

   public Tri(A left, B middle, C right) {
      this.left = left;
      this.middle = middle;
      this.right = right;
   }

   public A getLeft() {
      return this.left;
   }

   public B getMiddle() {
      return this.middle;
   }

   public C getRight() {
      return this.right;
   }
}
