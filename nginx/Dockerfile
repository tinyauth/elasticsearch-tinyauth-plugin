FROM openresty/openresty:alpine AS builder
RUN apk --no-cache add curl perl
RUN /usr/local/openresty/bin/opm get tinyauth/lua-resty-tinyauth

FROM openresty/openresty:alpine

RUN apk --no-cache add openssl

COPY --from=builder /usr/local/openresty/site /usr/local/openresty/site

COPY nginx.conf /etc/nginx/nginx.conf
COPY entrypoint.sh /docker-entrypoint
COPY *.js /srv/static/

ENTRYPOINT ["/docker-entrypoint"]
CMD ["nginx", "-c", "/etc/nginx/nginx.conf"]
