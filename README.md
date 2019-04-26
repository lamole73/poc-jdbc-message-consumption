# Prerequisites

Oracle database, see https://github.com/DeepDiver1975/docker-oracle-xe-11g

To initialize via docker toolbox on windows:  
```
docker pull deepdiver/docker-oracle-xe-11g:2.0  
docker run -d --name xe11g -p 49160:22 -p 1521:1521 deepdiver/docker-oracle-xe-11g:2.0  
```

To stop
```
docker stop xe11g
```
To start on another time (i.e. initialization ok and stopped)  
```
docker start xe11g
```

To access the database hostname:192.168.99.100, port:1521 with autotest/owncloud

# Purpose
The purpose of this repo is to test simultaneous message consumption from same table ov multiple threads via JDBC and Oracle FOR UPDATE NO WAIT

See also: https://medium.com/@manjulapiyumal/effective-usage-oracle-row-locking-with-spring-jdbc-for-concurrent-data-processing-without-having-9ae55e4331a6

## JDBCRepository test
The JDBCRepository test is implemented per reference but it is not really efficient as all threads await the 1st thread to finish.
The reason is that the reference processes multiple rows per call but we only want to process one.

## JDBCRepository2
The JDBCRepository2 test works as expected

# How to run
To execute the sample you need to
1. Start the Oracle XE, se Prerequisites
2. Execute the spring boot app, i.e. mvnw


