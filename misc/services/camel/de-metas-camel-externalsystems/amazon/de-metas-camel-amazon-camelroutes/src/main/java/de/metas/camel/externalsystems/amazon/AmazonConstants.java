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

package de.metas.camel.externalsystems.amazon;

public interface AmazonConstants
{
	

	// camel route properties
	String ROUTE_PROPERTY_AMAZON_CLIENT = "amazonClient";
	String ROUTE_PROPERTY_CURRENT_ORDER = "currentOrder";
	String ROUTE_PROPERTY_ORDER_DELIVERIES = "orderDeliveries";
	String ROUTE_PROPERTY_ORG_CODE = "orgCode";

	/**
	 * Various props used for order processing.
	 * {@link AmazonImportOrdersRouteContext}
	 */
	String ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT = "amazon_order_context";

}
