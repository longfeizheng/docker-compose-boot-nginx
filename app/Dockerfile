FROM hub.c.163.com/wuxukun/maven-aliyun:3-jdk-8
ADD ["target/app.jar", "app.jar"]
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]