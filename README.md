Spark Streaming with Kafka
## Local Usage

1. Start ZooKeeper

    `bin/zookeeper-server-start.sh config/zookeeper.properties`

2. Start Kafka

    `bin/kafka-server-start.sh config/server.properties`
    
3. Create a Kafka topic

    `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 4 --topic spark_sql_test_topic`
    
4. Start Spark application

5. Send messages

    `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic spark_sql_test_topic`

### build
> Build the project
* sbt clean compile assembly

> Once the project is built successlly and created jar in the 'target' folder, submit it to spark
* spark-submit /path/to/jar-file


