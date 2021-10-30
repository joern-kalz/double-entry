import Entry from "./Entry";

export default class Transaction {
  date: string;
  name: string;
  amount: string;
  debitAccount?: string;
  newDebitAccount?: string;
  creditAccount?: string;
  newCreditAccount?: string;
}