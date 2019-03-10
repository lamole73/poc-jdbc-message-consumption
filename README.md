# Prerequisites

Oracle database, see https://github.com/DeepDiver1975/docker-oracle-xe-11g
docker pull deepdiver/docker-oracle-xe-11g:2.0
docker run -d --name -p 49160:22 -p 1521:1521 deepdiver/docker-oracle-xe-11g:2.0
connect on 192.168.99.100 1521 with autotest/owncloud

See also: https://medium.com/@manjulapiyumal/effective-usage-oracle-row-locking-with-spring-jdbc-for-concurrent-data-processing-without-having-9ae55e4331a6
Strange thing it is not working on same datasource.... i.e. both threads process the same row.



