package de.metas.distribution.ddorder.movement.schedule.plan;

import com.google.common.collect.ImmutableList;
import de.metas.bpartner.ShipmentAllocationBestBeforePolicy;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKey;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.HUsLoadingCache;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.PickFromHU;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.PickFromHUsGetRequest;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.PickFromHUsSupplier;
import de.metas.handlingunits.storage.IHUStorageFactory;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.util.collections.CollectionUtils;
import lombok.NonNull;
import org.adempiere.mm.attributes.AttributeSetInstanceId;

import java.util.HashMap;
import java.util.Optional;

class AllocableHUsGroups
{
	private final PickFromHUsSupplier pickFromHUsSupplier;
	private final IHUStorageFactory storageFactory;

	private final HashMap<AllocationGroupingKey, AllocableHUsList> groups = new HashMap<>();

	AllocableHUsGroups(
			@NonNull final PickFromHUsSupplier pickFromHUsSupplier,
			@NonNull final IHUStorageFactory storageFactory)
	{
		this.pickFromHUsSupplier = pickFromHUsSupplier;
		this.storageFactory = storageFactory;
	}

	public AllocableHUsList getAvailableHUsToPick(AllocationGroupingKey key)
	{
		return groups.computeIfAbsent(key, this::retrieveAvailableHUsToPick);
	}

	private AllocableHUsList retrieveAvailableHUsToPick(AllocationGroupingKey key)
	{
		final ProductId productId = key.getProductId();
		final ImmutableList<PickFromHU> husEligibleToPick = pickFromHUsSupplier.getEligiblePickFromHUs(
				PickFromHUsGetRequest.builder()
						.pickFromLocatorId(key.getPickFromLocatorId())
						.productId(productId)
						.asiId(AttributeSetInstanceId.NONE)
						.bestBeforePolicy(ShipmentAllocationBestBeforePolicy.Expiring_First)
						.reservationRef(Optional.empty()) // TODO introduce some DD Order Step reservation
						.build());

		final ImmutableList<AllocableHU> hus = CollectionUtils.map(husEligibleToPick, pickFromHU -> toAllocableHU(pickFromHU, productId));
		return new AllocableHUsList(hus);
	}

	private AllocableHU toAllocableHU(@NonNull final PickFromHU pickFromHU, @NonNull final ProductId productId)
	{
		final HUsLoadingCache husCache = pickFromHUsSupplier.getHusCache();
		final I_M_HU hu = husCache.getHUById(pickFromHU.getTopLevelHUId());
		return new AllocableHU(storageFactory, hu, productId);
	}

	public boolean hasQtyAvailableToAllocate(@NonNull AlternativePickFromKey alternativeKey)
	{
		return getQtyAvailableToAllocate(alternativeKey)
				.filter(Quantity::isPositive)
				.isPresent();
	}

	public Optional<Quantity> getQtyAvailableToAllocate(@NonNull AlternativePickFromKey alternativeKey)
	{
		final AllocableHUsList group = groups.get(AllocationGroupingKey.builder()
				.productId(alternativeKey.getProductId())
				.pickFromLocatorId(alternativeKey.getLocatorId())
				.build());

		if(group == null)
		{
			return Optional.empty();
		}

		return group.getQtyAvailableToAllocate(alternativeKey);
	}
}
