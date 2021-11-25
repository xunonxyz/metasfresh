package de.metas.distribution.ddorder.movement.schedule.plan;

import de.metas.product.ProductId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.adempiere.warehouse.LocatorId;

@Value
@Builder
class AllocationGroupingKey
{
	@NonNull ProductId productId;
	@NonNull LocatorId pickFromLocatorId;
}
