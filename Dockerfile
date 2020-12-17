FROM openjdk:16-ea-20-jdk

COPY dist/diversity-score.jar /diversity-score.jar

CMD ["java","-jar","/diversity-score.jar"]