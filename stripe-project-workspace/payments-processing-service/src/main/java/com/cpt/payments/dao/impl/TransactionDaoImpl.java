package com.cpt.payments.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.cpt.payments.dao.TransactionDao;
import com.cpt.payments.dto.Transaction;
import com.cpt.payments.utils.LogMessage;

@Repository
public class TransactionDaoImpl implements TransactionDao {
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	

	
	@Override
	public Transaction createTransaction(Transaction transaction) {
		
		String sql = "INSERT INTO `Transaction` (userId, paymentMethodId, providerId, paymentTypeId, amount, currency, txnStatusId, merchantTransactionReference, txnReference, txnDetailsId, providerCode, providerMessage, debitorAccount, creditorAccount, providerReference, retryCount) " +
	            "VALUES (:userId, :paymentMethodId, :providerId, :paymentTypeId, :amount, :currency, :txnStatusId, :merchantTransactionReference, :txnReference, :txnDetailsId, :providerCode, :providerMessage, :debitorAccount, :creditorAccount, :providerReference, :retryCount)";

		
		MapSqlParameterSource params = new MapSqlParameterSource()
		.addValue("userId", transaction.getUserId())
		.addValue("paymentMethodId", transaction.getPaymentMethodId())
		.addValue("providerId", transaction.getProviderId())
		.addValue("paymentTypeId", transaction.getPaymentTypeId())
		.addValue("amount", transaction.getAmount())
		.addValue("currency", transaction.getCurrency())
		.addValue("txnStatusId", transaction.getTxnStatusId())
		.addValue("merchantTransactionReference", transaction.getMerchantTransactionReference())
		.addValue("txnReference", transaction.getTxnReference())
		.addValue("txnDetailsId", transaction.getTxnDetailsId())
		.addValue("providerCode", transaction.getProviderCode())
		.addValue("providerMessage", transaction.getProviderMessage())
		.addValue("debitorAccount", transaction.getDebitorAccount())
		.addValue("creditorAccount", transaction.getCreditorAccount())
		.addValue("providerReference", transaction.getProviderReference())
		.addValue("retryCount", transaction.getRetryCount());
		
		try {
			//keyHolder holds auto-generated id
			KeyHolder keyHolder=new GeneratedKeyHolder();
		int rowUpdated=jdbcTemplate.update(sql,params,keyHolder);
		
		transaction.setId(keyHolder.getKey().intValue());
		System.out.println("Successfully inserted data | row updated: "+rowUpdated);
		}
		catch(Exception e) {
			System.out.println("Exception in creatingTransaction:  "+e);
			return null;
		}
		
		
		return transaction;
	}
	
	
	@Override
	public boolean updateTransaction(Transaction transaction) {
	    try {
	        jdbcTemplate.update(updateTransactionQuery(), new BeanPropertySqlParameterSource(transaction));
	        return true;
	    } catch (Exception e) {
	        System.out.println("Exception while updating TRANSACTION in DB: " + e.getMessage());
	        return false; // Or handle the exception as needed
	    }
	}

	private String updateTransactionQuery() {
	    StringBuilder queryBuilder = new StringBuilder("UPDATE `Transaction` ");
	    queryBuilder.append("SET txnStatusId=:txnStatusId, txnDetailsId=:txnDetailsId, providerCode=:providerCode, providerMessage=:providerMessage ");
	    queryBuilder.append("WHERE id=:id");
	    System.out.println("Update Transaction query: " + queryBuilder);
	    return queryBuilder.toString();
	}
	
	
	@Override
	public Transaction getTransactionById(long transactionId) {
		System.out.println(" :: fetching Transaction for :: " + transactionId);

		Transaction Transaction = null;
		try {
			Transaction = jdbcTemplate.queryForObject(getTransactionById(),
					new BeanPropertySqlParameterSource(Transaction.builder().id((int) transactionId).build()),
					new BeanPropertyRowMapper<>(Transaction.class));
			System.out.println( " :: transaction Details from DB  = " + Transaction);
		} catch (Exception e) {
			System.out.println("unable to get transaction Details " + e);
			
		}
		return Transaction;
	}

	private String getTransactionById() {
		StringBuilder queryBuilder = new StringBuilder("SELECT * FROM  Transaction  WHERE id = :id ");
		System.out.println( "getTransactionById -> " + queryBuilder);
		return queryBuilder.toString();
	}
	
	@Override
	public void updateProviderReference(Transaction transaction) {
		try {
			jdbcTemplate.update(updateProviderReference(), new BeanPropertySqlParameterSource(transaction));
		} catch (Exception e) {
			System.out.println( "exception while updating TRANSACTION in DB :: " + transaction);
			
		}

	}
	
	private String updateProviderReference() {
		StringBuilder queryBuilder = new StringBuilder("Update Transaction ");
		queryBuilder.append("SET providerReference=:providerReference ");
		queryBuilder.append("WHERE id=:id ");
		System.out.println( "updateProviderReference query -> " + queryBuilder);
		return queryBuilder.toString();
	}


	@Override
	public Transaction getTransactionByProviderReference(String paymentId) {
		System.out.println( " :: fetching Transaction Details  for provider reference :: " + paymentId);

		Transaction transaction = null;
		try {
			transaction = jdbcTemplate.queryForObject(getTransactionByProviderReference(),
					new BeanPropertySqlParameterSource(Transaction.builder().providerReference(paymentId).build()),
					new BeanPropertyRowMapper<>(Transaction.class));
			System.out.println( " :: transaction Details from DB  = " + transaction);
		} catch (Exception e) {
			System.out.println( "unable to get transaction Details " + e);
		
		}
		return transaction;
	}

	private String getTransactionByProviderReference() {
		StringBuilder queryBuilder = new StringBuilder(
				"Select * from Transaction where providerReference=:providerReference ");
		System.out.println( "getTransactionByProviderReference query -> " + queryBuilder);
		return queryBuilder.toString();
	}


}