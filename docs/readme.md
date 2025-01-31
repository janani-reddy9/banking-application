## Problem Statement:

### Description:
As mentioned above, the exercise should satisfy the following scenario:
A user has an account at the Bank and should be able to control their account through an ATM. The end goal of this project is to:
1. Allow users to deposit and withdraw money from an ATM (Keep in mind withdrawal limitations - no overdraft allowed)
2. Allow simultaneous access of an account (joint accounts), so it's important to check the order of withdrawals (balance should never fall
   below 0)

### Requirements:
1. An application that handles and covers the scenario above satisfying both points 1 + 2 through an api layer
2. All database scripts for the creation of the tables/indices/foreign keys etc.
3. Readme instructions for running the application end to end (including setting up the database)

## Solution

### Database

All the related tables schema is present in [Tables](../database/init.sql), visualization [here](../database/schema-diagram.png).

Generated diagram using - https://dbdiagram.io/d 

Note: 

### Common Assumptions/Validations across APIs:

1. Validate the case class against the json passed.
2. All the parameters are expected to be passed correctly from UI

### APIs

1. [User](apis/user-readme.md)
2. [Account](apis/account-readme.md)
3. [Transaction](apis/transaction-readme.md)
