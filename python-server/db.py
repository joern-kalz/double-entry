from psycopg2 import connect

con = connect(database="accounting", user="accounting", password="1a41gq5af5", host="127.0.0.1", port="5432")
con.autocommit = True
cur = con.cursor()

cur.execute("""
CREATE TABLE IF NOT EXISTS transactions (
	id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
	date DATE,
	name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS entries (
	transactionId INTEGER,
	accountId INTEGER,
	amount FLOAT,
    verified BOOLEAN
);

CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255),
    parentId INTEGER,
    active BOOLEAN
);
""")

