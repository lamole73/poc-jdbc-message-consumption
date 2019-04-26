--
-- MSG_DATA  (Table)
--
CREATE TABLE MSG_DATA
(
  MSG_ID        INTEGER                         NOT NULL,
  MSG_STATUS    INTEGER                         NOT NULL,
  MSG_CONTENT   CLOB,
  PROC_CONTENT  CLOB,
  PROC_APP  VARCHAR2(2000 CHAR),
  PROC_TIME     TIMESTAMP(6),
  VENDOR_ID     INTEGER                         NOT NULL
)
NOLOGGING
NOCOMPRESS
NOCACHE
NOPARALLEL
NOMONITORING;


--
-- Non Foreign Key Constraints for Table MSG_DATA
--
ALTER TABLE MSG_DATA ADD (
  PRIMARY KEY
  (MSG_ID)
  USING INDEX
);