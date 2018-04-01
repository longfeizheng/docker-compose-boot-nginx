> Docker 是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。容器是完全使用沙箱机制，相互之间不会有任何接口。

## 前言
本篇文章引导你使用`Docker Compose`在`Docker`容器中运行`nginx`和两个简单的`Spring Boot`应用程序，从而实现负载均衡。关于`Docker` 入门请参考**纯洁的微笑**[Docker 系列文章](http://www.ityouknow.com/docker.html)

### 准备
- Docker CE

![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_01.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/docker/docker_01.png)


### 技术栈
- Docker
- Spring Boot
- NGINX
- Maven

### 目录结构
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.png")


#### Spring Boot应用程序
##### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
       <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.0.RELEASE</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>docker-compose-springboot-nginx</artifactId>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-freemarker</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

##### DockerController

```java
package cn.merryyou.docker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created on 2018/3/20 0020.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
@Controller
public class DockerController {

    @GetMapping("/")
    public String docker() {
        return "docker";
    }
}

```

##### DockerApplication

```java
package cn.merryyou.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created on 2018/3/20 0020.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
@SpringBootApplication
public class DockerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerApplication.class, args);
    }
}


```


##### docker.ftl
```html
<h1>Hello Spring Boot with docker-compose!</h1>
```


##### application.yml

```yaml
spring:
  freemarker:
    template-loader-path: classpath:/templates
    request-context-attribute: .ftl
```
##### Dockerfile

```yaml
FROM hub.c.163.com/wuxukun/maven-aliyun:3-jdk-8
ADD ["app-1.jar", "app.jar"]
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

##### nginx.conf

```yaml
upstream app {
    server app-1:8080;
    server app-2:8080;
    }

server {
    listen 80;
    charset utf-8;
    access_log off;

    location / {
        proxy_pass http://app;
        proxy_set_header Host $host:$server_port;
        proxy_set_header X-Forwarded-Host $server_name;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /static {
        access_log   off;
        expires      30d;

        alias /app/static;
    }
}
```

##### docker-compose.yaml

```yaml
version: '2'
services:
  nginx:
   container_name: some-nginx
   image: nginx:1.13
   restart: always
   ports:
   - 80:80
#   - 443:443
   volumes:
   - ./nginx/conf.d:/etc/nginx/conf.d
  app-1:
    restart: always
    build: ./app/docker-app-1
    working_dir: /app
    volumes:
      - ./app:/app
    expose:
      - "8080"
#    command: mvn clean spring-boot:run
    depends_on:
      - nginx
  app-2:
    restart: always
    build: ./app/docker-app-2
    working_dir: /app
    volumes:
      - ./app:/app
    expose:
      - "8080"
#    command: mvn clean spring-boot:run
    depends_on:
      - nginx
```

- `version: '2'`： 表示使用第二代语法来构建 `docker-compose.yaml` 文件；
- `services`: 用来表示 `compose` 需要启动的服务，我们可以看出此文件中有三个服务分别为：nginx、app-1、app-2。
- `container_name`:容器名称
- `ports`:表示对外开放的端口
- `restart: always`:表示如果服务启动不成功会一直尝试。
- `volumes`:加载本地目录下的配置文件到容器目标地址下


##### 测试

- 切换到`docker-compose-boot-nginx` 目录下执行 `docker-compose up`
- 访问`http://localhost`

##### 补充
1. 如果执行`docker-compose up`很慢，可在`host`文件中添加`127.0.0.1 localunixsocket.local`  [参考](https://github.com/docker/compose/issues/3419)
2. `docker logs -f -t --tail 行数 容器名`查看容器的实时日志

效果如下：
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker01.gif")

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/docker/docker02.gif")



## 代码下载 ##
从我的 github 中下载，[https://github.com/longfeizheng/docker-compose-boot-nginx](https://github.com/longfeizheng/docker-compose-boot-nginx)

---

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/wechat/xiaochengxu.png")

> 🙂🙂🙂关注微信小程序**java架构师历程**
上下班的路上无聊吗？还在看小说、新闻吗？不知道怎样提高自己的技术吗？来吧这里有你需要的java架构文章，1.5w+的java工程师都在看，你还在等什么？