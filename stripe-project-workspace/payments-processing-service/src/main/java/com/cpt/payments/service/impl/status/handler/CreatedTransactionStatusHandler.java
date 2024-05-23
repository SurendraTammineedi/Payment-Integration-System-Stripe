package com.cpt.payments.service.impl.status.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dao.TransactionLogDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.dto.TransactionLog;
import com.cpt.payments.service.TransactionStatusHandler;

@Service
public class CreatedTransactionStatusHandler extends TransactionStatusHandler {


	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	TransactionLogDao transactionLogDao;


	@Override
	public boolean updateStatus(Transaction transaction) {

	System.out.println("CreatedTransactionStatusHandler.updateStatus updated ");
	
		transaction.setTxnDetailsId(TransactionStatusEnum.CREATED.getId());
		Transaction txnResponse=transactionDao.createTransaction(transaction);
		
		if(txnResponse==null) {
			System.out.println("Failed to save Transaction");
			return false;
		}
		TransactionLog txnLog = TransactionLog.builder().transactionId(txnResponse.getId()).txnFromStatus("-")
				.txnToStatus(TransactionStatusEnum.CREATED.getName()).build();
		transactionLogDao.createTransactionLog(txnLog);
		
			System.out.println("Success in saving Transaction");
			return true;
		


	}

}
