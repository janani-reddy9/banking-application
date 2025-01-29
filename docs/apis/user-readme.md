# User

### 1. Create
- Request:
```json
{
  "name": "test_name",
  "validId": "id",
  "email": "test@test.com",
  "phno": "6790072367",
  "address": "Flat xx, Road x, City x",
  "password": "somepwd"
}
```

- Assumptions:
  - password and confirm password options are there at UI and confirmed password is sent to API in encoded format
  - validId will be of nation's identity issued by Government. Limiting the validation to only one valid id proof.
- Validations:
  - validId should be unique

- Ideal Response:
```json
{
  "id": "createdId",
  "status": 200,
  "description": "User is created"
}
```

### 2. UpdateInformation

- Request:
```json
{
  "id": "id",
  "sessionId": "session",
  "updates": [
    {
      "field": "updated_value"
    }
  ]
}
```
- Validations:
  - Check if the id is valid
  - Check if the sessionId is valid for user
  - Validate if the requested field to update and the existing field are not same
- Ideal Response:
```json
{
   "status": 200,
   "description": "Updated the user details"
}
```

### 3. Deactivate

- Request:
```json
{
  "id": "user_id",
  "sessionId": "session",
  "deactivate_reason": ""
}
```
- Validations:
  - Check if the user exists
  - Check if the sessionId is valid for user
- Ideal Response:
```json
{
  "status": 200,
  "description": "Deactivated the user"
}
```

### 4. GetById
- Request:
```json
{
  "userId": "id",
  "sessionId": "session"
}
```
- Validations:
  - validate session id
- Response:
```json
{
  "status": 200,
  "user": {
    "name": "test_name",
    "validId": "id",
    "email": "test@test.com",
    "phno": "6790072367",
    "address": "Flat xx, Road x, City x",
  },
  "description": ""
}
```

### 5. Login
- Request:
```json
{
  "userId": "",
  "password": ""
}
```
- Validation:
  - userId should exist
  - password should match
- Response:
```json
{
  "status": 200,
  "description": "",
  "sessionId": "session"
}
```

### 5. Logout
- Request:
```json
{
  "userId": "",
  "sessionId": ""
}
```
- Validation:
  - userId should exist
  - sessionId should be valid for user
- Response:
```json
{
  "status": 200,
  "description": ""
}
```
