package com.hypixel.hytale.server.npc.valuestore;

import com.hypixel.hytale.server.npc.asset.builder.BuilderContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ValueStoreValidator {
   private final EnumMap<ValueStore.Type, HashMap<String, List<ValueStoreValidator.ValueUsage>>> usages = new EnumMap<>(ValueStore.Type.class);

   public void registerValueUsage(@Nonnull ValueStoreValidator.ValueUsage usage) {
      if (usage.useType != ValueStoreValidator.UseType.READ) {
         HashMap<String, List<ValueStoreValidator.ValueUsage>> usagesOfType = this.usages.computeIfAbsent(usage.valueType, k -> new HashMap<>());
         List<ValueStoreValidator.ValueUsage> usagesByParameter = usagesOfType.computeIfAbsent(usage.name, k -> new ObjectArrayList());
         usagesByParameter.add(usage);
      }
   }

   public boolean validate(@Nonnull List<String> errors) {
      boolean result = true;

      for (ValueStore.Type type : ValueStore.Type.VALUES) {
         result &= this.validateType(type, errors);
      }

      return result;
   }

   private boolean validateType(@Nonnull ValueStore.Type type, @Nonnull List<String> errors) {
      HashMap<String, List<ValueStoreValidator.ValueUsage>> usagesOfType = this.usages.get(type);
      if (usagesOfType == null) {
         return true;
      } else {
         boolean result = true;
         ObjectArrayList<ValueStoreValidator.ValueUsage> writes = new ObjectArrayList();
         ObjectArrayList<ValueStoreValidator.ValueUsage> exclusiveWrites = new ObjectArrayList();

         for (Entry<String, List<ValueStoreValidator.ValueUsage>> usagesByParameter : usagesOfType.entrySet()) {
            for (ValueStoreValidator.ValueUsage usage : usagesByParameter.getValue()) {
               writes.add(usage);
               if (usage.useType == ValueStoreValidator.UseType.EXCLUSIVE_WRITE) {
                  exclusiveWrites.add(usage);
               }
            }

            if (writes.size() > 1 && !exclusiveWrites.isEmpty()) {
               StringBuilder sb = new StringBuilder();
               sb.append("The core components [ ");
               ObjectListIterator var13 = exclusiveWrites.iterator();

               while (var13.hasNext()) {
                  ValueStoreValidator.ValueUsage writer = (ValueStoreValidator.ValueUsage)var13.next();
                  sb.append(writer.context.getLabel()).append(" ");
               }

               sb.append("] require an exclusive write of the ")
                  .append(type.get())
                  .append(" parameter '")
                  .append(usagesByParameter.getKey())
                  .append("' but it is written to by [ ");
               var13 = writes.iterator();

               while (var13.hasNext()) {
                  ValueStoreValidator.ValueUsage writer = (ValueStoreValidator.ValueUsage)var13.next();
                  sb.append(writer.context.getLabel()).append(" ");
               }

               sb.append("]");
               errors.add(sb.toString());
               result = false;
            }

            writes.clear();
            exclusiveWrites.clear();
         }

         return result;
      }
   }

   public static enum UseType implements Supplier<String> {
      READ("Reads the value"),
      WRITE("Writes the value"),
      EXCLUSIVE_WRITE("Has exclusive write ownership of the value");

      private final String description;

      private UseType(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }

   public static class ValueUsage {
      protected final String name;
      protected final ValueStore.Type valueType;
      protected final ValueStoreValidator.UseType useType;
      protected final BuilderContext context;

      public ValueUsage(String name, ValueStore.Type valueType, ValueStoreValidator.UseType useType, BuilderContext context) {
         this.name = name;
         this.valueType = valueType;
         this.useType = useType;
         this.context = context;
      }
   }
}
