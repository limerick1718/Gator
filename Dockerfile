FROM runmymind/docker-android-sdk:alpine-standalone
RUN apk add --no-cache python3
ENV GatorRoot /usr/src/gator
COPY . $GatorRoot
WORKDIR $GatorRoot
RUN cd gator && ./gator b
# RUN cd AndroidBench && ./guiAnalysis.py runAll
