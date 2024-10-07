# XpressPayment

XpressPayment is a Spring Boot application designed to register and authenticate users using JWT and handle payment transactions especially Airtime purchase (The scope of this project) via the third-party API (Xpresspayment). This project aims to simplify the process of making airtime purchases while ensuring user security.

## Features

- **User Authentication**: User registration and login through the authentication endpoint.
- **Payment Processing**: Seamless processing of payment transactions, specifically for airtime purchases.

## Technologies Used

- **Java**: Programming language used for development.
- **Spring Boot**: Framework for building the application.
- **RestTemplate**: For making HTTP requests to the third-party API.
- **PostgreSQL**: Database for storing user and transaction information.
- **JUnit**: Framework for unit testing.
- **MockMvc**: For testing Spring MVC controllers.
- **Mockito**: For mocking objects in tests.

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed on your machine:

- **Java 8+**
- **Maven**
- **PostgreSQL**

### Configuration

Configuration settings are located in the `application.properties` file. Key configurations include:

- **Payment API URL**: The URL for the third-party payment API.
- **Private and Public Keys**: Keys required for authentication.

Make sure to set these configurations as environmental variables.

### Testing
Unit tests were written for the Authentication method and the AirtimePurchase method to effectively cover several important edge cases such as;
1. AuthServiceImPlTest: register_success_test, invalidEmail_test, userAlreadyExists_test, login_success_test, invalidCredentials_test.
2. AirtimePurchaseTest: testInitiateAirtimePurchase_Success, testInitiateAirtimePurchase_InvalidPhoneNumber, testBuyAirtime_Success, testBuyAirtime_Failure, testCalculateHMAC512, testIsValidPhoneNumber.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/egbas/XpressPayment
   cd XpressPayment

### Usage
The primary functionality of the application is to process airtime payments. You can make payment requests through the /api/v1/payment/airtime endpoint.
A sample json request

    POST {{base_url}}/api/v1/payment/airtime
    
    {
    "requestId": "144444",
    "uniqueCode": "MTN_24207",
    "details": {
    "phoneNumber": "08033333333",
    "amount": 100
    }
    }

Response Sample

    {
    "requestId": "144444",
    "referenceId": "------------",
    "responseCode": "00",
    "responseMessage": "Successful",
    "data": {
        "phoneNumber": "08033333333"
        "amount": 100
        }
    }
