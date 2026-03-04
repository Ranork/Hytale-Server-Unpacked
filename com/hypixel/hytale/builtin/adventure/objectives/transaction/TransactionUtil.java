package com.hypixel.hytale.builtin.adventure.objectives.transaction;

import javax.annotation.Nullable;

public class TransactionUtil {
   public static boolean anyFailed(@Nullable TransactionRecord[] transactionRecords) {
      if (transactionRecords == null) {
         return false;
      } else {
         for (TransactionRecord transactionRecord : transactionRecords) {
            if (transactionRecord.status == TransactionStatus.FAIL) {
               return true;
            }
         }

         return false;
      }
   }

   public static void revertAll(@Nullable TransactionRecord[] transactionRecords) {
      if (transactionRecords != null) {
         for (TransactionRecord transactionRecord : transactionRecords) {
            transactionRecord.revert();
         }
      }
   }

   public static void completeAll(@Nullable TransactionRecord[] transactionRecords) {
      if (transactionRecords != null) {
         for (TransactionRecord transactionRecord : transactionRecords) {
            transactionRecord.complete();
         }
      }
   }

   public static void unloadAll(@Nullable TransactionRecord[] transactionRecords) {
      if (transactionRecords != null) {
         for (TransactionRecord transactionRecord : transactionRecords) {
            transactionRecord.unload();
         }
      }
   }
}
