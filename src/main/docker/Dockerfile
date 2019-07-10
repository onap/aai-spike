FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

ARG MICRO_HOME=/opt/app/spike
ARG BIN_HOME=$MICRO_HOME/bin

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME
WORKDIR $MICRO_HOME
COPY /maven/spike .
RUN chmod 755 $BIN_HOME/* \
    && ln -snf /logs $MICRO_HOME/logs

EXPOSE 9518 9518

CMD ["/opt/app/spike/bin/start.sh"]
