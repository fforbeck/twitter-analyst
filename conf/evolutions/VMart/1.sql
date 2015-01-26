CREATE TABLE public.tweet (
  id                NUMERIC(15,0) NOT NULL PRIMARY KEY,
  user_id           NUMERIC(15,0) NOT NULL UNIQUE,
  user_name         VARCHAR(100),
  text              VARCHAR(144),
  hash_tag          VARCHAR(50),
  lang              VARCHAR(25),
  sentiment         VARCHAR(100),
  sentiment_score   DECIMAL(5,5),
  created_at        DATE
);

CREATE SEQUENCE public.hibernate_sequence
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 3333
  CACHE 1;
