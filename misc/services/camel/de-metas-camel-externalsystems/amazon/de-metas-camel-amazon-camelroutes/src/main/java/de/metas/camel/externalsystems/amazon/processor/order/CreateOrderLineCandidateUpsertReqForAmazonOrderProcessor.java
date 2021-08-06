/*
 * #%L
 * de-metas-camel-amazon-camelroutes
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.camel.externalsystems.amazon.processor.order;

import static de.metas.camel.externalsystems.amazon.AmazonConstants.EXTERNAL_ID_PREFIX;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.DATA_SOURCE_INT_AMAZON;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.DEFAULT_DELIVERY_RULE;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.DEFAULT_DELIVERY_VIA_RULE;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.DEFAULT_ORDER_LINE_DISCOUNT;
import static de.metas.camel.externalsystems.amazon.ProcessorHelper.getPropertyOrThrowError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.metas.camel.externalsystems.amazon.AmazonImportOrdersRouteContext;
import de.metas.camel.externalsystems.amazon.AmazonUtils;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderBuyerInfo;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderItem;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderItemsList;
import de.metas.common.bpartner.v2.response.JsonResponseBPartnerCompositeUpsert;
import de.metas.common.bpartner.v2.response.JsonResponseBPartnerCompositeUpsertItem;
import de.metas.common.bpartner.v2.response.JsonResponseUpsertItem;
import de.metas.common.ordercandidates.v2.request.JsonOLCandCreateBulkRequest;
import de.metas.common.ordercandidates.v2.request.JsonOLCandCreateRequest;
import de.metas.common.ordercandidates.v2.request.JsonRequestBPartnerLocationAndContact;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.common.util.Check;
import lombok.NonNull;

public class CreateOrderLineCandidateUpsertReqForAmazonOrderProcessor implements Processor
{

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		final AmazonImportOrdersRouteContext importOrdersRouteContext = getPropertyOrThrowError(exchange, ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, AmazonImportOrdersRouteContext.class);
		log.debug("Create OLCs for amazon order {}", importOrdersRouteContext.getOrderNotNull().getAmazonOrderId());

		// get created business partners.
		final JsonResponseBPartnerCompositeUpsert bPartnerUpsertResponseList = exchange.getIn().getBody(JsonResponseBPartnerCompositeUpsert.class);
		final JsonResponseBPartnerCompositeUpsertItem bPartnerUpsertResponse = Check.singleElement(bPartnerUpsertResponseList.getResponseItems());

		if (bPartnerUpsertResponse == null)
		{
			throw new RuntimeException("No JsonResponseUpsert present! OrderId=" + importOrdersRouteContext.getOrderNotNull().getAmazonOrderId());
		}

		final JsonOLCandCreateBulkRequest olCandBulkRequest = buildOlCandRequest(importOrdersRouteContext, bPartnerUpsertResponse);

		exchange.getIn().setBody(olCandBulkRequest);
	}

	private JsonOLCandCreateBulkRequest buildOlCandRequest(
			@NonNull final AmazonImportOrdersRouteContext context,
			@NonNull final JsonResponseBPartnerCompositeUpsertItem bPartnerUpsertResponse)
	{

		final JsonOLCandCreateBulkRequest.JsonOLCandCreateBulkRequestBuilder olCandCreateBulkRequestBuilder = JsonOLCandCreateBulkRequest.builder();

		final Order order = context.getOrderNotNull();
		final OrderItemsList orderItems = context.getOrderItemsList();

		
		final JsonOLCandCreateRequest.JsonOLCandCreateRequestBuilder olCandCreateRequestBuilder = JsonOLCandCreateRequest.builder();
		olCandCreateRequestBuilder
				.orgCode(context.getOrgCode())
				.currencyCode(getCurrencyCode(order))
				.externalHeaderId(order.getAmazonOrderId())
				.poReference(order.getAmazonOrderId())
				.bpartner(getBPartnerInfo(context, bPartnerUpsertResponse))
				.billBPartner(getBillBPartnerInfo(context, bPartnerUpsertResponse))
				.dateOrdered(getDateOrdered(order))
				.dateRequired(getDateOrdered(order).plusDays(7)) // TODO - no mapping.
				.dataSource(DATA_SOURCE_INT_AMAZON)
				.isManualPrice(true)
				.isImportedWithIssues(true)
				.discount(DEFAULT_ORDER_LINE_DISCOUNT)
				.deliveryViaRule(DEFAULT_DELIVERY_VIA_RULE)
				.deliveryRule(DEFAULT_DELIVERY_RULE)
				.importWarningMessage("PRE ALPHA TEST IMPORT"); // FIXME ;)

		
		final List<OrderItem> orderLines = orderItems.getOrderItems();

		if (orderLines.isEmpty())
		{
			throw new RuntimeException("Missing order lines! OrderId=" + order.getAmazonOrderId());
		}

		orderLines.stream()
				.map(orderLine -> processOrderLine(olCandCreateRequestBuilder, order, orderLine))
				.forEach(olCandCreateBulkRequestBuilder::request);

		return olCandCreateBulkRequestBuilder.build();
	}
	
	
	@NonNull
	private JsonRequestBPartnerLocationAndContact getBPartnerInfo(
			@NonNull final AmazonImportOrdersRouteContext context,
			@NonNull final JsonResponseBPartnerCompositeUpsertItem bPartnerUpsertResponse)
	{
		final Order orderAndCustomId = context.getOrderNotNull();
		final OrderBuyerInfo buyerInfo = context.getOrderBuyerInfo();
		
		final String bPartnerExternalIdentifier = EXTERNAL_ID_PREFIX + buyerInfo.getBuyerName();

		final JsonMetasfreshId bpartnerId = getMetasfreshIdForExternalIdentifier(ImmutableList.of(bPartnerUpsertResponse.getResponseBPartnerItem()), bPartnerExternalIdentifier);

		final String shippingBPLocationExternalIdentifier = context.getShippingBPLocationExternalIdNotNull();
		final JsonMetasfreshId shippingBPartnerLocationId = getMetasfreshIdForExternalIdentifier(bPartnerUpsertResponse.getResponseLocationItems(), shippingBPLocationExternalIdentifier);

		return JsonRequestBPartnerLocationAndContact.builder()
				.bPartnerIdentifier(String.valueOf(bpartnerId.getValue()))
				.bPartnerLocationIdentifier(String.valueOf(shippingBPartnerLocationId.getValue()))
				.build();
	}

	@NonNull
	private JsonRequestBPartnerLocationAndContact getBillBPartnerInfo(
			@NonNull final AmazonImportOrdersRouteContext context,
			@NonNull final JsonResponseBPartnerCompositeUpsertItem bPartnerUpsertResponse)
	{
		final Order orderAndCustomId = context.getOrderNotNull();
		final OrderBuyerInfo buyerInfo = context.getOrderBuyerInfo();

		final String bPartnerExternalIdentifier = EXTERNAL_ID_PREFIX + buyerInfo.getBuyerName();;

		final JsonMetasfreshId bpartnerId = getMetasfreshIdForExternalIdentifier(ImmutableList.of(bPartnerUpsertResponse.getResponseBPartnerItem()), bPartnerExternalIdentifier);

		final String billingBPLocationExternalIdentifier = context.getBillingBPLocationExternalIdNotNull();
		final JsonMetasfreshId billingBPartnerLocationId = getMetasfreshIdForExternalIdentifier(bPartnerUpsertResponse.getResponseLocationItems(), billingBPLocationExternalIdentifier);

		return JsonRequestBPartnerLocationAndContact.builder()
				.bPartnerIdentifier(String.valueOf(bpartnerId.getValue()))
				.bPartnerLocationIdentifier(String.valueOf(billingBPartnerLocationId.getValue()))
				.build();
	}

	private JsonOLCandCreateRequest processOrderLine(
			@NonNull final JsonOLCandCreateRequest.JsonOLCandCreateRequestBuilder olCandCreateRequestBuilder,
			@NonNull final Order order,
			@NonNull final OrderItem orderLine)
	{
		return olCandCreateRequestBuilder
				.externalLineId(orderLine.getOrderItemId())
				.productIdentifier(orderLine.getSellerSKU())
				.price(new BigDecimal(orderLine.getItemPrice().getAmount()).add(new BigDecimal(orderLine.getItemTax().getAmount()) ) )
				.currencyCode(orderLine.getItemPrice().getCurrencyCode())
				.qty(BigDecimal.valueOf(orderLine.getQuantityOrdered()))
				.description(orderLine.getTitle())
				.description(orderLine.getTitle())
				.dateCandidate(getDateCandidate(order))
				.build();
	}

	@Nullable
	private LocalDate getDateCandidate(@NonNull final Order orderLine)
	{
		if (orderLine.getLastUpdateDate() != null)
		{
			return AmazonUtils.toLocalDate(orderLine.getLastUpdateDate());
		}
		else if (orderLine.getPurchaseDate() != null)
		{
			return AmazonUtils.toLocalDate(orderLine.getPurchaseDate());
		}

		return null;
	}

	@NonNull
	private JsonMetasfreshId getMetasfreshIdForExternalIdentifier(
			@NonNull final List<JsonResponseUpsertItem> bPartnerResponseUpsertItems,
			@NonNull final String externalIdentifier)
	{
		return bPartnerResponseUpsertItems
				.stream()
				.filter(responseItem -> responseItem.getIdentifier().equals(externalIdentifier) && responseItem.getMetasfreshId() != null)
				.findFirst()
				.map(JsonResponseUpsertItem::getMetasfreshId)
				.orElseThrow(() -> new RuntimeException("Something went wrong! No JsonResponseUpsertItem was found for the externalIdentifier:" + externalIdentifier));
	}

	@Nullable
	private String getCurrencyCode(@NonNull final Order order)
	{
		return order.getOrderTotal().getCurrencyCode();
	}

	@Nullable
	private LocalDate getDateOrdered(@NonNull final Order order)
	{
		return order.getPurchaseDate() != null
				? AmazonUtils.toLocalDate(order.getPurchaseDate())
				: null;
	}
}
