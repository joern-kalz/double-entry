from db import Session
from model import Account, Transaction, Entry
from sqlalchemy.orm import selectinload
from datetime import datetime
from flask import make_response


def find_all(after = None, before = None):
    balances_by_accountId = {}

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

        for transaction in transactions:
            for entry in transaction.entries:
                balance = balances_by_accountId.get(entry.account_id, 0)
                balance += entry.amount
                balances_by_accountId[entry.account_id] = balance

    return [
        {'accountId': account_id, 'balance': balance} 
        for account_id, balance in balances_by_accountId.items()
    ]        
