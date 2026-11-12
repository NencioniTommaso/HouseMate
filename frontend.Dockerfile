FROM ghcr.io/cirruslabs/flutter:3.47.9 AS build
ARG API_BASE_URL=https://api.housemateapp.stream/api
WORKDIR /app/mobile
COPY mobile/ .
RUN flutter pub get && flutter build web --release --dart-define=API_BASE_URL=${API_BASE_URL}

FROM nginx:alpine
COPY --from=build /app/mobile/build/web /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
