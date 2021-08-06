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

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_ERROR_ROUTE_ID;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_PUSH_OL_CANDIDATES_ROUTE_ID;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_UPSERT_BPARTNER_V2_CAMEL_URI;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_UPSERT_PRODUCT_PRICE_V2_CAMEL_URI;
import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_UPSERT_PRODUCT_V2_CAMEL_URI;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_ORDER_TYPE;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_FBA;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_HEADER_FBS;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.PredicateBuilder.and;



import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.stereotype.Component;

import de.metas.camel.externalsystems.amazon.processor.bpartner.CreateBPartnerUpsertReqForAmazonOrderProcessor;
import de.metas.camel.externalsystems.amazon.processor.ext.GetAmazonOrderAddressProcessor;
import de.metas.camel.externalsystems.amazon.processor.ext.GetAmazonOrderBuyerProcessor;
import de.metas.camel.externalsystems.amazon.processor.ext.GetAmazonOrderItemsProcessor;
import de.metas.camel.externalsystems.amazon.processor.ext.GetAmazonOrdersProcessor;
import de.metas.camel.externalsystems.amazon.processor.order.CreateOrderLineCandidateUpsertReqForAmazonOrderProcessor;
import de.metas.camel.externalsystems.amazon.processor.order.OrderFilterProcessor;
import de.metas.camel.externalsystems.amazon.processor.product.CreateProductUpsertReqProcessor;
import de.metas.camel.externalsystems.amazon.processor.product.price.CreateProductPriceUpsertReqProcessor;
import de.metas.camel.externalsystems.common.ProcessLogger;
import de.metas.common.bpartner.v2.response.JsonResponseBPartnerCompositeUpsert;

@Component
public class GetAmazonOrdersRouteBuilder extends RouteBuilder
{

	public static final String GET_ORDERS_ROUTE_ID = "Amazon-getOrders";

	public static final String GET_ORDER_DETAILS_ROUTE_ID = "Amazon-getOrderDetails";
	public static final String GET_ORDER_DETAILS_ADDRESS_ID = "Amazon-getOrderDetails-Address";
	public static final String GET_ORDER_DETAILS_BUYER_ID = "Amazon-getOrderDetails-Buyer";
	public static final String GET_ORDER_DETAILS_ITEMS_ID = "Amazon-getOrderDetails-Items";

	public static final String PROCESS_ORDERS_ROUTE_ID = "Amazon-processOrders";
	public static final String PROCESS_ORDER_BPARTNER_ROUTE_ID = "Amazon-processOrderBPartner";
	public static final String PROCESS_PRODUCTS_ROUTE_ID = "Amazon-processProducts";
	public static final String PROCESS_ORDER_PRODUCTS_ROUTE_ID = "Amazon-processOrderProducts";
	public static final String PROCESS_ORDER_PRODUCTS_PRICES_ROUTE_ID = "Amazon-processOrderProductsPrices";
	public static final String PROCESS_ORDER_OLC_ROUTE_ID = "Amazon-processOrderOLC";
	public static final String PROCESS_NEXT_ORDER_IMPORT_RUNTIME_PARAMS_ROUTE_ID = "Amazon-nextOrderImportRuntimeParams";
	public static final String PROCESS_NEXT_ORDER_IMPORT_RUNTIME_PARAMS_PROCESSOR_ID = "Amazon-nextOrderImportRuntimeParamsProcessor";
	public static final String FILTER_ORDER_ROUTE_ID = "Amazon-filterOrder";

	private final ProcessLogger processLogger;

	public GetAmazonOrdersRouteBuilder(final ProcessLogger processLogger)
	{
		this.processLogger = processLogger;
	}

	@Override
	public void configure() throws Exception
	{
		log.debug("Configure amazon order route");

		errorHandler(defaultErrorHandler());
		onException(Exception.class)
				.to(StaticEndpointBuilders.direct(MF_ERROR_ROUTE_ID));

		//@formatter:off
		//1) get orders and split them.
		from( direct(GET_ORDERS_ROUTE_ID) )
			.routeId(GET_ORDERS_ROUTE_ID)
			.log(LoggingLevel.DEBUG, "Amazon get order route invoked")
			.process(new GetAmazonOrdersProcessor(processLogger))
			.split(body())
			.to( direct(GET_ORDER_DETAILS_ROUTE_ID));
		
		//2) get details for orders.
		from( direct(GET_ORDER_DETAILS_ROUTE_ID))
			.routeId(GET_ORDER_DETAILS_ROUTE_ID)
			.log(LoggingLevel.DEBUG, "Amazon get order details route invoked")
			.doTry()
				// filter fulfilled orders  
				.process(new OrderFilterProcessor()).id(FILTER_ORDER_ROUTE_ID)
				.choice()
					.when( and(body().isNotNull(), header(ROUTE_HEADER_ORDER_TYPE).isEqualTo(ROUTE_HEADER_FBA)) )
						.log(LoggingLevel.INFO, "FBA Route not implemendet yet.")
						
					.when( and(body().isNotNull(), header(ROUTE_HEADER_ORDER_TYPE).isEqualTo(ROUTE_HEADER_FBS)) )
						.to( direct(GET_ORDER_DETAILS_ADDRESS_ID) )
						//		.multicast()
						//		.to( direct(GET_ORDER_DETAILS_ADDRESS_ID) )
						//		.to( direct(GET_ORDER_DETAILS_BUYER_ID) )
						//		.to( direct(GET_ORDER_DETAILS_ITEMS_ID) );
					.otherwise()
						.log(LoggingLevel.INFO, "Nothing to do! The order was filtered out!");

//		
//		//3) individual detail getters.
//		from( direct(GET_ORDER_DETAILS_ADDRESS_ID) ).process(new GetAmazonOrderAddressProcessor()).to( direct(PROCESS_ORDERS_ROUTE_ID) );
//		from( direct(GET_ORDER_DETAILS_BUYER_ID) ).process(new GetAmazonOrderBuyerProcessor()).to( direct(PROCESS_ORDERS_ROUTE_ID) );
//		from( direct(GET_ORDER_DETAILS_ITEMS_ID) ).process(new GetAmazonOrderItemsProcessor()).to( direct(PROCESS_ORDERS_ROUTE_ID) );

		// TODO: workaround use simple chain. Parallel multicast from above won't work O_o.
		from( direct(GET_ORDER_DETAILS_ADDRESS_ID) ).process(new GetAmazonOrderAddressProcessor()).to( direct(GET_ORDER_DETAILS_BUYER_ID) );
		from( direct(GET_ORDER_DETAILS_BUYER_ID) ).process(new GetAmazonOrderBuyerProcessor()).to( direct(GET_ORDER_DETAILS_ITEMS_ID) );
		from( direct(GET_ORDER_DETAILS_ITEMS_ID) ).process(new GetAmazonOrderItemsProcessor()).to( direct(PROCESS_ORDERS_ROUTE_ID) );
		
		
		//4) process each order and its details to create upsert requests.
		from( direct(PROCESS_ORDERS_ROUTE_ID) )
			.routeId(PROCESS_ORDERS_ROUTE_ID)
			.log("Amazon process orders route invoked!")
			.doTry()
					
				//i) create products
				//.to( direct(PROCESS_PRODUCTS_ROUTE_ID) ) TODO
				
				//ii) create bparners and put them in bparner import pipeline.
				.process(new CreateBPartnerUpsertReqForAmazonOrderProcessor()).id(PROCESS_ORDER_BPARTNER_ROUTE_ID)
				.log(LoggingLevel.DEBUG, "Calling metasfresh-api to store business partners!")
				.to( "{{" + MF_UPSERT_BPARTNER_V2_CAMEL_URI + "}}" )
			
				.unmarshal(CamelRouteUtil.setupJacksonDataFormatFor(getContext(), JsonResponseBPartnerCompositeUpsert.class))
				
				//iii) create order line candidates.
				.process(new CreateOrderLineCandidateUpsertReqForAmazonOrderProcessor()).id(PROCESS_ORDER_OLC_ROUTE_ID)
				.log(LoggingLevel.DEBUG, "Calling metasfresh-api to store order candidates!")
				.to( direct(MF_PUSH_OL_CANDIDATES_ROUTE_ID) )
						
			.endDoTry()
			
			.doCatch(Exception.class)
				.to( direct(MF_ERROR_ROUTE_ID) )
		
		.end();
		
		//3) process order products ->  i)
		from(direct(PROCESS_PRODUCTS_ROUTE_ID))
				.routeId(PROCESS_PRODUCTS_ROUTE_ID)
		
				.process(new CreateProductUpsertReqProcessor()).id(PROCESS_ORDER_PRODUCTS_ROUTE_ID)
		
				.choice()
					.when(body().isNull())
						.log(LoggingLevel.INFO, "Nothing to do! No new or updated Products!")
					.otherwise()
						.log(LoggingLevel.DEBUG, "Calling metasfresh-api to upsert Products: ${body}")
						.to(direct(MF_UPSERT_PRODUCT_V2_CAMEL_URI))
			
						// and their prices.
						.process(new CreateProductPriceUpsertReqProcessor()).id(PROCESS_ORDER_PRODUCTS_PRICES_ROUTE_ID)
						.split(body())
							.to(direct(MF_UPSERT_PRODUCT_PRICE_V2_CAMEL_URI))
				.endChoice()
		.end();
//		
//		//4) upsert params for future calls.
//		from(direct(PROCESS_NEXT_ORDER_IMPORT_RUNTIME_PARAMS_ROUTE_ID))
//				.routeId(PROCESS_NEXT_ORDER_IMPORT_RUNTIME_PARAMS_ROUTE_ID)
//				.log("Route invoked")
//		
//				.process(new NextOrderImportRuntimeParameterUpsert()).id(PROCESS_NEXT_ORDER_IMPORT_RUNTIME_PARAMS_PROCESSOR_ID)
//				.choice()
//					.when(body().isNull())
//						.log(LoggingLevel.DEBUG, "Calling metasfresh-api to upsert runtime parameters: ${body}")
//					.otherwise()
//						.to(direct(MF_UPSERT_RUNTIME_PARAMETERS_ROUTE_ID))
//				.endChoice()
//		.end();
		//@formatter:on

	}

}
