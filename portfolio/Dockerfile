FROM clojure:temurin-21-bookworm AS base

RUN apt-get update && \
    apt-get install -y curl ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app

COPY package.json package-lock*.json shadow-cljs.edn ./
RUN npm install

FROM base AS build
COPY . .
CMD ["npx", "shadow-cljs", "release", "app"]

FROM nginx:alpine AS prod
COPY --from=build /app/resources/public /usr/share/nginx/html
ENV PORT=8080
EXPOSE $PORT
CMD ["/bin/sh", "-c", "sed -i 's/listen  80;/listen '\"$PORT\"';/' /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]

FROM base AS dev

COPY . .
EXPOSE 8280 9630 8777
CMD ["npx", "shadow-cljs", "watch", "app"]