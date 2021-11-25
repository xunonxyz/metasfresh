package de.metas.distribution.ddorder.movement.schedule.plan;

import com.google.common.collect.ImmutableList;
import de.metas.distribution.ddorder.DDOrderId;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromsList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A plan about what HUs and how much to move for a given Distribution Order.
 * <p>
 * These plans can be saved and they become move schedules.
 */
@Value
@Builder
public class DDOrderMovePlan
{
	@NonNull DDOrderId ddOrderId;
	@NonNull ImmutableList<DDOrderMovePlanLine> lines;

	@NonNull AlternativePickFromsList alternativesPool;
}
