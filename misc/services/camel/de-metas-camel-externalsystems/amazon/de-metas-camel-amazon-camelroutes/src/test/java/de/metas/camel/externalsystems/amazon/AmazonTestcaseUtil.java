/*
 * #%L
 * de-metas-camel-ebay-camelroutes
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderAddressResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderBuyerInfoResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderItemsResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrdersResponse;

/**
 * This is a small util class to load the latest orders-api-model json, which contains example results for api calls.
 * 
 * Results are then processed and parsed to check, if the model changed. Also local example-json can be updated
 * which is used in other unit tests.
 * 
 * @see https://raw.githubusercontent.com/amzn/selling-partner-api-models/main/models/orders-api-model/ordersV0.json
 * 
 * @author Werner Gaulke
 *
 */
public class AmazonTestcaseUtil
{

	/**
	 * Github repo of orders api spec.
	 */
	private static final String AMAZON_ORDER_VO_API_MODEL = "https://raw.githubusercontent.com/amzn/selling-partner-api-models/main/models/orders-api-model/ordersV0.json";

	@Test
	public void exampleJsonToApiModelParsingTest() throws IOException
	{
		Gson gson = new Gson();

		String model = readStringFromURL(AMAZON_ORDER_VO_API_MODEL);

		Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider()).build(); /// .options(Option.AS_PATH_LIST)
		ReadContext document = JsonPath.using(conf).parse(model);

		// get order example
		Object orders = document.read("$.paths['/orders/v0/orders'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		GetOrdersResponse or = gson.fromJson(orders.toString(), GetOrdersResponse.class);
		Assertions.assertNotNull(or.getPayload(), "order example response parseable");
		Assertions.assertNotNull(or.getPayload().getOrders(), "order example response parseable");

		// buyerInfo
		Object buyerInfo = document.read("$.paths['/orders/v0/orders/{orderId}/buyerInfo'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		GetOrderBuyerInfoResponse obir = gson.fromJson(buyerInfo.toString(), GetOrderBuyerInfoResponse.class);
		Assertions.assertNotNull(obir.getPayload(), "order example buyer info response parseable");
		Assertions.assertNotNull(obir.getPayload().getAmazonOrderId(), "order example buyer info response parseable");

		// addressInfo
		Object orderAddress = document.read("$.paths['/orders/v0/orders/{orderId}/address'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		GetOrderAddressResponse oar = gson.fromJson(orderAddress.toString(), GetOrderAddressResponse.class);
		Assertions.assertNotNull(oar.getPayload(), "order example address info response parseable");
		Assertions.assertNotNull(oar.getPayload().getAmazonOrderId(), "order example address info response parseable");

		// items
		Object orderItems = document.read("$.paths['/orders/v0/orders/{orderId}/orderItems'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		GetOrderItemsResponse oir = gson.fromJson(orderItems.toString(), GetOrderItemsResponse.class);
		Assertions.assertNotNull(oir.getPayload(), "order example items info response parseable");
		Assertions.assertNotNull(oir.getPayload().getAmazonOrderId(), "order example items info response parseable");

	}

	@Disabled("Only refresh test json, if necessary.")
	public void updateLocalJsonWithLatestRepoTest() throws IOException
	{

		String model = readStringFromURL(AMAZON_ORDER_VO_API_MODEL);

		Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider()).build(); /// .options(Option.AS_PATH_LIST)
		ReadContext document = JsonPath.using(conf).parse(model);

		// get order example
		Object orders = document.read("$.paths['/orders/v0/orders'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		writeJsonToTestFile(orders, "01_amazon-orders-example.json");

		// buyerInfo
		Object buyerInfo = document.read("$.paths['/orders/v0/orders/{orderId}/buyerInfo'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		writeJsonToTestFile(buyerInfo, "02_amazon-order-buyer-info-example.json");

		// addressInfo
		Object orderAddress = document.read("$.paths['/orders/v0/orders/{orderId}/address'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		writeJsonToTestFile(orderAddress, "02_amazon-order-address-example.json");

		// items
		Object orderItems = document.read("$.paths['/orders/v0/orders/{orderId}/orderItems'].get.responses['200']['x-amazon-spds-sandbox-behaviors'][0].response");
		writeJsonToTestFile(orderItems, "02_amazon-order-items-example.json");
	}

	private static void writeJsonToTestFile(Object json, String fileName) throws FileNotFoundException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement je = JsonParser.parseString(json.toString());
		String prettyJsonString = gson.toJson(je);

		var ordersExamplePath = Paths.get("src", "test", "resources", "examples", fileName);
		try (PrintWriter out = new PrintWriter(new File(ordersExamplePath.toFile().getAbsolutePath())))
		{
			out.println(prettyJsonString);
		}
	}

	private static String readStringFromURL(String url) throws IOException
	{
		try (Scanner scanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8.toString()))
		{
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

}
