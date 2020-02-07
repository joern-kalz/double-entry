from model import Base, Account
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from os import environ

engine = create_engine(environ['db_url'])

Base.metadata.create_all(engine)

SessionImplementation = sessionmaker(bind=engine)

class Session:
    def __enter__(self):
        self.sessionImplementation = SessionImplementation()
        return self.sessionImplementation
    def __exit__(self, type, value, tb):
        if tb is None:
            self.sessionImplementation.commit()
        else:
            self.sessionImplementation.rollback()

with Session() as session:
    if session.query(Account).count() == 0:
        session.add(Account(name='[ASSET]', active=True))
        session.add(Account(name='[EXPENSE]', active=True))
        session.add(Account(name='[REVENUE]', active=True))

