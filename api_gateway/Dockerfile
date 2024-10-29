FROM openresty/openresty:alpine

# 로그 디렉터리와 파일 생성
RUN mkdir -p /var/log/nginx && \
    touch /var/log/nginx/proxy_debug.log && \
    chmod 644 /var/log/nginx/proxy_debug.log

COPY nginx.conf /usr/local/openresty/nginx/conf/nginx.conf

EXPOSE 80

CMD ["/usr/local/openresty/bin/openresty", "-g", "daemon off;"]