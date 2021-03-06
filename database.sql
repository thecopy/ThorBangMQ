-- Table: clients

-- DROP TABLE clients;

CREATE TABLE clients
(
  id bigserial NOT NULL,
  name character varying(25),
  CONSTRAINT id PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE clients
  OWNER TO asl;


-- Table: queues

-- DROP TABLE queues;

CREATE TABLE queues
(
  id bigserial NOT NULL,
  name character varying(25),
  CONSTRAINT queues_id PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE queues
  OWNER TO asl;



-- Table: messages

-- DROP TABLE messages;

CREATE TABLE messages
(
  id bigserial NOT NULL,
  sender_id bigint,
  receiver_id bigint,
  queue_id bigint NOT NULL,
  time_of_arrival timestamp without time zone NOT NULL DEFAULT now(),
  priority integer NOT NULL,
  context_id bigint,
  message text NOT NULL,
  CONSTRAINT messages_id PRIMARY KEY (id ),
  CONSTRAINT client_id FOREIGN KEY (sender_id)
      REFERENCES clients (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT queue_id FOREIGN KEY (queue_id)
      REFERENCES queues (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT sender_id FOREIGN KEY (sender_id)
      REFERENCES clients (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE messages
  OWNER TO asl;

-- Index: fki_client_id
