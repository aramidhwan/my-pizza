FROM wodby/openjdk:17-alpine

# "Build context dir (default:ducker build -t name .)" to "WORK DIR(default:/)"
# WORKDIR 은 COPY, RUN, ENTRYPOINT 등의 실행기준 DIR이 된다.
COPY build/libs/*SNAPSHOT.jar app.jar
EXPOSE 80

# 만약 alpine 버전을 베이스로 한 docker image를 빌드 중이라면,
# 아주 가벼운 친구이기 때문에 timezone 파일들 또한 존재하지 않는다.
# 그래서 alpine 버전에서 사용하는 package manager인 'apk'를 사용하여
# tzdata package 를 설치하고, 그 후에 링크 작업을 실시한다.
RUN apk add tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENTRYPOINT ["java","-Xmx200M","-Djava.net.preferIPv4Stack=true","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.profiles.active=k8s-dev"]
