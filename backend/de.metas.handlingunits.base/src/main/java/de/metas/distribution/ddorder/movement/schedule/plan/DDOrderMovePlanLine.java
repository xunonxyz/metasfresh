package de.metas.distribution.ddorder.movement.schedule.plan;

import com.google.common.collect.ImmutableList;
import de.metas.distribution.ddorder.DDOrderLineId;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKeys;
import de.metas.quantity.Quantity;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@Builder
public class DDOrderMovePlanLine
{
	@NonNull DDOrderLineId ddOrderLineId;
	@NonNull Quantity qtyToPickTarget;
	@NonNull ImmutableList<DDOrderMovePlanStep> steps;

	@With
	@NonNull AlternativePickFromKeys alternatives;

	public boolean isFullyAllocated()
	{
		return qtyToPickTarget.subtract(getQtyToPick()).isZero();
	}

	public Quantity getQtyToPick()
	{
		return steps.stream()
				.map(DDOrderMovePlanStep::getQtyToPick)
				.reduce(Quantity::add)
				.orElseGet(qtyToPickTarget::toZero);
	}
}
