from flask import make_response

import db

def create(body): 
    account = (body.get('name'), body.get('parentId'), body.get('active'))

    db.cur.execute("""
        INSERT INTO accounts (name, parentId, active) 
        VALUES (%s, %s, %s)
        RETURNING id""",
        account)

    id = db.cur.fetchone()[0]

    return make_response(f'account {id} created', 201)

def update(body, accountId): 
    account = (
        body.get('name'), 
        body.get('parentId'), 
        body.get('active'), 
        accountId
    )

    db.cur.execute("""
        UPDATE accounts 
        SET name = %s, parentId = %s, active = %s
        WHERE id = %s""",
        account)

    return 'account updated'

def findAll():
    db.cur.execute("""
        SELECT id, name, parentId, active
        FROM accounts
        """)

    return [{
            'id': row[0], 
            'name': row[1], 
            'parentId': row[2], 
            'active': row[3]
        } for row in db.cur.fetchall()]
    