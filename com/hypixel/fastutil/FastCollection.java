package com.hypixel.fastutil;

import java.util.Collection;

public interface FastCollection<E> extends Collection<E> {
   void forEachWithFloat(FastCollection.FastConsumerF<? super E> var1, float var2);

   void forEachWithInt(FastCollection.FastConsumerI<? super E> var1, int var2);

   void forEachWithLong(FastCollection.FastConsumerL<? super E> var1, long var2);

   <A, B, C, D> void forEach(
      FastCollection.FastConsumerD9<? super E, A, B, C, D> var1,
      A var2,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      double var17,
      double var19,
      B var21,
      C var22,
      D var23
   );

   <A, B, C, D> void forEach(
      FastCollection.FastConsumerD6<? super E, A, B, C, D> var1,
      A var2,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      B var15,
      C var16,
      D var17
   );

   @FunctionalInterface
   public interface FastConsumerD6<A, B, C, D, E> {
      void accept(A var1, B var2, double var3, double var5, double var7, double var9, double var11, double var13, C var15, D var16, E var17);
   }

   @FunctionalInterface
   public interface FastConsumerD9<A, B, C, D, E> {
      void accept(
         A var1,
         B var2,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         double var13,
         double var15,
         double var17,
         double var19,
         C var21,
         D var22,
         E var23
      );
   }

   @FunctionalInterface
   public interface FastConsumerF<A> {
      void accept(A var1, float var2);
   }

   @FunctionalInterface
   public interface FastConsumerI<A> {
      void accept(A var1, int var2);
   }

   @FunctionalInterface
   public interface FastConsumerL<A> {
      void accept(A var1, long var2);
   }
}
