from flask import make_response
from db import Session
from model import Account


def create(body): 
    account = Account(
        name = body.get('name'), 
        parent_id = body.get('parentId'), 
        active = body.get('active')
    )

    with Session() as session: session.add(account)

    return make_response({'createdId': account.id}, 201)


def update(body, accountId): 
    with  Session() as session:
        account = session.query(Account).get(accountId)
        if body.get('name') != None: 
            account.name = body.get('name')
        if body.get('parentId') != None: 
            account.parent_id = body.get('parentId')
        if body.get('active') != None: 
            account.active = body.get('active')

    return 'account updated'


def find_all():
    with Session() as session:
        return [
            {
                'id': account.id, 
                'name': account.name, 
                'parentId': account.parent_id, 
                'active': account.active
            } 
            for account in session.query(Account).all()
        ]
    