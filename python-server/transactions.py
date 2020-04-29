from flask import make_response
from db import Session
from model import Transaction, Entry
from sqlalchemy.orm import selectinload
from datetime import datetime


def create(body): 
    transaction = Transaction(
        name = body.get('name'), 
        date = body.get('date'),
        entries = create_entries(body.get('entries'))
    )

    with Session() as session: session.add(transaction)

    return make_response({'createdId': transaction.id}, 201)


def update(transactionId, body): 
    with Session() as session:
        transaction = session.query(Transaction).get(transactionId)
        transaction.name = body.get('name')
        transaction.date = body.get('date')
        transaction.entries = create_entries(body.get('entries'))

    return 'transaction updated'


def delete(transactionId): 
    with Session() as session: 
        transaction = session.query(Transaction).get(transactionId)
        session.delete(transaction)

    return 'transaction deleted'


def find_by_id(transactionId):
    with Session() as session:
        transaction = session.query(Transaction).options(
            selectinload(Transaction.entries)
        ).get(transactionId)
        
        return {
            'id': transaction.id, 
            'date': transaction.date.strftime('%Y-%m-%d'), 
            'name': transaction.name, 
            'entries': [
                {
                    'amount': entry.amount,
                    'verified': entry.verified,
                    'accountId': entry.account_id
                } 
                for entry in transaction.entries
            ]
        }
    
def find_all(after = None, before = None):
    with Session() as session:
        transactions = session.query(Transaction).options(
            selectinload(Transaction.entries)
        )

        if after != None: 
            try:
                datetime.strptime(after, '%Y-%m-%d')
            except ValueError:
                return make_response('invalid query parameter "after"', 400)
            transactions = transactions.filter(Transaction.date >= after)

        if before != None: 
            try:
                datetime.strptime(before, '%Y-%m-%d')
            except ValueError:
                return make_response('invalid query parameter "before"', 400)
            transactions = transactions.filter(Transaction.date <= before)

        return [
            {
                'id': transaction.id, 
                'date': transaction.date.strftime('%Y-%m-%d'), 
                'name': transaction.name, 
                'entries': [
                    {
                        'amount': entry.amount,
                        'verified': entry.verified,
                        'accountId': entry.account_id
                    } 
                    for entry in transaction.entries
                ]
            } 
            for transaction in transactions.order_by(Transaction.date.desc(), Transaction.id)
        ]
    
    
def create_entries(entries):
    return [
        Entry(
            account_id = entry.get('accountId'), 
            amount = entry.get('amount'), 
            verified = entry.get('verified', False)
        ) 
        for entry in entries
    ]