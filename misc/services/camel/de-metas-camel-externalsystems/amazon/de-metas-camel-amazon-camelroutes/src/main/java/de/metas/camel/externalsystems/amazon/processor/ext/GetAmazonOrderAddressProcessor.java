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

import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT;
import static de.metas.camel.externalsystems.amazon.ProcessorHelper.getPropertyOrThrowError;

import java.time.Instant;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import de.metas.camel.externalsystems.amazon.AmazonImportOrdersRouteContext;
import de.metas.camel.externalsystems.amazon.api.OrdersV0Api;
import de.metas.camel.externalsystems.amazon.api.model.orders.Error;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderAddressResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderAddress;

public class GetAmazonOrderAddressProcessor implements Processor
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		final AmazonImportOrdersRouteContext importOrderRouteContext = getPropertyOrThrowError(exchange, ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, AmazonImportOrdersRouteContext.class);
		log.debug("Get order address for amazon order {}", importOrderRouteContext.getOrderNotNull().getAmazonOrderId());

		// to process
		final Order order = importOrderRouteContext.getOrderNotNull();
		final OrdersV0Api orderApi = importOrderRouteContext.getOrderApi();

		
		GetOrderAddressResponse response = orderApi.getOrderAddress(order.getAmazonOrderId());


		if (CollectionUtils.isEmpty(response.getErrors()))
		{
			OrderAddress address = response.getPayload();
			importOrderRouteContext.setOrderAddress(address);
		}
		else
		{
			for (Error err : response.getErrors())
			{
				log.error("AmazonApi: " + err.getMessage());
			}

			throw new RuntimeException("Amazon:Failed to get order order address! " + Instant.now());
		}

	}

}
