FROM kibana:5.5.2

RUN sh -c 'echo elasticsearch.username: "gatekeeper" >> /etc/kibana/kibana.yml'
RUN sh -c 'echo elasticsearch.password: "keymaster" >> /etc/kibana/kibana.yml'
RUN sh -c 'echo elasticsearch.requestHeadersWhitelist: [cookie] >> /etc/kibana/kibana.yml'
