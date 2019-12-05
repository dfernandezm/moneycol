FROM docker.elastic.co/elasticsearch/elasticsearch:6.5.0
ARG environ=dev
ARG service_account_gcs_key=gcs-buckets-service-account.json
RUN echo "Environment is \${environ}\n Service account file needed is \${service_account_gcs_key}"
WORKDIR /usr/share/elasticsearch
RUN bin/elasticsearch-plugin install repository-gcs --batch
COPY --chown=elasticsearch:elasticsearch ${service_account_gcs_key} /usr/share/elasticsearch/config
RUN bin/elasticsearch-keystore create 
RUN bin/elasticsearch-keystore add-file gcs.client.moneycol_${environ}.credentials_file /usr/share/elasticsearch/config/${service_account_gcs_key}