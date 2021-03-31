/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util.item;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraftforge.oredict.OreDictionary;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AESharedItemStack.Bounds;


    private final Reference2ObjectMap<Item, ItemVariantList> records = new Reference2ObjectOpenHashMap<>();
    /**
     * We increment this version field everytime an attempt to mutate this item list (or potentially one of its
     * sub-lists) is made. Iterators will copy the version when they are created and compare it against the current
     * version whenever they advance to trigger a {@link ConcurrentModificationException}.
     */
    private final AtomicInteger version = new AtomicInteger(0);

    @Override
    public IAEItemStack findPrecise(final IAEItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        ItemVariantList record = this.records.get(itemStack.getItem());
        return record != null ? record.findPrecise(itemStack) : null;
    }

		if( st != null )
		{
			st.add( option );
			return;
		}

        ItemVariantList record = this.records.get(filter.getItem());
        return record != null ? record.findFuzzy(filter, fuzzy) : Collections.emptyList();
    }

		this.putItemRecord( opt );
	}

    @Override
    public void add(final IAEItemStack itemStack) {
        version.incrementAndGet();

        if (itemStack == null) {
            return;
        }

		return this.records.get( ( (AEItemStack) itemStack ).getSharedStack() );
	}

    @Override
    public void addStorage(final IAEItemStack itemStack) {
        version.incrementAndGet();

        if (itemStack == null) {
            return;
        }

		final AEItemStack ais = (AEItemStack) filter;

    @Override
    public void addCrafting(final IAEItemStack itemStack) {
        version.incrementAndGet();

        if (itemStack == null) {
            return;
        }

				return this.findFuzzyDamage( is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE );
			}
			else
			{
				final Collection<IAEItemStack> output = new ArrayList<>();

    @Override
    public void addRequestable(final IAEItemStack itemStack) {
        version.incrementAndGet();

        if (itemStack == null) {
            return;
        }

				return output;
			}
		} ).orElse( this.findFuzzyDamage( ais, fuzzy, false ) );
	}

	@Override
	public boolean isEmpty()
	{
		return !this.iterator().hasNext();
	}

	@Override
	public void addStorage( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

    @Override
    public int size() {
        int size = 0;
        for (ItemVariantList entry : records.values()) {
            size += entry.size();
        }

		if( st != null )
		{
			st.incStackSize( option.getStackSize() );
			return;
		}

    @Override
    public Iterator<IAEItemStack> iterator() {
        return new ChainedIterator(this.records.values().iterator(), version);
    }

		this.putItemRecord( opt );
	}

    private ItemVariantList getOrCreateRecord(Item item) {
        return this.records.computeIfAbsent(item, this::makeRecordMap);
    }

    private ItemVariantList makeRecordMap(Item item) {
        if (item.isDamageable()) {
            return new FuzzyItemVariantList();
        } else {
            return new NormalItemVariantList();
        }
    }

    /**
     * Iterates over multiple item lists as if they were one list.
     */
    private static class ChainedIterator implements Iterator<IAEItemStack> {

        private final AtomicInteger parentVersion;
        private final int version;
        private final Iterator<ItemVariantList> parent;
        private Iterator<IAEItemStack> next;

        public ChainedIterator(Iterator<ItemVariantList> iterator, AtomicInteger parentVersion) {
            this.parent = iterator;
            this.parentVersion = parentVersion;
            this.version = parentVersion.get();
            this.ensureItems();
        }

        @Override
        public boolean hasNext() {
            return next != null && next.hasNext();
        }

        @Override
        public IAEItemStack next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            if (this.version != this.parentVersion.get()) {
                throw new ConcurrentModificationException();
            }

            IAEItemStack result = this.next.next();
            this.ensureItems();
            return result;
        }

        private void ensureItems() {
            if (hasNext()) {
                return; // Still items left in the current one
            }

            // Find the next iterator willing to return some items...
            while (this.parent.hasNext()) {
                this.next = this.parent.next().iterator();

                if (this.next.hasNext()) {
                    return; // Found one!
                }
            }

            // No more items
            this.next = null;
        }
    }
}
