package com.hypixel.hytale.server.npc.asset.builder;

import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.instructions.builders.BuilderInstructionReference;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InternalReferenceResolver {
   private final List<BuilderInstructionReference> builders = new ObjectArrayList();
   @Nullable
   private Object2IntMap<String> indexMap;
   @Nullable
   private Int2ObjectMap<String> nameMap = new Int2ObjectOpenHashMap();
   @Nullable
   private IntSet recordedDependencies;

   public InternalReferenceResolver() {
      this.indexMap = new Object2IntOpenHashMap();
      this.indexMap.defaultReturnValue(Integer.MIN_VALUE);
   }

   public int getOrCreateIndex(String name) {
      int index = this.indexMap.getInt(name);
      if (index == Integer.MIN_VALUE) {
         index = this.builders.size();
         this.indexMap.put(name, index);
         this.nameMap.put(index, name);
         this.builders.add(null);
      }

      if (this.recordedDependencies != null) {
         this.recordedDependencies.add(index);
      }

      return index;
   }

   public void setRecordDependencies() {
      this.recordedDependencies = new IntOpenHashSet();
   }

   @Nullable
   public IntSet getRecordedDependenices() {
      return this.recordedDependencies;
   }

   public void stopRecordingDependencies() {
      this.recordedDependencies = null;
   }

   public void addBuilder(int index, BuilderInstructionReference builder) {
      Objects.requireNonNull(builder, "Builder cannot be null when adding as a reference");
      if (index < 0 || index >= this.builders.size()) {
         throw new IllegalArgumentException("Slot for putting builder must be >= 0 and < the size of the list");
      } else if (this.builders.get(index) != null) {
         throw new IllegalStateException(String.format("Duplicate internal reference builder with name: %s", this.nameMap.get(index)));
      } else {
         this.builders.set(index, builder);
      }
   }

   public void validateInternalReferences(String configName, @Nonnull List<String> errors) {
      for (int i = 0; i < this.builders.size(); i++) {
         BuilderInstructionReference builder = this.builders.get(i);
         if (builder == null) {
            errors.add(configName + ": Internal reference builder: " + (String)this.nameMap.get(i) + " doesn't exist");
         } else {
            try {
               this.validateNoCycles(builder, i, new IntArrayList());
            } catch (IllegalArgumentException var6) {
               errors.add(configName + ": " + var6.getMessage());
            }
         }
      }
   }

   private void validateNoCycles(@Nonnull BuilderInstructionReference builder, int index, @Nonnull IntArrayList path) {
      if (path.contains(index)) {
         throw new IllegalArgumentException("Cyclic reference detected for internal component reference: " + (String)this.nameMap.get(index));
      } else {
         path.add(index);
         IntIterator i = builder.getInternalDependencies().iterator();

         while (i.hasNext()) {
            int dependency = i.nextInt();
            BuilderInstructionReference nextBuilder = this.builders.get(dependency);
            if (nextBuilder == null) {
               throw new IllegalStateException("Reference to internal reference builder: " + (String)this.nameMap.get(dependency) + " which doesn't exist");
            }

            this.validateNoCycles(nextBuilder, dependency, path);
         }

         path.removeInt(path.size() - 1);
      }
   }

   public <T> Builder<T> getBuilder(int index, Class<?> classType) {
      if (classType != Instruction.class) {
         throw new IllegalArgumentException("Internal references are currently only supported for instruction list");
      } else {
         return this.builders.get(index);
      }
   }

   public void optimise() {
      this.indexMap = null;
      this.nameMap = null;
   }
}
