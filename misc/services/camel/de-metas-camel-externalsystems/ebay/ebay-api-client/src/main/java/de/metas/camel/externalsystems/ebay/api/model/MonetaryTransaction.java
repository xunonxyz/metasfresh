/*
 * Fulfillment API
 * Use the Fulfillment API to complete the process of packaging, addressing, handling, and shipping each order on behalf of the seller, in accordance with the payment method and timing specified at checkout.
 *
 * The version of the OpenAPI document: v1.19.3
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package de.metas.camel.externalsystems.ebay.api.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.metas.camel.externalsystems.ebay.api.model.DisputeAmount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * This type is used to provide details about one or more monetary transactions that occur as part of a payment dispute.
 */
@ApiModel(description = "This type is used to provide details about one or more monetary transactions that occur as part of a payment dispute.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-03-15T16:42:26.057642+01:00[Europe/Berlin]")
public class MonetaryTransaction {
  public static final String SERIALIZED_NAME_DATE = "date";
  @SerializedName(SERIALIZED_NAME_DATE)
  private String date;

  public static final String SERIALIZED_NAME_TYPE = "type";
  @SerializedName(SERIALIZED_NAME_TYPE)
  private String type;

  public static final String SERIALIZED_NAME_REASON = "reason";
  @SerializedName(SERIALIZED_NAME_REASON)
  private String reason;

  public static final String SERIALIZED_NAME_AMOUNT = "amount";
  @SerializedName(SERIALIZED_NAME_AMOUNT)
  private DisputeAmount amount;


  public MonetaryTransaction date(String date) {
    
    this.date = date;
    return this;
  }

   /**
   * This timestamp indicates when the monetary transaction occurred. A date is returned for all monetary transactions. The following format is used: YYYY-MM-DDTHH:MM:SS.SSSZ. For example, 2015-08-04T19:09:02.768Z.
   * @return date
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This timestamp indicates when the monetary transaction occurred. A date is returned for all monetary transactions. The following format is used: YYYY-MM-DDTHH:MM:SS.SSSZ. For example, 2015-08-04T19:09:02.768Z.")

  public String getDate() {
    return date;
  }


  public void setDate(String date) {
    this.date = date;
  }


  public MonetaryTransaction type(String type) {
    
    this.type = type;
    return this;
  }

   /**
   * This enumeration value indicates whether the monetary transaction is a charge or a credit to the seller. For implementation help, refer to &lt;a href&#x3D;&#39;https://developer.ebay.com/api-docs/sell/fulfillment/types/api:MonetaryTransactionTypeEnum&#39;&gt;eBay API documentation&lt;/a&gt;
   * @return type
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This enumeration value indicates whether the monetary transaction is a charge or a credit to the seller. For implementation help, refer to <a href='https://developer.ebay.com/api-docs/sell/fulfillment/types/api:MonetaryTransactionTypeEnum'>eBay API documentation</a>")

  public String getType() {
    return type;
  }


  public void setType(String type) {
    this.type = type;
  }


  public MonetaryTransaction reason(String reason) {
    
    this.reason = reason;
    return this;
  }

   /**
   * This enumeration value indicates the reason for the monetary transaction. For implementation help, refer to &lt;a href&#x3D;&#39;https://developer.ebay.com/api-docs/sell/fulfillment/types/api:MonetaryTransactionReasonEnum&#39;&gt;eBay API documentation&lt;/a&gt;
   * @return reason
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This enumeration value indicates the reason for the monetary transaction. For implementation help, refer to <a href='https://developer.ebay.com/api-docs/sell/fulfillment/types/api:MonetaryTransactionReasonEnum'>eBay API documentation</a>")

  public String getReason() {
    return reason;
  }


  public void setReason(String reason) {
    this.reason = reason;
  }


  public MonetaryTransaction amount(DisputeAmount amount) {
    
    this.amount = amount;
    return this;
  }

   /**
   * Get amount
   * @return amount
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public DisputeAmount getAmount() {
    return amount;
  }


  public void setAmount(DisputeAmount amount) {
    this.amount = amount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonetaryTransaction monetaryTransaction = (MonetaryTransaction) o;
    return Objects.equals(this.date, monetaryTransaction.date) &&
        Objects.equals(this.type, monetaryTransaction.type) &&
        Objects.equals(this.reason, monetaryTransaction.reason) &&
        Objects.equals(this.amount, monetaryTransaction.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, type, reason, amount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetaryTransaction {\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
