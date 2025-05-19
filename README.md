# banking_platform

# A banking platform that enable customers access card services.It uses microservices approach as below:
1. Card service: manages customer’s card data.
2. Account service: manages customer’s account.
3. Customer service: Handles customer’s bio data

# How To install
1. DB connection
  1. Install Postgres DB
  2. Then add your username and password under the application.properties.
    Default username=postgres, password=root10
  3. Create three databases: bank_customer, bank_account, bank_card 
3. Apache Kafka Connection
  1. Install and run Zookeeper
  2. Intall and run apache kafka
# Run the Microservices
1. Build the project using maven(mvn clean install).
2. Access the microservices:
  1. Customer: http://{{baseUrl}}:8081/api/v1/customers
  2. Account: http://{{baseUrl}}:8082/api/v1/accounts
  3. Card: http://{{baseUrl}}:8083/api/v1/cards
