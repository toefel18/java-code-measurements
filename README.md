# Small Java library for sampling code.

![travis-ci](https://travis-ci.org/toefel18/patan.svg?branch=master "build")(https://travis-ci.org/toefel18/patan)  ![codecov.io](https://codecov.io/github/toefel18/patan/coverage.svg?branch=master "coverage")(https://codecov.io/github/toefel18/patan)

There are multiple ways for measuring the performance of code, sampling and instrumentation. Instrumentation modifies the original program's byte code by inserting measurement code. Sampling is done by programming the measurement code manually. 

This is a sampling library that provides:
  - counters, keeping track of how many *something* has taken place
  - sampling, collecting samples and describing it's distribution
  - durations, measuring the duration of a task as a special case of sampling 

The library provides an API and comes with a default implementation safe to be used in a multi-threaded environment. 
  
Some examples:

```java
 // you can expose this instance application wide using an Enum, Singleton or perhaps as a spring-bean
 public static final Statistics STATISTICS = new StatisticsFacade();
 ...
 
 // counters
 public void onMessage(Message message) {
    STATISTICS.addOccurrence("jms.message.received");
 }
 
 
 // durations
 public void onMessage(Message message) {
    Stopwatch onMessageStopwatch = STATISTICS.startStopwatch();
    
    //expensive method that takes long
    processMessage(message);
    
    STATISTICS.recordElapsedTime("jms.message.received.duration", onMessageStopwatch);    
 }
 
 // samples
 public void onLogin(Message message) {
    int loggedInUsers = System.countUsersLoggedIn();
    STATISTICS.addSample("users.logged.in", loggedInUsers);    
 }
 
 // retrieve results 
 public void printResults() {
    Map<String, Statistic> statisticsByName = STATISTICS.getSnapshot();
    
    // each key has a corresponding statistic which provides samplecount/min/max/avg/variance/standarddeviation
 }
 
```