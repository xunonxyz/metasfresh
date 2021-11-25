package de.metas.distribution.ddorder.movement.schedule.plan;

import com.google.common.collect.ImmutableList;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKey;
import de.metas.quantity.Quantity;
import lombok.NonNull;

import java.util.Iterator;
import java.util.Optional;

class AllocableHUsList implements Iterable<AllocableHU>
{
	private final ImmutableList<AllocableHU> hus;

	AllocableHUsList(@NonNull final ImmutableList<AllocableHU> hus) {this.hus = hus;}

	@Override
	public @NonNull Iterator<AllocableHU> iterator() {return hus.iterator();}

	public Optional<Quantity> getQtyAvailableToAllocate(final AlternativePickFromKey alternativeKey)
	{
		return hus.stream()
				.filter(hu -> hu.isMatching(alternativeKey))
				.map(AllocableHU::getQtyAvailableToAllocate)
				.reduce(Quantity::add);
	}
}
