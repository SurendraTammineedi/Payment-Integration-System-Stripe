package com.cpt.payments.dao;

import com.cpt.payments.dto.Transaction;

public interface TransactionDao {
	public Transaction createTransaction(Transaction transaction);
	
	public boolean updateTransaction(Transaction transaction);
	
	public Transaction getTransactionById(long transactionId);
	
	public Transaction getTransactionByProviderReference(String paymentId);
	
	public void updateProviderReference(Transaction transaction);

	
}
