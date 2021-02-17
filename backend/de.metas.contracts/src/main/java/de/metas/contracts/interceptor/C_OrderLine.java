package de.metas.contracts.interceptor;

import de.metas.contracts.order.model.I_C_OrderLine;
import de.metas.contracts.subscription.ISubscriptionBL;
import de.metas.lang.SOTrx;
import de.metas.logging.LogManager;
import de.metas.order.IOrderLineBL;
import de.metas.order.OrderLinePriceUpdateRequest;
import de.metas.order.OrderLinePriceUpdateRequest.ResultUOM;
import de.metas.order.compensationGroup.GroupId;
import de.metas.order.compensationGroup.OrderGroupCompensationChangesHandler;
import de.metas.order.compensationGroup.OrderGroupRepository;
import de.metas.util.Services;
import lombok.NonNull;
import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.ad.modelvalidator.annotations.ModelChange;
import org.adempiere.ad.persistence.ModelDynAttributeAccessor;
import org.compiere.model.ModelValidator;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import static org.adempiere.model.InterfaceWrapperHelper.isCopy;
import static org.adempiere.model.InterfaceWrapperHelper.save;

@Interceptor(I_C_OrderLine.class)
@Component
public class C_OrderLine
{
	private static final ModelDynAttributeAccessor<I_C_OrderLine, Boolean> DYNATTR_SkipUpdatingGroupFlatrateConditions = new ModelDynAttributeAccessor<>("SkipUpdatingGroupFlatrateConditions", Boolean.class);
	private static final Logger logger = LogManager.getLogger(de.metas.contracts.interceptor.C_OrderLine.class);
	private final OrderGroupCompensationChangesHandler groupChangesHandler;
	private final IOrderLineBL orderLineBL = Services.get(IOrderLineBL.class);
	private final ISubscriptionBL subscriptionBL = Services.get(ISubscriptionBL.class);

	public C_OrderLine(@NonNull final OrderGroupCompensationChangesHandler groupChangesHandler)
	{
		this.groupChangesHandler = groupChangesHandler;
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW }, skipIfCopying = true)
	public void setSameFlatrateConditionsForWholeCompensationGroupWhenGroupIsCreated(final I_C_OrderLine orderLine)
	{
		if (!orderLine.isGroupCompensationLine())
		{
			return;
		}

		final GroupId groupId = OrderGroupRepository.extractGroupId(orderLine);
		final int flatrateConditionsId = retrieveFirstFlatrateConditionsIdForCompensationGroup(groupId);

		orderLine.setC_Flatrate_Conditions_ID(flatrateConditionsId);

		int excludeOrderLineId = orderLine.getC_OrderLine_ID();
		setFlatrateConditionsIdToCompensationGroup(flatrateConditionsId, groupId, excludeOrderLineId);
	}

	/**
	 * In case the flatrate conditions for an order line is updated and that line is part of an compensation group,
	 * then set the same flatrate conditions to all other lines from the same compensation group.
	 * 
	 * @task https://github.com/metasfresh/metasfresh/issues/3150
	 */
	@ModelChange(timings = { ModelValidator.TYPE_AFTER_CHANGE }, ifColumnsChanged = I_C_OrderLine.COLUMNNAME_C_Flatrate_Conditions_ID, skipIfCopying = true)
	public void setSameFlatrateConditionsForWholeCompensationGroupWhenOneGroupLineChanged(final I_C_OrderLine orderLine)
	{
		if (DYNATTR_SkipUpdatingGroupFlatrateConditions.getValue(orderLine, Boolean.FALSE))
		{
			return;
		}

		final GroupId groupId = OrderGroupRepository.extractGroupIdOrNull(orderLine);
		if (groupId == null)
		{
			return;
		}

		final int flatrateConditionsId = orderLine.getC_Flatrate_Conditions_ID();
		final int excludeOrderLineId = orderLine.getC_OrderLine_ID();
		setFlatrateConditionsIdToCompensationGroup(flatrateConditionsId, groupId, excludeOrderLineId);

		groupChangesHandler.recreateGroupOnOrderLineChanged(orderLine);
	}

	private int retrieveFirstFlatrateConditionsIdForCompensationGroup(final GroupId groupId)
	{
		final Integer flatrateConditionsId = groupChangesHandler.retrieveGroupOrderLinesQuery(groupId)
				.addNotNull(I_C_OrderLine.COLUMNNAME_C_Flatrate_Conditions_ID)
				.orderBy(I_C_OrderLine.COLUMNNAME_Line)
				.orderBy(I_C_OrderLine.COLUMNNAME_C_OrderLine_ID)
				.create()
				.first(I_C_OrderLine.COLUMNNAME_C_Flatrate_Conditions_ID, Integer.class);
		return flatrateConditionsId != null && flatrateConditionsId > 0 ? flatrateConditionsId : -1;
	}

	private void setFlatrateConditionsIdToCompensationGroup(final int flatrateConditionsId, final GroupId groupId, final int excludeOrderLineId)
	{
		groupChangesHandler.retrieveGroupOrderLinesQuery(groupId)
				.addNotEqualsFilter(I_C_OrderLine.COLUMN_C_OrderLine_ID, excludeOrderLineId)
				.addNotEqualsFilter(I_C_OrderLine.COLUMNNAME_C_Flatrate_Conditions_ID, flatrateConditionsId > 0 ? flatrateConditionsId : null)
				.create()
				.list(I_C_OrderLine.class)
				.forEach(otherOrderLine -> {
					otherOrderLine.setC_Flatrate_Conditions_ID(flatrateConditionsId);
					DYNATTR_SkipUpdatingGroupFlatrateConditions.setValue(otherOrderLine, Boolean.TRUE);
					save(otherOrderLine);
				});
	}

	@ModelChange(timings = { ModelValidator.TYPE_BEFORE_NEW, ModelValidator.TYPE_BEFORE_CHANGE }, //
			ifColumnsChanged = { I_C_OrderLine.COLUMNNAME_QtyEntered, I_C_OrderLine.COLUMNNAME_M_DiscountSchemaBreak_ID })
	public void updatePricesOverrideExistingDiscounts(final I_C_OrderLine orderLine)
	{
		if (isCopy(orderLine))
		{
			return;
		}
		if (orderLine.isProcessed())
		{
			return;
		}

		// make the BL revalidate the discounts..the new QtyEntered might also mean a new discount schema break
		orderLine.setM_DiscountSchemaBreak(null);
		if (orderLine.getC_Flatrate_Conditions_ID() <= 0)
		{
			orderLineBL.updatePrices(OrderLinePriceUpdateRequest.builder()
											 .orderLine(orderLine)
											 .resultUOM(ResultUOM.PRICE_UOM)
											 .updatePriceEnteredAndDiscountOnlyIfNotAlreadySet(false) // i.e. always update them
											 .updateLineNetAmt(true)
											 .build());

			logger.debug("Setting TaxAmtInfo for {}", orderLine);
			orderLineBL.setTaxAmtInfo(orderLine);
		}
		else
		{
			final org.compiere.model.I_C_Order order = orderLine.getC_Order();
			final SOTrx soTrx = SOTrx.ofBoolean(order.isSOTrx());

			if (soTrx.isPurchase())
			{
				return; // leave this job to the adempiere standard callouts
			}

			final boolean updatePriceEnteredAndDiscountOnlyIfNotAlreadySet = true;
			subscriptionBL.updatePrices(orderLine, soTrx, updatePriceEnteredAndDiscountOnlyIfNotAlreadySet);
		}
	}
}
