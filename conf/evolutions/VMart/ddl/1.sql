CREATE SEQUENCE public.hibernate_sequence
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 3333
  CACHE 1;

CREATE TABLE public.tweet (
  id                NUMERIC(15,0) NOT NULL PRIMARY KEY,
  user_id           NUMERIC(15,0) NOT NULL UNIQUE,
  user_name         VARCHAR(100),
  text              VARCHAR(256),
  hash_tag          VARCHAR(50),
  lang              VARCHAR(25),
  sentiment         VARCHAR(100),
  sentiment_score   DECIMAL(5,5),
  created_at        TIMESTAMP,
  retweets          NUMERIC(15,0)

);


SELECT COUNT(*) as TOTAL_TWEETS from public.tweet;