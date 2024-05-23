Payment Integration project for Ecommerce Client :
-------------------------------------------------
Developed this Microservices architecture application as 2 Separate Microservices:
	1. payment-processing-service
	2. stripe-provider-service

Core Payment Integration:
     3rd party Integration (Payment Service Providers (PSPs) as STRIPE/TRUSTLY).

These 2 systems can talk to one another using APIs.

This design of Payment System, which can support multiple Payment Service Providers (PSPs) which are modular/reusable.
This will ensure Ease of developing, debugging and maintaining.

Based on Request Structure and correct API URL, to analyze success flow.

1. Payment system will call create-session Stripe API 
	stripe-provider

2. In response we get response where the customer would be redirected URL for completing the payment to a hosted payment page.

We are able to save the payment status of transactions to database (CREATED, INITIATED, PENDING, FAILED, APPROVED) through appropriate Status Handlers.



TECHNOLOGIES:
JAVA,SPRING STS,MYSQL,POSTMAN CLIENT,JUNIT +MOCKITO TESTING

Stripe Docs URL:https://docs.stripe.com/api/checkout/sessions
