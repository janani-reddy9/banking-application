# Transaction

1. Create
- Request:
```json
{
  "accountId": "account1",
  "userSessionId": "session",
  "transactionType": "withdraw|deposit",
  "amount": 120.1
}
```
- Assumptions:
  - If there is only one instance of the application running, implementation1 works
  - If there are multiple instances of the application running, we would need a cache to store this information
- Implementation1:
  - Create a `HashMap` transactionSessions in play cache with values (`accountId` -> `transactionSessionId`)
- Implementation2: (Redis cache)
  - Insert a record to Redis (`accountId` -> `transactionSessionId`)
- Validations:
  - Check if the sessionId is valid
  - Check if the accountId passed is having any transactionSessions. If no, update the cache and proceed with transaction, then remove the key. If yes, wait for 500ms for 2 times, on 3rd check throw 404 with `cannot proceed with multiple transactions at a time for same account`
  - If `transactionType` is withdraw, then validate whether the accountId is having enough balance. If yes, deduct money. If no, throw 404 with No sufficient balance.
- Response:
```json
{
  "status": 200,
  "description": ""
}
```

2. GetByAccountId
- Request:
```json
{
  "accountId": "id",
  "userSessionId": "session",
  "limit": 10 // Optional, default is 10, max is 50
}
```
- Validations:
  - Check if sessionId is valid
  - Check if accountId is valid
- Response:
```json
{
  "status": 200,
  "description": "",
  "transactions": [
    {
      "transactionType": "",
      "amount": 10.1,
      "balance": 125.07
    }
  ]
}
```

3. GetByAccountIdAndUserId
- Request:
```json
{
  "accountId": "id",
  "userId": "is",
  "userSessionId": "session",
  "limit": 10 // Optional, default is 10, max is 50
}
```
- Validations:
  - Check if userSessionId is valid
  - Check if accountId is valid
  - Check if accountId is joint account
  - Check if User is mapped to account
- Response:
```json
{
  "status": 200,
  "description": "",
  "transactions": [
    {
      "transactionType": "",
      "amount": 10.1,
      "balance": 125.07
    }
  ]
}
```

Food to think:
1. Can 2 and 3 APIs be a single API? with userId as optional parameter?
