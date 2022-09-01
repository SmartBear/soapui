FROM ubuntu:20.04 AS basic

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		fontconfig libfreetype6 \
	; \
	rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/SmartBear

ARG SOAPUI_VERSION=5.6.1
ENV PROJECT_DIR ./project
ENV REPORTS_DIR ./reports
ENV MOUNTED_PROJECT_DIR ./project
ENV MOUNTED_EXT_DIR ./ext
ENV SOAPUI_DIR ./SoapUI-$SOAPUI_VERSION

COPY Files/SoapUI-x64-$SOAPUI_VERSION.sh ./

RUN chmod 755 ./SoapUI-x64-$SOAPUI_VERSION.sh && \
	./SoapUI-x64-$SOAPUI_VERSION.sh -q -dir $SOAPUI_DIR && \
	rm ./SoapUI-x64-$SOAPUI_VERSION.sh

ENV JAVA_HOME $SOAPUI_DIR/jre/bin
ENV PATH $PATH:$JAVA_HOME

COPY Files/RunProject.sh .
COPY Files/EntryPoint.sh .
RUN chmod 755 ./EntryPoint.sh
RUN chmod 755 ./RunProject.sh

ENTRYPOINT $WORKDIR/EntryPoint.sh

FROM ubuntu:20.04

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		fontconfig libfreetype6 \
	; \
	rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/SmartBear

ARG SOAPUI_VERSION=5.6.1
ENV PROJECT_DIR ./project
ENV REPORTS_DIR ./reports
ENV MOUNTED_PROJECT_DIR ./project
ENV MOUNTED_EXT_DIR ./ext
ENV SOAPUI_DIR ./SoapUI-$SOAPUI_VERSION

COPY --from=basic /usr/local/SmartBear .
#RUN ls -a -R /usr/local/SmartBear

ENTRYPOINT ./EntryPoint.sh
