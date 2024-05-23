package com.cpt.payments.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cpt.payments.constants.Endpoints;
import com.cpt.payments.dto.ProcessPayment;
import com.cpt.payments.dto.ProcessPaymentResponse;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.pojo.PaymentResponse;
import com.cpt.payments.pojo.ProcessingServiceRequest;
import com.cpt.payments.pojo.TransactionReqRes;
import com.cpt.payments.service.PaymentService;
import com.cpt.payments.service.PaymentStatusService;
import com.cpt.payments.utils.LogMessage;
import com.cpt.payments.utils.TransactionMapper;

@RestController
@RequestMapping(Endpoints.PAYMENTS)
public class PaymentController {
	
	private static final Logger LOGGER = LogManager.getLogger(PaymentController.class);

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	TransactionMapper transactionMapper;
	
	@Autowired
	PaymentStatusService paymentStatusService;
	
	@Autowired
	PaymentService paymentService;

	@PostMapping(value = Endpoints.STATUS_UPDATE)
	public ResponseEntity<TransactionReqRes> processPaymentStatus(@RequestBody TransactionReqRes transactionReqRes) {

		System.out.println(" payment request is -> " + transactionReqRes);

		//converting into DTO Transaction 
		Transaction transaction = transactionMapper.toDTO(transactionReqRes);
		System.out.println("converted into tnxDTO: " + transaction);

		Transaction response = paymentStatusService.updatePaymentStatus(transaction);
		TransactionReqRes responseObject =
				transactionMapper.toResponseObject(response);

		return ResponseEntity.ok(responseObject);
	}

	@PostMapping(value = Endpoints.PROCESS_PAYMENT)
	public ResponseEntity<PaymentResponse> processPayment(
			@RequestBody ProcessingServiceRequest  processingServiceRequest) {
		LogMessage.setLogMessagePrefix("/PROCESS_PAYMENT");
		LogMessage.log(LOGGER, " processingServiceRequest is -> " + processingServiceRequest);

			
		ProcessPayment processPaymentDTO=modelMapper.map(processingServiceRequest, ProcessPayment.class);
		
		ProcessPaymentResponse serviceResponse=paymentService.processPayment(processPaymentDTO);

		PaymentResponse response=modelMapper.map(serviceResponse, PaymentResponse.class);
		
		LogMessage.log(LOGGER, " processPayment response is -> " + response);
		return ResponseEntity.ok(response);
	}
}