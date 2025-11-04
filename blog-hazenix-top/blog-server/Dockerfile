# 基础镜像
FROM openjdk:8-jre-slim
# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户（提升安全性）
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# 拷贝jar包
# COPY blog-server-1.0-SNAPSHOT.jar /app.jar
COPY blog-server-1.0-SNAPSHOT.jar /app.jar

# 入口
ENTRYPOINT ["java","-jar","/app.jar"]
