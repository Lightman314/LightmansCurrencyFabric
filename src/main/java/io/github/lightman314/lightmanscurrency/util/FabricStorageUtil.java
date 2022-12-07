package io.github.lightman314.lightmanscurrency.util;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class FabricStorageUtil {


    public static ItemStack getStack(ItemVariant item, long amount) { return item.toStack((int)amount); }

    public static boolean hasItem(Storage<ItemVariant> storage, ItemStack stack) { return getItemCount(storage, stack) >= stack.getCount(); }

    public static int getItemCount(Storage<ItemVariant> storage, ItemStack stack) {
        int foundCount = 0;
        Iterator<StorageView<ItemVariant>> iterator = storage.iterator();
        while(iterator.hasNext())
        {
            StorageView<ItemVariant> slot = iterator.next();
            if(slot.getResource().matches(stack))
                foundCount += slot.getAmount();
        }
        return foundCount;
    }

    public static int getItemCount(Storage<ItemVariant> storage, Function<ItemVariant,Boolean> filter)
    {
        int foundCount = 0;
        Iterator<StorageView<ItemVariant>> iterator = storage.iterator();
        while(iterator.hasNext())
        {
            StorageView<ItemVariant> slot = iterator.next();
            if(filter.apply(slot.getResource()))
                foundCount += slot.getAmount();
        }
        return foundCount;
    }

    public static List<ItemStack> getMatchingItems(Storage<ItemVariant> storage, Function<ItemVariant,Boolean> filter)
    {
        List<ItemStack> results = new ArrayList<>();
        Iterator<StorageView<ItemVariant>> iterator = storage.iterator();
        while(iterator.hasNext())
        {
            StorageView<ItemVariant> slot = iterator.next();
            if(filter.apply(slot.getResource()))
                results.add(slot.getResource().toStack());
        }
        return results;
    }

    public static FluidStack getStack(StorageView<FluidVariant> fluidView) {
        return new FluidStack(fluidView.getResource().getFluid(), fluidView.getAmount(), fluidView.getResource().copyNbt());
    }

    public static boolean hasFluid(Storage<FluidVariant> storage, FluidStack stack) { return getFluidQuantity(storage, stack) >= stack.getAmount(); }

    public static long getFluidQuantity(Storage<FluidVariant> storage, FluidStack stack)
    {
        long foundQuantity = 0;
        Iterator<StorageView<FluidVariant>> iterator = storage.iterator();
        while(iterator.hasNext())
        {
            FluidStack fluidEntry = getStack(iterator.next());
            if(stack.matches(fluidEntry))
                foundQuantity += fluidEntry.getAmount();
        }
        return foundQuantity;
    }

    public static <T> Iterator<T> createIterator(List<T> list) { return new LazyIterator<>(list); }

    private static class LazyIterator<T> implements Iterator<T> {
        private int currentIndex = 0;
        private final List<T> baseList;
        LazyIterator(List<T> list) { this.baseList = list; }
        @Override
        public boolean hasNext() { return this.currentIndex < this.baseList.size(); }
        @Override
        public T next() { return this.baseList.get(currentIndex++); }
    }

    public static <T> long insert(Storage<T> storage, T resource, long maxAmount) { return tryInsert(storage, resource, maxAmount, val -> true).getFirst(); }

    public static <T> boolean tryInsertExact(Storage<T> storage, T resource, long exactAmount) { return tryInsert(storage, resource, exactAmount, val -> val == exactAmount).getSecond(); }

    public static <T> Pair<Long,Boolean> tryInsert(Storage<T> storage, T resource, long maxAmount, Function<Long,Boolean> commitCheck) {
        Transaction transaction = newTransaction();
        if(transaction != null)
        {
            long result = storage.insert(resource, maxAmount, transaction);
            boolean commit = commitCheck.apply(result);
            if(commit)
                transaction.commit();
            else
                transaction.abort();
            return Pair.of(result, commit);
        }
        return Pair.of(0L, false);
    }

    public static <T> long simulateInsert(Storage<T> storage, T resource, long maxAmount) {
        Transaction transaction = newTransaction();
        if(transaction != null)
        {
            long result = storage.insert(resource, maxAmount, transaction);
            transaction.abort();
            return result;
        }
        return 0;
    }

    public static <T> long extract(Storage<T> storage, T resource, long maxAmount) { return tryExtract(storage, resource, maxAmount, val -> true).getFirst(); }

    public static <T> boolean tryExtractExact(Storage<T> storage, T resource, long exactAmount) { return tryExtract(storage, resource, exactAmount, val -> val == exactAmount).getSecond(); }

    public static <T> Pair<Long,Boolean> tryExtract(Storage<T> storage, T resource, long maxAmount, Function<Long,Boolean> commitCheck) {
        Transaction transaction = newTransaction();
        if(transaction != null)
        {
            long result = storage.extract(resource, maxAmount, transaction);
            boolean commit = commitCheck.apply(result);
            if(commit)
                transaction.commit();
            else
                transaction.abort();
            return Pair.of(result,commit);
        }
        return Pair.of(0L,false);
    }

    public static <T> long simulateExtract(Storage<T> storage, T resource, long maxAmount) {
        Transaction transaction = newTransaction();
        if(transaction != null)
        {
            long result = storage.extract(resource, maxAmount, transaction);
            transaction.abort();
            return result;
        }
        return 0;
    }

    private static Transaction newTransaction() {
        try { return Transaction.openOuter();
        } catch(Throwable t) { return null; }
    }

}
