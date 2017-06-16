# Clustered Security Configuration Service

This is a proof of concept service that polls, validates, and applies security configuration metadata from a distrubuted consensus data-store and applies it to local OS services.  It is a demonstration of potential OS features for IoT native operating systems as part of a class project for the Syracuse University class CIS657 Principles of Operating Systems.

This is a Linux system service with a RHEL/CentOS 7 compatible init script.  The service is a Java Spring Boot (https://projects.spring.io/spring-boot/) application.  The service implements the Copycat (http://atomix.io/copycat/) server process providing a fault-tolerant state machine replication framework using the Raft consensus algorithm.

For our demonstration we deployed this application to a small number of EC2 hosts to simulate IoT devices.  Every five minutes the application's scheduler executes the YumConfigService to poll the distributed data store for security configuration updates.  In this demo the service updates Yum repository configuration used by the system to perform software updates.  The value of each KVP is expected to be SHA256withRSA signed and the service validates the signature with the public key of the trusted signer.

Signed keys may be generated and submitted to the cluster using the cscs-admin-utility (https://github.com/kjfallon/cscs-admin-utility)

Log file excerpt of the YumConfigService executing
---------------------------------------------------
```
2016-12-02 17:25:00,009 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig Scheduled task processClusteredSecurityConfigMetadata starting.  
2016-12-02 17:25:00,009 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig Starting to apply Yum configuration  
2016-12-02 17:25:00,010 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService This node is a member of 1 config domains  
2016-12-02 17:25:00,011 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Reading values for domain domain1 from cluster  
2016-12-02 17:25:00,022 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Reading key: domain1.yum.centos7.repo.name  
2016-12-02 17:25:00,059 DEBUG [pool-4-thread-1] e.s.e.c.c.s.StateMachineClientOperationsService MapGetQuery result: ZGVtb1JlcG8=,jpr072vbJ9lBMjaCAXNb2/o1J525gz4c16p+jwes0q+lEcRJSfD1ZdOHZG0JN9qpW3NB4MJ3kmEisPP9c5yfmxRivZMSXhbOvzDuH0OQSTmq8bsU/jadJyD615kpusigS//9iRD9VCB9dEho7RZiNVlCwChBJhXMaOYlizZOkZI=  
2016-12-02 17:25:00,061 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService signature validation of nameKey data is: true  
2016-12-02 17:25:00,061 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Reading key: domain1.yum.centos7.repo.baseurl  
2016-12-02 17:25:00,072 DEBUG [pool-4-thread-1] e.s.e.c.c.s.StateMachineClientOperationsService MapGetQuery result: aHR0cHM6Ly9taXJyb3Iuc3lyLmVkdS9jZW50b3Mv,VXvAodFgzeE4xEMDQof+od7s1GpzgVX+/WJLT65T8fjwe6SXzQmbRD6nNWxFeAspHuDJ1MRE7lZtQlsjzSjFS9im4EJbgA0cNgJTyXSAscYblslqLLztljEh9qIyYSYcm+zJhIrtDZvCY+hm8eri98fN+fgQD8ZkiGUl07GBXEk=  
2016-12-02 17:25:00,073 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService signature validation of baseUrlKey data is: true  
2016-12-02 17:25:00,073 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Reading key: domain1.yum.centos7.repo.enabled  
2016-12-02 17:25:00,096 DEBUG [pool-4-thread-1] e.s.e.c.c.s.StateMachineClientOperationsService MapGetQuery result: MA==,Qu6lLzq26FG1h1JDmrS5LKw7Eo0rMvcVelL1r5jMQnUrZD47t7RjY2+dSV/K51bMBOaWkmedxUOZny3EmlGuRCmVsv4rhBLIIxtQOPnF1Tv0f9lRRWgfJHJiwMrwpehu/NSUiM7BTNp0/pu3BPkqwPwnguagJLCOh5uiOmodSD8=  
2016-12-02 17:25:00,097 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService signature validation of enabledKey data is: true  
2016-12-02 17:25:00,097 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Domain domain1 name: demoRepo  
2016-12-02 17:25:00,097 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Domain domain1 base url: https://mirror.syr.edu/centos/  
2016-12-02 17:25:00,097 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Domain domain1 enabled: 0  
2016-12-02 17:25:00,108 DEBUG [pool-4-thread-1] e.s.e.c.c.s.YumConfigService Updated domain demoRepo repo file: /etc/yum.repos.d/CSCS-Managed-demoRepo.repo  
2016-12-02 17:25:00,108 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig result of processYumConfig: true  
2016-12-02 17:25:00,108 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig Completed applying Yum configuration  
2016-12-02 17:25:00,109 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig Scheduled task processClusteredSecurityConfigMetadata execution time: 0.1 seconds (0.0min)  
2016-12-02 17:25:00,109 INFO [pool-4-thread-1] e.s.e.c.c.c.SchedulerConfig Scheduled task processClusteredSecurityConfigMetadata complete.  Next run will be 5 minutes from this timestamp
```
