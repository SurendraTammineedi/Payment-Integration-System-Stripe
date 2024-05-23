package com.cpt.payments.provider.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StripeProviderRequest {
	private String transactionReference;
	private String currency;
	private double amount;
	private Long quantity;
	private String productDescription;
	private String successUrl;
	private String cancelUrl;

}
