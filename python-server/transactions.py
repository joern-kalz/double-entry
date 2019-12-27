from flask import make_response

import db
from psycopg2.extras import execute_values


def create(body): 
    transaction = (body.get('date'), body.get('name'))

    db.cur.execute("""
        INSERT INTO transactions (date, name) 
        VALUES (%s, %s)
        RETURNING id""",
        transaction)
        
    transactionId = db.cur.fetchone()[0]

    createEntries(transactionId, body.get('entries'))
    return make_response('transaction created', 201)


def update(transactionId, body): 
    transaction = (body.get('date'), body.get('name'), transactionId)

    db.cur.execute("""
        UPDATE transactions 
        SET date = %s, name = %s
        WHERE id = %s""",
        transaction)
        
    db.cur.execute("""
        DELETE FROM entries 
        WHERE transactionId = %s""", 
        (transactionId,))

    createEntries(transactionId, body.get('entries'))
    return 'transaction updated'


def delete(transactionId): 
    db.cur.execute("""
        DELETE FROM transactions 
        WHERE id = %s""", 
        (transactionId,))
        
    db.cur.execute("""
        DELETE FROM entries 
        WHERE transactionId = %s""", 
        (transactionId,))

    return 'transaction deleted'


def findAll():
    db.cur.execute("""
        SELECT 
            transactions.id,
            transactions.date,
            transactions.name,
            entries.amount,
            entries.verified,
            entries.accountId,
            accounts.name
        FROM transactions
        INNER JOIN entries 
            ON transactions.id = entries.transactionId
        LEFT OUTER JOIN accounts 
            ON accounts.id = entries.accountId
        ORDER BY transactions.id, accounts.id
        """)

    result = []
    currentId = ''
    for row in db.cur.fetchall():
        (id, date, name, amount, verified, accountId, accountName) = row
 
        if id != currentId: 
            result.append({
                'id': id, 
                'date': date, 
                'name': name, 
                'entries': []
            })
            currentId = id

        result[-1]['entries'].append({
            'amount': amount,
            'verified': verified,
            'account': {'id': accountId, 'name': accountName}
        })

    return result
    
    
def createEntries(transactionId, entries):
    entries = [
        (
            transactionId, 
            entry.get('account'), 
            entry.get('amount'), 
            entry.get('verified', False)
        ) 
        for entry in entries
    ]

    execute_values(db.cur, """
        INSERT INTO entries (transactionId, accountId, amount, verified) 
        VALUES %s""",
        entries)
