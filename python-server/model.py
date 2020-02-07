from sqlalchemy import Column, Integer, String, DateTime, Float, Boolean, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship, backref

Base = declarative_base()

class Transaction(Base):
    __tablename__ = 'transactions'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(250))
    date = Column(DateTime)

    entries = relationship('Entry', foreign_keys='Entry.transaction_id', 
        cascade="all, delete, delete-orphan")


class Entry(Base):
    __tablename__ = 'entries'

    transaction_id = Column(Integer, ForeignKey('transactions.id'), 
        primary_key=True)
    account_id = Column(Integer, ForeignKey('accounts.id'), 
        primary_key=True)
    amount = Column(Float)
    verified = Column(Boolean)

    transaction = relationship('Transaction', 
        foreign_keys='Entry.transaction_id')
    account = relationship('Account', 
        foreign_keys='Entry.account_id')


class Account(Base):
    __tablename__ = 'accounts'

    id = Column(Integer, primary_key=True)
    name = Column(String(250))
    parent_id = Column(Integer, ForeignKey('accounts.id'))
    active = Column(Boolean)

    entries = relationship('Entry', 
        foreign_keys='Entry.account_id')
    children = relationship('Account', 
        backref=backref('parent', remote_side=[id]))
