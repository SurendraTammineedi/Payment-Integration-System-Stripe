package com.cpt.payments.service.impl.provider.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.http.HttpRequest;
import com.cpt.payments.http.HttpRestTemplateEngine;
import com.cpt.payments.provider.ProviderServiceErrorResponse;
import com.cpt.payments.provider.stripe.StripeProviderRequest;
import com.cpt.payments.provider.stripe.StripeProviderResponse;
import com.cpt.payments.service.PaymentStatusService;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.service.impl.PaymentServiceImpl;
import com.cpt.payments.utils.LogMessage;
import com.google.gson.Gson;


@Service
public class StripeProviderHandler implements ProviderHandler {
	private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
	
	@Value("${stripe.provider.service.process.payment}")
	private String processPaymentUrl;
	
	@Autowired
	private HttpRestTemplateEngine httpRestTemplateEngine;
	
	@Autowired
	private PaymentStatusService paymentStatusService;
	
	@Autowired
	private TransactionDao transactionDao;
	
	@Override
	public ProcessPaymentResponse processPayment(Transaction transaction, ProcessPayment processingServiceRequest) {
		
		LogMessage.debug(LOGGER, " Invoking stripe provider Handler service : " + transaction+"| ProcessPayment : "+processingServiceRequest);
		
		StripeProviderRequest stripeProviderRequest = StripeProviderRequest.builder()
				.transactionReference(transaction.getTxnReference())
				.currency(transaction.getCurrency())
				.amount(transaction.getAmount())
				.quantity(processingServiceRequest.getQuantity())
				.productDescription(processingServiceRequest.getProductDescription())
				.successUrl(processingServiceRequest.getSuccessUrl())
				.cancelUrl(processingServiceRequest.getCancelUrl())
				.build();
		
		LogMessage.log(LOGGER, " stripe provider request -> " + stripeProviderRequest);
		  
		//before calling stripe provider service update payment status to INITIATED
		transaction.setTxnStatusId(TransactionStatusEnum.INITIATED.getId());
		transaction = paymentStatusService.updatePaymentStatus(transaction);
		
		Gson gson=new Gson();

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.request(gson.toJson(stripeProviderRequest))
				.url(processPaymentUrl)
				.build();
		
		ResponseEntity<String> response = httpRestTemplateEngine.execute(httpRequest);
		LogMessage.log(LOGGER, " got API response from stripe provider | response:  -> " + response);
		
		boolean isRedirectUrlNull=false;
			
		if(response!=null
				&&response.getBody()!=null
				&&response.getStatusCode().value()==HttpStatus.OK.value()) {
			
			StripeProviderResponse stripeProviderResponse = gson.fromJson(response.getBody(),
					StripeProviderResponse.class);
						
			if(stripeProviderResponse.getRedirectUrl()!=null) {//success
				transaction.setTxnStatusId(TransactionStatusEnum.PENDING.getId());
				transaction=paymentStatusService.updatePaymentStatus(transaction);
				transaction.setProviderReference(stripeProviderResponse.getPaymentId());	
				
				transactionDao.updateProviderReference(transaction);
				
				return ProcessPaymentResponse.builder()
						 .paymentReference(transaction.getTxnReference())
						 .redirectUrl(stripeProviderResponse.getRedirectUrl())
						 .build();
		}
		else {
			LogMessage.log(LOGGER, "received null redirectUrl | stripeProviderResponse: ");
			isRedirectUrlNull=true;
		}
	}
	
	throw processFailedResponse(transaction,gson,response,isRedirectUrlNull);
	
	}	
		
private PaymentProcessingException processFailedResponse(Transaction transaction,Gson gson,ResponseEntity<String> response,
		boolean isRedirectUrlNull) {
	if(null==response || null==response.getBody()) {//failed to make API call
		System.out.println("Payment failed-payment creation at provider failed->" +response);
	
	
	updateFailedPayment(transaction,
			ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
			ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
	
	return new PaymentProcessingException(
			HttpStatus.INTERNAL_SERVER_ERROR,
			ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorCode(),
			ErrorCodeEnum.FAILED_TO_CREATE_TRANSACTION.getErrorMessage());
	}

 if(isRedirectUrlNull) {
	 LogMessage.log(LOGGER, "Payment failed-No Redirect URL from stripe ");
	 
	 updateFailedPayment(transaction,
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
	 
	 return new PaymentProcessingException(
			 HttpStatus.BAD_GATEWAY,
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
 }
	
 //Error Reponse
 ProviderServiceErrorResponse errorResponse=gson.fromJson(response.getBody(), ProviderServiceErrorResponse.class);
 
 if(errorResponse.isTpProviderError()) {
	 LogMessage.log(LOGGER, "Payment failed -is stripe Error ->" +response);
	 
	 updateFailedPayment(transaction,
			 errorResponse.getErrorCode(),
			 errorResponse.getErrorMessage());
	 //3rd party error needs to be mapped with our system error
	 return new PaymentProcessingException(
			 HttpStatus.BAD_GATEWAY,
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
 }
 else {
 LogMessage.log(LOGGER, "INTERNAL Payment creation at provider Error ->" +response);
	 
	 updateFailedPayment(transaction,
			 errorResponse.getErrorCode(),
			 errorResponse.getErrorMessage());
	 //3rd party error needs to be mapped with our system error
	 return new PaymentProcessingException(
			 HttpStatus.BAD_GATEWAY,
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorCode(),
			 ErrorCodeEnum.TP_STRIPE_ERROR.getErrorMessage());
 
 }
}
private void updateFailedPayment(
		Transaction transaction,
		String erroCode,String erroMessage) {
	transaction.setTxnStatusId(TransactionStatusEnum.FAILED.getId());
	
	transaction.setProviderCode(erroCode);
	transaction.setProviderMessage(erroMessage);
	
	paymentStatusService.updatePaymentStatus(transaction);
}




}




