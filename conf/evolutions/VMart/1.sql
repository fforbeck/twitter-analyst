CREATE TABLE public.tweet (
  id        NUMERIC(9,0) NOT NULL PRIMARY KEY,
  user_id    VARCHAR(100),
  data      VARCHAR(255),
  tag       VARCHAR(100),
  sentiment NUMERIC,
  created_on  DATE
);

CREATE SEQUENCE public.hibernate_sequence
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 3333
  CACHE 1;
