package io.github.kayr.ezyquery;

public interface EzyInsert {}

/*
ezy.insert(    T.SAVING_TX)
   .set(T.SAVING_TX.ACCOUNT_ID, 1)
    .set(T.SAVING_TX.TRANSACTION_DATE, "2023-10-01")
    .set(T.SAVING_TX.TRANSACTION_AMOUNT, 100)
    .persist()


ezy.update(T.SAVING_TX)
    .set(T.SAVING_TX.TRANSACTION_DATE, "2023-10-01")
    .set(T.SAVING_TX.TRANSACTION_AMOUNT, 100)
    .where(T.SAVING_TX.ACCOUNT_ID, 1)
    .persist()

ezy.

 */
