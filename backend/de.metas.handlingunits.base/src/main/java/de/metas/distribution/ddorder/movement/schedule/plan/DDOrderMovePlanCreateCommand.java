package de.metas.distribution.ddorder.movement.schedule.plan;

import com.google.common.collect.ImmutableList;
import de.metas.distribution.ddorder.DDOrderId;
import de.metas.distribution.ddorder.DDOrderLineId;
import de.metas.distribution.ddorder.lowlevel.DDOrderLowLevelDAO;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.exceptions.HUException;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFrom;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKey;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromKeys;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.AlternativePickFromsList;
import de.metas.handlingunits.picking.plan.generator.pickFromHUs.PickFromHUsSupplier;
import de.metas.handlingunits.reservation.HUReservationService;
import de.metas.i18n.AdMessageKey;
import de.metas.product.ProductId;
import de.metas.quantity.Quantity;
import de.metas.uom.IUOMDAO;
import de.metas.util.Services;
import lombok.Builder;
import lombok.NonNull;
import org.adempiere.warehouse.LocatorId;
import org.adempiere.warehouse.api.IWarehouseDAO;
import org.compiere.model.I_C_UOM;
import org.eevolution.model.I_DD_Order;
import org.eevolution.model.I_DD_OrderLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DDOrderMovePlanCreateCommand
{
	private final IUOMDAO uomDAO = Services.get(IUOMDAO.class);
	private final IWarehouseDAO warehouseDAO = Services.get(IWarehouseDAO.class);
	private final DDOrderLowLevelDAO ddOrderLowLevelDAO;

	private static final AdMessageKey MSG_CannotFullAllocate = AdMessageKey.of("de.metas.handlingunits.ddorder.api.impl.HUDDOrderBL.NoHu_For_Product");

	//
	// Parameters
	@NonNull private final I_DD_Order ddOrder;
	private final boolean failIfNotFullAllocated;
	private final boolean computeAlternatives;

	//
	// State
	private final AllocableHUsGroups allocableHUsGroups;

	@Builder
	private DDOrderMovePlanCreateCommand(
			final @NonNull DDOrderLowLevelDAO ddOrderLowLevelDAO,
			final @NonNull HUReservationService huReservationService,
			//
			final @NonNull DDOrderMovePlanCreateRequest request)
	{
		this.ddOrderLowLevelDAO = ddOrderLowLevelDAO;
		this.ddOrder = request.getDdOrder();
		this.failIfNotFullAllocated = request.isFailIfNotFullAllocated();
		this.computeAlternatives = request.isComputeAlternatives();

		this.allocableHUsGroups = new AllocableHUsGroups(
				PickFromHUsSupplier.builder()
						.huReservationService(huReservationService)
						.build(),
				Services.get(IHandlingUnitsBL.class).getStorageFactory());
	}

	public DDOrderMovePlan execute()
	{
		ImmutableList<DDOrderMovePlanLine> lines = ddOrderLowLevelDAO.retrieveLines(ddOrder)
				.stream()
				.map(this::createPlanLine)
				.collect(ImmutableList.toImmutableList());

		//
		// Compute alternatives
		final AlternativePickFromsList alternativesPool;
		if (computeAlternatives)
		{
			lines = lines.stream()
					.map(this::removeUnavailableAlternatives)
					.collect(ImmutableList.toImmutableList());
			alternativesPool = getRelevantAlternativesFor(lines);
		}
		else
		{
			alternativesPool = AlternativePickFromsList.EMPTY;
		}

		return DDOrderMovePlan.builder()
				.ddOrderId(DDOrderId.ofRepoId(ddOrder.getDD_Order_ID()))
				.lines(lines)
				.alternativesPool(alternativesPool)
				.build();
	}

	public DDOrderMovePlanLine createPlanLine(final I_DD_OrderLine ddOrderLine)
	{
		final ProductId productId = ProductId.ofRepoId(ddOrderLine.getM_Product_ID());
		final LocatorId pickFromLocatorId = warehouseDAO.getLocatorIdByRepoId(ddOrderLine.getM_Locator_ID());
		final LocatorId dropToLocatorId = warehouseDAO.getLocatorIdByRepoId(ddOrderLine.getM_LocatorTo_ID());
		final Quantity targetQty = getQtyEntered(ddOrderLine);

		final AllocableHUsList availableHUs = allocableHUsGroups.getAvailableHUsToPick(AllocationGroupingKey.builder()
				.productId(productId)
				.pickFromLocatorId(pickFromLocatorId)
				.build());

		final ArrayList<DDOrderMovePlanStep> planSteps = new ArrayList<>();
		final ArrayList<AlternativePickFromKey> alternativeIds = new ArrayList<>();
		Quantity remainingQtyToAllocate = targetQty;
		for (final AllocableHU allocableHU : availableHUs)
		{
			//
			// Skip HUs which were fully allocated
			if (!allocableHU.hasQtyAvailableToAllocate())
			{
				continue;
			}

			//
			// Case we fully allocated the target quantity
			if (remainingQtyToAllocate.isPositive())
			{
				final Quantity huQtyAvailable = allocableHU.getQtyAvailableToAllocate();
				// TODO: handle/convert when HU's UOM != DD_OrderLine's UOM

				final DDOrderMovePlanStep.DDOrderMovePlanStepBuilder planStepBuilder = DDOrderMovePlanStep.builder()
						.productId(productId)
						.pickFromLocatorId(pickFromLocatorId)
						.dropToLocatorId(dropToLocatorId)
						.pickFromHU(allocableHU.getHu());

				//
				// Case: the HU contains more than we need
				if (huQtyAvailable.isGreaterThan(remainingQtyToAllocate))
				{
					planStepBuilder.qtyToPick(remainingQtyToAllocate).isPickWholeHU(false);
				}
				//
				// Case: the HU contains less or exactly what we need
				else
				{
					planStepBuilder.qtyToPick(huQtyAvailable).isPickWholeHU(true);
				}

				final DDOrderMovePlanStep planStep = planStepBuilder.build();
				planSteps.add(planStep);
				allocableHU.addQtyAllocated(planStep.getQtyToPick());
				remainingQtyToAllocate = remainingQtyToAllocate.subtract(planStep.getQtyToPick());
			}

			//
			// If we fulfilled the target quantity,
			// consider the rest as an alternative (if asked),
			// or stop here if we don't want to consider alternatives
			if (remainingQtyToAllocate.signum() <= 0)
			{
				if (computeAlternatives)
				{
					if (allocableHU.hasQtyAvailableToAllocate())
					{
						alternativeIds.add(allocableHU.toAlternativePickFromKey());
					}
				}
				else
				{
					break;
				}
			}
		}

		final DDOrderMovePlanLine planLine = DDOrderMovePlanLine.builder()
				.ddOrderLineId(DDOrderLineId.ofRepoId(ddOrderLine.getDD_OrderLine_ID()))
				.qtyToPickTarget(targetQty)
				.steps(ImmutableList.copyOf(planSteps))
				.alternatives(AlternativePickFromKeys.ofCollection(alternativeIds))
				.build();

		if (failIfNotFullAllocated && !planLine.isFullyAllocated())
		{
			throw new HUException(MSG_CannotFullAllocate)
					.appendParametersToMessage()
					.setParameter("Product", ddOrderLine.getM_Product_ID())
					.setParameter("Locator", pickFromLocatorId);

		}

		return planLine;
	}

	@NonNull
	private Quantity getQtyEntered(final @NonNull I_DD_OrderLine ddOrderLine)
	{
		final I_C_UOM uom = uomDAO.getById(ddOrderLine.getC_UOM_ID());
		return Quantity.of(ddOrderLine.getQtyEntered(), uom);
	}

	private DDOrderMovePlanLine removeUnavailableAlternatives(@NonNull final DDOrderMovePlanLine line)
	{
		final AlternativePickFromKeys alternativeKeys = line.getAlternatives()
				.filter(allocableHUsGroups::hasQtyAvailableToAllocate);

		return line.withAlternatives(alternativeKeys);
	}

	private AlternativePickFromsList getRelevantAlternativesFor(final List<DDOrderMovePlanLine> lines)
	{
		final HashSet<AlternativePickFromKey> keysConsidered = new HashSet<>();
		final ArrayList<AlternativePickFrom> alternatives = new ArrayList<>();
		for (final DDOrderMovePlanLine line : lines)
		{
			for (final AlternativePickFromKey key : line.getAlternatives())
			{
				if (keysConsidered.add(key))
				{
					allocableHUsGroups.getQtyAvailableToAllocate(key)
							.filter(Quantity::isPositive)
							.map(availableQty -> AlternativePickFrom.of(key, availableQty))
							.ifPresent(alternatives::add);
				}
			}
		}

		return AlternativePickFromsList.ofList(alternatives);
	}
}
