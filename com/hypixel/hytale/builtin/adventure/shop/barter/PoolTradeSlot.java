package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

public class PoolTradeSlot extends TradeSlot {
   @Nonnull
   public static final BuilderCodec<PoolTradeSlot> CODEC = BuilderCodec.builder(PoolTradeSlot.class, PoolTradeSlot::new)
      .append(new KeyedCodec<>("SlotCount", Codec.INTEGER), (slot, count) -> slot.slotCount = count, slot -> slot.slotCount)
      .addValidator(Validators.greaterThanOrEqual(1))
      .add()
      .<WeightedTrade[]>append(
         new KeyedCodec<>("Trades", new ArrayCodec<>(WeightedTrade.CODEC, WeightedTrade[]::new)), (slot, trades) -> slot.trades = trades, slot -> slot.trades
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected int slotCount = 1;
   protected WeightedTrade[] trades = WeightedTrade.EMPTY_ARRAY;

   public PoolTradeSlot(int slotCount, @Nonnull WeightedTrade[] trades) {
      this.slotCount = slotCount;
      this.trades = trades;
   }

   protected PoolTradeSlot() {
   }

   public int getPoolSlotCount() {
      return this.slotCount;
   }

   @Nonnull
   public WeightedTrade[] getTrades() {
      return this.trades;
   }

   @Nonnull
   @Override
   public List<BarterTrade> resolve(@Nonnull Random random) {
      List<BarterTrade> result = new ObjectArrayList(this.slotCount);
      if (this.trades.length == 0) {
         return result;
      } else {
         ObjectArrayList<WeightedTrade> available = new ObjectArrayList(this.trades.length);
         available.addAll(Arrays.asList(this.trades));
         int toSelect = Math.min(this.slotCount, available.size());

         for (int i = 0; i < toSelect; i++) {
            int selectedIndex = selectWeightedIndex(available, random);
            if (selectedIndex >= 0) {
               WeightedTrade selected = (WeightedTrade)available.remove(selectedIndex);
               result.add(selected.toBarterTrade(random));
            }
         }

         return result;
      }
   }

   @Override
   public int getSlotCount() {
      return this.slotCount;
   }

   private static int selectWeightedIndex(@Nonnull List<WeightedTrade> trades, @Nonnull Random random) {
      if (trades.isEmpty()) {
         return -1;
      } else {
         double totalWeight = 0.0;

         for (WeightedTrade trade : trades) {
            totalWeight += trade.getWeight();
         }

         if (totalWeight <= 0.0) {
            return random.nextInt(trades.size());
         } else {
            double roll = random.nextDouble() * totalWeight;
            double cumulative = 0.0;

            for (int i = 0; i < trades.size(); i++) {
               cumulative += trades.get(i).getWeight();
               if (roll < cumulative) {
                  return i;
               }
            }

            return trades.size() - 1;
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "PoolTradeSlot{slotCount=" + this.slotCount + ", trades=" + Arrays.toString((Object[])this.trades) + "}";
   }
}
