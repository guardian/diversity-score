FROM openjdk:16-ea-20-jdk

COPY target/scala-2.13/diversity-score-assembly-0.1.0-SNAPSHOT.jar /diversity-score.jar

CMD ["java","-jar","/diversity-score.jar"]