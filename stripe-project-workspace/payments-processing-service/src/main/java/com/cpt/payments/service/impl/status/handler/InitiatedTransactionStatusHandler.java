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
public class InitiatedTransactionStatusHandler extends TransactionStatusHandler{

	@Autowired
	private TransactionDao transactionDao;
	
	@Autowired
	TransactionLogDao transactionLogDao;
	
	@Override
	public boolean updateStatus(Transaction transaction) {
		
		System.out.println("transaction INITIATED 	"+transaction);
		
		String fromStatus=TransactionStatusEnum.getTransactionStatusEnum(
				transactionDao.getTransactionById(transaction.getId()).getTxnStatusId()).getName();
		
		transaction.setTxnDetailsId(TransactionStatusEnum.INITIATED.getId());
		transaction.setTxnStatusId(TransactionStatusEnum.INITIATED.getId());

		System.out.println("TRANSACTION STATUS ID: "+transaction.getTxnStatusId());
		
		boolean transactionStatus=transactionDao.updateTransaction(transaction);
	
		if(!transactionStatus) {
			System.out.println(" updating transaction failed-> "+transaction);
			return false;
		}
		
		TransactionLog transactionLog = TransactionLog.builder().transactionId(transaction.getId())
				.txnFromStatus(fromStatus)
				.txnToStatus(TransactionStatusEnum.INITIATED.getName()).build();
		transactionLogDao.createTransactionLog(transactionLog);
		
		return true;
	}

}
