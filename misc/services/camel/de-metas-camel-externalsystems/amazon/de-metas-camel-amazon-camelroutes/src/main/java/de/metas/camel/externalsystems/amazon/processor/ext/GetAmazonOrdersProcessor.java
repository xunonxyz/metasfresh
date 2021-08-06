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

package de.metas.camel.externalsystems.amazon.processor.ext;

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.HEADER_ORG_CODE;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.HEADER_PINSTANCE_ID;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import de.metas.camel.externalsystems.amazon.AmazonConstants;
import de.metas.camel.externalsystems.amazon.AmazonImportOrdersRouteContext;
import de.metas.camel.externalsystems.amazon.api.OrdersV0Api;
import de.metas.camel.externalsystems.amazon.api.model.orders.Error;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrdersResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrdersList;
import de.metas.camel.externalsystems.amazon.api.orders.invoker.ApiClient;
import de.metas.camel.externalsystems.amazon.api.orders.invoker.Configuration;
import de.metas.camel.externalsystems.common.ProcessLogger;
import de.metas.common.externalsystem.JsonExternalSystemRequest;

public class GetAmazonOrdersProcessor implements Processor
{

	protected Logger log = LoggerFactory.getLogger(getClass());

	private final ProcessLogger processLogger;

	public GetAmazonOrdersProcessor(final ProcessLogger processLogger)
	{
		this.processLogger = processLogger;
	}

	@Override
	public void process(Exchange exchange) throws Exception
	{
		log.debug("Execute amazon order request");

		final JsonExternalSystemRequest request = exchange.getIn().getBody(JsonExternalSystemRequest.class);

		exchange.getIn().setHeader(HEADER_ORG_CODE, request.getOrgCode());
		if (request.getAdPInstanceId() != null)
		{
			exchange.getIn().setHeader(HEADER_PINSTANCE_ID, request.getAdPInstanceId().getValue());

			processLogger.logMessage("Amazon:GetOrders process started!" + Instant.now(), request.getAdPInstanceId().getValue());
		}

		
		
		// TODO: sort out real amazon api access.
		
		
		// construct api client or use provided one.
		final OrdersV0Api orderApi;
		if (exchange.getIn().getHeader(AmazonConstants.ROUTE_PROPERTY_AMAZON_CLIENT) == null)
		{
			log.debug("Constructing amazon api client");
			
			ApiClient defaultClient = Configuration.getDefaultApiClient();
			orderApi = new OrdersV0Api(defaultClient);
		}
		else
		{
			log.debug("Using provided amazon api client");
			
			orderApi = (OrdersV0Api)exchange.getIn().getHeader(AmazonConstants.ROUTE_PROPERTY_AMAZON_CLIENT);
		}
		
		
		

		// required
		List<String> marketplaceIds = new ArrayList<>();

		// NOTE: always get orders of last x days, as there could be pending orders and fulfilled orders mixed up.

		// one of both, ISO 8601
		String createdAfter = "";
		String lastUpdatedAfter = null;

		// optional
		String createdBefore = null;
		String lastUpdatedBefore = null;

		// useful for MF
		List<String> orderStatuses = null; // OrderStatusEnum.
		List<String> fulfillmentChannels = null; // FBA (Fulfillment by Amazon); SellerFulfilled (Fulfilled by the seller)
		List<String> paymentMethods = null; // COD (Cash on delivery); CVS (Convenience store payment); Other (Any payment method other than COD or CVS).
		String buyerEmail = null;
		String sellerOrderId = null; // own metasfresh id -> makes other params / filters obsolete.
		Integer maxResultsPerPage = 100; //// default 100

		// not useful for MF.
		List<String> easyShipShipmentStatuses = null; // Easy Ship orders with statuses
		String nextToken = null; // token from previous request.
		List<String> amazonOrderIds = null;// amazon order ids
		String actualFulfillmentSupplySourceId = null; // recommended sourceId where the order should be fulfilled from
		Boolean isISPU = null; // true = pickup
		String storeChainStoreId = null;// store chain store identifier

		GetOrdersResponse response = orderApi.getOrders(marketplaceIds, createdAfter, createdBefore, lastUpdatedAfter, lastUpdatedBefore, orderStatuses, fulfillmentChannels, paymentMethods, buyerEmail, sellerOrderId, maxResultsPerPage, easyShipShipmentStatuses, nextToken, amazonOrderIds, actualFulfillmentSupplySourceId, isISPU, storeChainStoreId);

		if (CollectionUtils.isEmpty(response.getErrors()))
		{

			OrdersList orders = response.getPayload();
			// add orders to exchange
			exchange.getIn().setBody(orders.getOrders());

			// add order context to exchange.
			final AmazonImportOrdersRouteContext ordersContext = AmazonImportOrdersRouteContext.builder()
					.orgCode(request.getOrgCode())
					.orderApi(orderApi)
					.externalSystemRequest(request)
					// .ebayConfigMappings(getEbayOrderMappingRules(request).orElse(null)) TODO
					.build();

			exchange.setProperty(AmazonConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, ordersContext);

		}
		else
		{

			for (Error err : response.getErrors())
			{
				log.error("AmazonApi: " + err.getMessage());
			}

			throw new RuntimeException("Amazon:Failed to get orders! " + Instant.now());

		}

	}

}
