# use suggestions from team sikkerhet
#https://enturas.atlassian.net/wiki/spaces/TS/blog/2023/07/20/3814424621/Using+distroless+java+docker+images+from+Google
FROM gcr.io/distroless/java17-debian11:nonroot

EXPOSE 8080

COPY build/libs/osdm-converter-0.0.1-SNAPSHOT.jar /app/osdm-converter.jar

WORKDIR /app
CMD ["osdm-converter.jar"]
