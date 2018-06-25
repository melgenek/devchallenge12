FROM nginx:alpine

COPY nginx/static /usr/share/nginx/html/

COPY nginx/nginx.conf /etc/nginx/nginx.conf

ENV NGINX_PORT 8081
EXPOSE 8081

CMD ["nginx", "-g", "daemon off;"]
