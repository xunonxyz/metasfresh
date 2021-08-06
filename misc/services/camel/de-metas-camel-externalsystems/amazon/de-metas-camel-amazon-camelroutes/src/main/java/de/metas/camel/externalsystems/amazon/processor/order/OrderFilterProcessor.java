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

import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_ORDER_TYPE;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_FBA;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_FBS;
import static de.metas.camel.externalsystems.amazon.ProcessorHelper.getPropertyOrThrowError;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metas.camel.externalsystems.amazon.AmazonImportOrdersRouteContext;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order.FulfillmentChannelEnum;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order.OrderStatusEnum;

/**
 * Filters orders and checks type to trigger correct flow.
 * 
 * @author Werner Gaulke
 *
 */
public class OrderFilterProcessor implements Processor
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		log.info("Filter order by state");

		final AmazonImportOrdersRouteContext importOrdersRouteContext = getPropertyOrThrowError(exchange, ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, AmazonImportOrdersRouteContext.class);

		final Order order = exchange.getIn().getBody(Order.class);
		if (order == null)
		{
			throw new RuntimeException("Empty body!");
		}

		if ((order.getSellerOrderId() == null || "".equalsIgnoreCase(order.getSellerOrderId()))  // use sellerOrder Id to identify new orders.
				&& !OrderStatusEnum.PENDING.equals(order.getOrderStatus()) // Also, orders have to be confirmed (to get all payment details).
				)  // Also, only process seller fulfilled orders.
		{
			
			importOrdersRouteContext.setOrder(order);
			exchange.getIn().setBody(order);
			
			if(FulfillmentChannelEnum.AFN.equals(order.getFulfillmentChannel())) {
				exchange.getIn().setHeader(ROUTE_HEADER_ORDER_TYPE, ROUTE_HEADER_FBA);
			} else {
				exchange.getIn().setHeader(ROUTE_HEADER_ORDER_TYPE, ROUTE_HEADER_FBS);
			}

		}
		else
		{
			// order was filtered
			exchange.getIn().setBody(null);

		}

	}

}
