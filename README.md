# Spark Streaming with Kafka
## Local Usage

1. Start ZooKeeper

    `bin/zookeeper-server-start.sh config/zookeeper.properties`

2. Start Kafka

    `bin/kafka-server-start.sh config/server.properties`
    
3. Create a Kafka topic

    `bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 4 --topic spark_sql_test_topic`
    
4. Start Spark application

    >`spark-submit /path/to/jar-file`
    
    `spark-submit target/scala-2.12/spark-streaming-test.jar localhost:9092 spark-topic`

5. Send messages

    `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic spark_sql_test_topic`

### build
> Build the project
* sbt clean compile assembly

> Once the project is built successlly and created jar in the 'target' folder, submit it to spark
* spark-submit /path/to/jar-file


### Local Execution

> Start ZooKeeper and kafka 
*  zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties

> Create Topic
* kafka-topics --zookeeper localhost:2181 --create --topic merchant-stream8 --partitions 1 --replication-factor 1

> Run Kafka Console Priducer 
* kafka-console-producer --broker-list localhost:9092 --topic merchant-stream9

> Submit package to spark and run
* spark-submit target/scala-2.11/spark-streaming-merchant.jar localhost:9092 merant-stream9



