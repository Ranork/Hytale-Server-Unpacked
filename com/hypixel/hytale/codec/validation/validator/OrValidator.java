package com.hypixel.hytale.codec.validation.validator;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class OrValidator<T> implements Validator<T> {
   private final Validator<T>[] validators;

   public OrValidator(Validator<T>[] validators) {
      this.validators = validators;
   }

   @Override
   public void accept(T t, @Nonnull ValidationResults results) {
      ObjectArrayList<ValidationResults.ValidationResult> possibleResults = new ObjectArrayList();
      List<ValidationResults.ValidationResult> oldResults = results.getResults();

      for (Validator<T> validator : this.validators) {
         results.setResults(null);
         validator.accept(t, results);
         if (!results.hasFailed()) {
            results.setResults(oldResults);
            return;
         }

         possibleResults.addAll(results.getResults());
      }

      results.setResults(oldResults);
      ObjectListIterator var9 = possibleResults.iterator();

      while (var9.hasNext()) {
         ValidationResults.ValidationResult p = (ValidationResults.ValidationResult)var9.next();
         results.add(p);
      }
   }

   @Override
   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      if (target.getAnyOf() == null) {
         BuilderCodec<? extends Schema> subCodec = (BuilderCodec<? extends Schema>)Schema.CODEC.getCodecFor((Class<? extends Schema>)target.getClass());
         Schema[] anyOf = new Schema[this.validators.length];
         int index = 0;
         Schema def = subCodec.getSupplier().get();

         for (Validator<T> val : this.validators) {
            Schema base = subCodec.getSupplier().get();
            val.updateSchema(context, base);
            if (!base.equals(def)) {
               anyOf[index++] = base;
            }
         }

         if (index != 0) {
            target.setAnyOf(Arrays.copyOf(anyOf, index));
         }
      } else {
         for (Schema c : target.getAnyOf()) {
            this.updateSchema(context, c);
         }
      }
   }
}
