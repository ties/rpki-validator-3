FROM openjdk:8-jre-alpine 
ARG GENERIC_BUILD_ARCHIVE_URL=https://ftp.ripe.net/tools/rpki/validator3/prod/generic/rpki-validator-3-latest-dist.tar.gz

WORKDIR /opt/rpki-validator-3

# Webserver on 8080
EXPOSE 8080

# JVM memory settings
ENV JVM_XMS=""
ENV JVM_XMX=""
# Used by `rpki-validator-3.sh`
ENV CONFIG_DIR="/config"

RUN apk --no-cache add rsync \
    # Temporary file for archive & unpacking
    && export TMPDIR=$(mktemp -d) && export TMP_FILE=$(mktemp) \
    && wget ${GENERIC_BUILD_ARCHIVE_URL} -O ${TMP_FILE} \
    && mkdir -p /opt/rpki-validator-3 \
    && tar -zxf ${TMP_FILE} -C ${TMPDIR} \
    # Move files from the dir in the archive (like rpki-validator-3.1-2020.01.13.09.31.26) to target folder:
    && mv ${TMPDIR}/*/* /opt/rpki-validator-3 \
    # Clean up temporary directory
    && rm -rf ${TMPDIR} ${TMP_FILE} \
    # Ash instead of bash
    && sed -i 's/env bash/env ash/g'  /opt/rpki-validator-3/rpki-validator-3.sh \
    # UseContainerSupport: important
    && sed -i '/MEM_OPTIONS=/c\MEM_OPTIONS="-Xms$JVM_XMS -Xmx$JVM_XMX -XX:+ExitOnOutOfMemoryError -XX:+UseContainerSupport"' /opt/rpki-validator-3/rpki-validator-3.sh  \
    # Move about config and set defaults
    && mv /opt/rpki-validator-3/conf /config \
    && mv /opt/rpki-validator-3/preconfigured-tals/ /config \
    # Listen to 0.0.0.0 instead of just localhost
    && sed -i 's/server.address=localhost/server.address=0.0.0.0/g' ${CONFIG_DIR}/application.properties \
    # Load preconfigured-tals from /config
    && sed -i 's:rpki\.validator\.preconfigured\.trust\.anchors\.directory=./preconfigured-tals:rpki.validator.preconfigured.trust.anchors.directory=/config/preconfigured-tals:g' ${CONFIG_DIR}/application.properties \
    # Store data in /data
    && sed -i 's:rpki\.validator\.data\.path=.:rpki.validator.data.path=/data:g' ${CONFIG_DIR}/application.properties

EXPOSE 8080
CMD ["/opt/rpki-validator-3/rpki-validator-3.sh"]
VOLUME /config /data
