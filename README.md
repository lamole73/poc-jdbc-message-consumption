# Purpose
The purpose of this repo is to test simultaneous "_message_" consumption from same table 
on multiple threads via JDBC and Oracle `SELECT FOR UPDATE SKIP LOCKED`

See also: https://medium.com/@manjulapiyumal/effective-usage-oracle-row-locking-with-spring-jdbc-for-concurrent-data-processing-without-having-9ae55e4331a6

# Release
Version: 1.0.0

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

# Implementation

There are various JDBC implementations which are described below as JDBCRepository#.

The JDBCRepositories are called via a spring `Scheduler` 
which is configured to run 3 threads per interval (5000 miliseconds)

Simultaneous access to the database is further tested using a `Thread.sleep` with configurable timeout 
(see `application.properties/sample.jdbc.delay` parameter)
As such each thread will keep the records locked until the timeout passes before flushing.

## JDBCRepository1
The JDBCRepository1 test is implemented per reference.
But it is not really efficient as all threads await the 1st thread to finish, 
meaning only 1 record per interval is processed.

The reason is that the actual process of the reference implementation 
was to processes multiple rows per call but on our case we only want to process one per call.

As such this is just for info and should not be used

## JDBCRepository2
The JDBCRepository2 test works as expected and is implemented using spring PreparedStatementCreator and RowCallbackHandler

## JDBCRepository3
The JDBCRepository3 test works as expected and is implemented using pure JDBC PreparedStatement and ResultSet

# How to run
To execute the sample you need to
1. Start the Oracle XE
see Prerequisites the DB is now listening on 192.168.99.100 port:1521
2. Run the `sql/create.sql` and then `sql/data.sql` against the `autotest` DB schema
This could be executed via sqlplus or any other progam
3. Execute the spring boot app, i.e. `./mvnw`
4. watch the output and that each executor is actually processing different row, 
i.e. see the below output from using `JDBCRepository3`
```
...
2019-04-26 18:29:18.998  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] START at time: 2019-04-26T18:29:18.997
2019-04-26 18:29:18.998  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute3] [thread: lo-scheduled-task-pool-2] START at time: 2019-04-26T18:29:18.997
2019-04-26 18:29:18.998  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] START at time: 2019-04-26T18:29:18.997
2019-04-26 18:29:19.012  INFO 8740 --- [led-task-pool-1] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2019-04-26 18:29:19.012  WARN 8740 --- [led-task-pool-1] com.zaxxer.hikari.util.DriverDataSource  : Registered driver with driverClassName=oracle.jdbc.driver.OracleDriver was not found, trying direct instantiation.
2019-04-26 18:29:19.442  INFO 8740 --- [led-task-pool-1] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2019-04-26 18:29:19.506  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Started msgId not known yet
2019-04-26 18:29:19.506  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] Started msgId not known yet
2019-04-26 18:29:19.525  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-2] Started msgId not known yet
2019-04-26 18:29:19.591  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-2] Processing msgId=72
2019-04-26 18:29:19.661  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Processing msgId=74
2019-04-26 18:29:19.680  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] Processing msgId=73
2019-04-26 18:29:20.154  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-2] processed 1 records with ids=[72]
2019-04-26 18:29:20.162  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-2] Ended msgId=72
2019-04-26 18:29:20.162  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute3] [thread: lo-scheduled-task-pool-2] DONE  at time: 2019-04-26T18:29:20.162
2019-04-26 18:29:20.175  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] processed 1 records with ids=[74]
2019-04-26 18:29:20.177  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Ended msgId=74
2019-04-26 18:29:20.177  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] DONE  at time: 2019-04-26T18:29:20.177
2019-04-26 18:29:20.191  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] processed 1 records with ids=[73]
2019-04-26 18:29:20.194  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] Ended msgId=73
2019-04-26 18:29:20.194  INFO 8740 --- [led-task-pool-1] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-1] DONE  at time: 2019-04-26T18:29:20.194
2019-04-26 18:29:23.993  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] START at time: 2019-04-26T18:29:23.993
2019-04-26 18:29:23.993  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute3] [thread: lo-scheduled-task-pool-4] START at time: 2019-04-26T18:29:23.993
2019-04-26 18:29:23.993  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] START at time: 2019-04-26T18:29:23.993
2019-04-26 18:29:23.998  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Started msgId not known yet
2019-04-26 18:29:24.001  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] Started msgId not known yet
2019-04-26 18:29:24.017  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-4] Started msgId not known yet
2019-04-26 18:29:24.026  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Processing msgId=75
2019-04-26 18:29:24.029  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-4] Processing msgId=77
2019-04-26 18:29:24.029  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] Processing msgId=76
2019-04-26 18:29:24.569  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-4] processed 1 records with ids=[77]
2019-04-26 18:29:24.573  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-4] Ended msgId=77
2019-04-26 18:29:24.573  INFO 8740 --- [led-task-pool-4] l.s.m.endpoint.Scheduler                 : [execute3] [thread: lo-scheduled-task-pool-4] DONE  at time: 2019-04-26T18:29:24.573
2019-04-26 18:29:24.575  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] processed 1 records with ids=[75]
2019-04-26 18:29:24.576  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] processed 1 records with ids=[76]
2019-04-26 18:29:24.578  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] Ended msgId=76
2019-04-26 18:29:24.578  INFO 8740 --- [led-task-pool-2] l.s.m.endpoint.Scheduler                 : [execute1] [thread: lo-scheduled-task-pool-2] DONE  at time: 2019-04-26T18:29:24.578
2019-04-26 18:29:24.580  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] Ended msgId=75
2019-04-26 18:29:24.580  INFO 8740 --- [led-task-pool-3] l.s.m.endpoint.Scheduler                 : [execute2] [thread: lo-scheduled-task-pool-3] DONE  at time: 2019-04-26T18:29:24.580
...
```

