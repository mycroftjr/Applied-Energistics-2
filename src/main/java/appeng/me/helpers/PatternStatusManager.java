/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.me.helpers;


import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PatternStatusManager
{

	private final Multimap<ICraftingPatternDetails, IAEStack> patternDetailsIAEStackMultimap;
	private final Multimap<IAEStack, ICraftingPatternDetails> iaeStackPatternDetailsMultimap;

	private List<SavedTransactions> transactions = null;
	private int transDepth = 0;

	public PatternStatusManager( final Multimap<ICraftingPatternDetails, IAEStack> interests, final Multimap<IAEStack, ICraftingPatternDetails> interested )
	{
		this.patternDetailsIAEStackMultimap = interests;
		this.iaeStackPatternDetailsMultimap = interested;
	}

	public void enableTransactions()
	{
		if( this.transDepth == 0 )
		{
			this.transactions = new ArrayList<>();
		}

		this.transDepth++;
	}

	public void disableTransactions()
	{
		this.transDepth--;

		if( this.transDepth == 0 )
		{
			final List<SavedTransactions> myActions = this.transactions;
			this.transactions = null;

			for( final SavedTransactions t : myActions )
			{
				if( t.put )
				{
					this.put( t.patternDetails, t.stack );
				}
				else
				{
					this.remove( t.patternDetails, t.stack );
				}
			}
		}
	}

	public boolean put( final ICraftingPatternDetails craftingPatternDetails, final IAEStack debtStack )
	{
		if( this.transactions != null )
		{
			this.transactions.add( new SavedTransactions( true, craftingPatternDetails, debtStack ) );
			return true;
		}
		else
		{
			this.iaeStackPatternDetailsMultimap.put( debtStack, craftingPatternDetails );
			return this.patternDetailsIAEStackMultimap.put( craftingPatternDetails, debtStack );
		}
	}

	public boolean remove( final ICraftingPatternDetails craftingPatternDetails, final IAEStack debtStack )
	{
		if( this.transactions != null )
		{
			this.transactions.add( new SavedTransactions( false, craftingPatternDetails, debtStack ) );
			return true;
		}
		else
		{
			this.iaeStackPatternDetailsMultimap.remove( debtStack, craftingPatternDetails );
			return this.patternDetailsIAEStackMultimap.remove( craftingPatternDetails, debtStack );
		}
	}

	public boolean containsIncompletablePattern( final ICraftingPatternDetails craftingPatternDetails )
	{
		return this.patternDetailsIAEStackMultimap.containsKey( craftingPatternDetails );
	}

	public boolean containsMissingIAEStack( final IAEStack stack )
	{
		if( ( (IAEItemStack) stack ).getItem().isDamageable() )
		{
			for( IAEStack i : iaeStackPatternDetailsMultimap.keySet() )
			{
				if( i.fuzzyComparison( stack, FuzzyMode.IGNORE_ALL ) )
				{
					return true;
				}
			}
			return false;
		}
		return this.iaeStackPatternDetailsMultimap.containsKey( stack );
	}

	public Collection<IAEStack> getMissingIAEStack( final ICraftingPatternDetails patternDetails )
	{
		return this.patternDetailsIAEStackMultimap.get( patternDetails );
	}

	public Collection<ICraftingPatternDetails> getIncompletablePattern( final IAEStack stack )
	{
		Collection<ICraftingPatternDetails> a = new ArrayList<>();
		if( ( (IAEItemStack) stack ).getItem().isDamageable() )
		{
			for( IAEStack i : iaeStackPatternDetailsMultimap.keySet() )
			{
				if( i.fuzzyComparison( stack, FuzzyMode.IGNORE_ALL ) )
				{
					a.addAll( this.iaeStackPatternDetailsMultimap.get( i ) );
				}
			}
			return a;
		}
		return this.iaeStackPatternDetailsMultimap.get( stack );
	}

	private class SavedTransactions
	{

		private final boolean put;
		private final ICraftingPatternDetails patternDetails;
		private final IAEStack stack;

		public SavedTransactions( final boolean putOperation, final ICraftingPatternDetails myPatternDetails, final IAEStack stack )
		{
			this.put = putOperation;
			this.patternDetails = myPatternDetails;
			this.stack = stack;
		}
	}
}
