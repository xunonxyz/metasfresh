/*
 * #%L
 * de.metas.banking.camt53
 * %%
 * Copyright (C) 2023 metas GmbH
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

//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.7 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.06.07 at 02:56:38 PM EEST 
//


package de.metas.banking.camt53.jaxb.camt053_001_04;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PaymentCard4 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PaymentCard4"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PlainCardData" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.04}PlainCardData1" minOccurs="0"/&gt;
 *         &lt;element name="CardCtryCd" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.04}Exact3NumericText" minOccurs="0"/&gt;
 *         &lt;element name="CardBrnd" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.04}GenericIdentification1" minOccurs="0"/&gt;
 *         &lt;element name="AddtlCardData" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.04}Max70Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentCard4", propOrder = {
    "plainCardData",
    "cardCtryCd",
    "cardBrnd",
    "addtlCardData"
})
public class PaymentCard4 {

    @XmlElement(name = "PlainCardData")
    protected PlainCardData1 plainCardData;
    @XmlElement(name = "CardCtryCd")
    protected String cardCtryCd;
    @XmlElement(name = "CardBrnd")
    protected GenericIdentification1 cardBrnd;
    @XmlElement(name = "AddtlCardData")
    protected String addtlCardData;

    /**
     * Gets the value of the plainCardData property.
     * 
     * @return
     *     possible object is
     *     {@link PlainCardData1 }
     *     
     */
    public PlainCardData1 getPlainCardData() {
        return plainCardData;
    }

    /**
     * Sets the value of the plainCardData property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlainCardData1 }
     *     
     */
    public void setPlainCardData(PlainCardData1 value) {
        this.plainCardData = value;
    }

    /**
     * Gets the value of the cardCtryCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardCtryCd() {
        return cardCtryCd;
    }

    /**
     * Sets the value of the cardCtryCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardCtryCd(String value) {
        this.cardCtryCd = value;
    }

    /**
     * Gets the value of the cardBrnd property.
     * 
     * @return
     *     possible object is
     *     {@link GenericIdentification1 }
     *     
     */
    public GenericIdentification1 getCardBrnd() {
        return cardBrnd;
    }

    /**
     * Sets the value of the cardBrnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericIdentification1 }
     *     
     */
    public void setCardBrnd(GenericIdentification1 value) {
        this.cardBrnd = value;
    }

    /**
     * Gets the value of the addtlCardData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddtlCardData() {
        return addtlCardData;
    }

    /**
     * Sets the value of the addtlCardData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddtlCardData(String value) {
        this.addtlCardData = value;
    }

}
