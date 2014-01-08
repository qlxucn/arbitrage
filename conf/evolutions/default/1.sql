# margins schema

# --- !Ups

CREATE SEQUENCE margin_id_seq;
CREATE TABLE margin (
    id integer NOT NULL DEFAULT nextval('margin_id_seq'),
    okcoin_cny      varchar(40),
    okcoin_usd      varchar(40),
    coinbase_cny    varchar(40),
    coinbase_usd    varchar(40),
    okcoin2Coinbase varchar(40),
    coinbase2Okcoin varchar(40),
    exchange_rate   varchar(40),
    created_at      varchar(40)
);

# --- !Downs

DROP TABLE margin;
DROP SEQUENCE margin_id_seq;