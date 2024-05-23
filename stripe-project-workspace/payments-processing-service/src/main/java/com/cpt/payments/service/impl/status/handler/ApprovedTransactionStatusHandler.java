package com.cpt.payments.service.impl.status.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cpt.payments.constants.TransactionStatusEnum;
import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dao.TransactionLogDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.dto.TransactionLog;
import com.cpt.payments.service.TransactionStatusHandler;
import com.cpt.payments.utils.LogMessage;

@Component
public class ApprovedTransactionStatusHandler extends TransactionStatusHandler {

	private static final Logger LOGGER = LogManager.getLogger(ApprovedTransactionStatusHandler.class);



	@Autowired
	private TransactionDao transactionDao;

	@Autowired
	private TransactionLogDao transactionLogDao;

	@Override
	public boolean updateStatus(Transaction transaction) {
		LogMessage.log(LOGGER, " transaction approved -> " + transaction);

		transaction.setTxnDetailsId(TransactionStatusEnum.APPROVED.getId());
		transaction.setTxnStatusId(TransactionStatusEnum.APPROVED.getId());
		boolean transactionStatus = transactionDao.updateTransaction(transaction);
		if (!transactionStatus) {
			LogMessage.log(LOGGER, " updating transaction failed -> " + transaction);
			return false;
		}
		TransactionLog transactionLog = TransactionLog.builder().transactionId(transaction.getId())
				.txnFromStatus(TransactionStatusEnum.PENDING.getName())
				.txnToStatus(TransactionStatusEnum.APPROVED.getName()).build();
		transactionLogDao.createTransactionLog(transactionLog);
		return true;
	}

}
