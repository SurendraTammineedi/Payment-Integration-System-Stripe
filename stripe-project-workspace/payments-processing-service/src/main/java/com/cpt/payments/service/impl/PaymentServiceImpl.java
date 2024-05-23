package com.cpt.payments.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.exception.PaymentProcessingException;
import com.cpt.payments.service.PaymentService;
import com.cpt.payments.service.ProviderHandler;
import com.cpt.payments.service.factory.ProviderHandlerFactory;
import com.cpt.payments.utils.LogMessage;

@Service
public class PaymentServiceImpl implements PaymentService {
	
	private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
	
	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	private ProviderHandlerFactory providerHandlerFactory;
	
	@Override
	public ProcessPaymentResponse processPayment(ProcessPayment processPayment) {
		LogMessage.log(LOGGER, " running processPayment at service layer with processPayment : " + processPayment);
		
		Transaction txn=transactionDao.getTransactionById(processPayment.getTransactionId());
		
		if(txn==null) {
			LogMessage.log(LOGGER, "transaction not found txn: -> " + txn + "txnId: "+ processPayment.getTransactionId());
			throw new PaymentProcessingException(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorCode(),
					ErrorCodeEnum.PAYMENT_NOT_FOUND.getErrorMessage());
		}
		ProviderHandler providerHandler=providerHandlerFactory.getProviderHandler(txn.getProviderId());
		if(providerHandler==null) {
			LogMessage.log(LOGGER, "provider not found -> " + txn.getProviderId());

			throw new PaymentProcessingException(HttpStatus.BAD_REQUEST, ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorCode(),
					ErrorCodeEnum.PROVIDER_NOT_FOUND.getErrorMessage());
		}
		LogMessage.debug(LOGGER, "invoking provider handler for payment processing provider handler:  -> " + providerHandler);
		ProcessPaymentResponse serviceResponse=providerHandler.processPayment(txn, processPayment);		
		
		LogMessage.log(LOGGER, "got response from provider Handler serviceResponse:  "+serviceResponse);
		return serviceResponse;
	}

}
