
CREATE TABLE IF NOT EXISTS users(
  id varchar NOT NULL,
  validId varchar(10) UNIQUE NOT NULL,
  username varchar NOT NULL,
  password varchar(20) NOT NULL,
  email varchar,
  address varchar,
  phone_number varchar(10) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS account_type(
  id varchar(1) NOT NULL,
  name varchar(50) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS accounts(
  id text NOT NULL,
  account_type_id varchar(1) NOT NULL,
  balance decimal,
  PRIMARY KEY (id),
  FOREIGN KEY (account_type_id) REFERENCES account_type(id)
);

CREATE TABLE IF NOT EXISTS account_user_mapping(
  account_id text NOT NULL,
  user_id text NOT NULL,
  PRIMARY KEY (account_id, user_id),
  FOREIGN KEY (account_id) REFERENCES accounts(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transactions(
  id text NOT NULL,
  account_id text NOT NULL,
  user_id text NOT NULL,
  transaction_type varchar(20) NOT NULL,
  amount decimal NOT NULL,
  transaction_time_epoch text NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (account_id) REFERENCES accounts(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);


--insert into users values('1', 'abc1', 'juhi', 'pwd', 'email', 'address', 1235436748);
--insert into users values('2', 'abd1', 'justin', 'pxd', 'email', 'address', 1235436748);
--insert into users values('3', 'adg1', 'jan', 'put', 'email', 'address', 1235436748);
--insert into users values('4', 'aft1', 'jana', 'set', 'email', 'address', 1235436748);
--insert into users values('5', 'aer1', 'junu', 'cut', 'email', 'address', 1235436748);
--
--insert into account_type values('1', 'single');
--insert into account_type values('2', 'joint');
--
--insert into accounts values('1', '1', 0.0);
--insert into accounts values('2', '2', 30.0);
--insert into accounts values('3', '1', 100.0);
--insert into accounts values('4', '2', 0.0);
--insert into accounts values('5', '1', 2.0);
--insert into accounts values('6', '2', 0.0);
--
--insert into account_user_mapping values('1', '2');
--insert into account_user_mapping values('2', '3');
--insert into account_user_mapping values('2', '4');
--insert into account_user_mapping values('4', '4');
--insert into account_user_mapping values('5', '1');
--
--insert into transactions values('t1', '1', '2', 'withdraw', 1, '17089');
--
--select * from users;
--select * from accounts;
--select * from account_user_mapping;
--select * from transactions;
