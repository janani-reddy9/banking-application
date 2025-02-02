# Account

1. Create
- Request:
```json
{
  "userId": "id",
  "accountType": 0,
  "balance": 124.78 // Optional
}
```
- Validations:
  - user id exists, if not throw 404 with user doesn't exist
  - accountType exists, if not throw 404 with account type doesn't exist
- Additional implementation:
  - UserId and AccountId should be inserted in UserAccountMapping table as well. This can go to Kafka too, but for now, the implementation of this lies within the code blocking the confirmation.
- Response:
```json
{
  "status": 200,
  "description": "Account is created"
}
```

2. GetByAccountId
- Request:
```json
{
  "accountId": "",
  "sessionId": ""
}
```
- Validations:
  - accountId should exist
- Response:
```json
{
  "status": 200,
  "description": "",
  "account": {
    "accountId": "id",
    "users": [
      ""
    ],
    "accountType": "",
    "balance": "",
    "creationTimestamp": "" // epoch timestamp
    "updatedTimestamp": ""
  }
}
```
