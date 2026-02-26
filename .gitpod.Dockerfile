# Not actually used by the devcontainer, but it is used by gitpod
FROM gitpod/workspace-full

ARG JAVA_VERSION=17.0.7-ms
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && sdk install java $JAVA_VERSION && sdk use java $JAVA_VERSION"