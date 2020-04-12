from db import Session
from model import Account, Transaction, Entry
from sqlalchemy.orm import selectinload
from datetime import datetime
from flask import make_response


def find_all(after = None, before = None):
    error = validate_parameters(after, before)

    if error != None:
        return error

    with Session() as session:
        balances_by_accountId = get_balances(session, after, before)
        aggregate(session, balances_by_accountId)

    return [
        {'accountId': account_id, 'balance': balance / 100} 
        for account_id, balance in balances_by_accountId.items()
    ]        

def validate_parameters(after, before):
    if after != None: 
        try:
            datetime.strptime(after, '%Y-%m-%d')
        except ValueError:
            return make_response('invalid query parameter "after"', 400)

    if before != None: 
        try:
            datetime.strptime(before, '%Y-%m-%d')
        except ValueError:
            return make_response('invalid query parameter "before"', 400)

    return None

def get_balances(session, after, before):
    balances_by_accountId = {}

    for transaction in get_transactions(session, after, before):
        for entry in transaction.entries:
            balance = balances_by_accountId.get(entry.account_id, 0)
            balance += int(entry.amount * 100)
            balances_by_accountId[entry.account_id] = balance
    
    return balances_by_accountId

def get_transactions(session, after, before):
    transactions = session.query(Transaction).options(
        selectinload(Transaction.entries)
    )

    if after != None: 
        transactions = transactions.filter(Transaction.date >= after)

    if before != None: 
        transactions = transactions.filter(Transaction.date <= before)

    return transactions

class AccountNode:
    def __init__(self, id, parent_id):
        self.id = id
        self.parent_id = parent_id
        self.children = []

class Breadcrumb:
    def __init__(self, account_node):
        self.account_node = account_node
        self.child_index = 0

def aggregate(session, balances):
    accounts = get_accounts(session)

    for root in accounts:
        aggregate_for_root(root, balances)

def aggregate_for_root(root, balances):
    breadcrumbs = [Breadcrumb(root)]

    while len(breadcrumbs) > 0:
        current = breadcrumbs[-1]

        if len(current.account_node.children) > current.child_index:
            next = current.account_node.children[current.child_index]
            breadcrumbs.append(Breadcrumb(next))
            current.child_index += 1
        else:
            if len(breadcrumbs) > 1:
                parent = breadcrumbs[-2]
                balance = balances.get(parent.account_node.id, 0)
                balance += balances.get(current.account_node.id, 0) 
                balances[parent.account_node.id] = balance

            breadcrumbs.pop()

def get_accounts(session):
    account_nodes_by_id = {
        account.id : AccountNode(account.id, account.parent_id)
        for account in session.query(Account).all()
    }

    root_accounts = []

    for account_node in account_nodes_by_id.values():
        if account_node.parent_id in account_nodes_by_id:
            account_nodes_by_id[account_node.parent_id].children.append(account_node)
        else:
            root_accounts.append(account_node)

    return root_accounts