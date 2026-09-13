# Stage 1: Build
FROM node:20-slim AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Vite writes the backend address into the bundle, so the image needs it at build time.
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Stage 2: Runtime
FROM nginx:stable-alpine
# Vite baut standardmäßig in den Ordner 'dist'
COPY --from=build /app/dist /usr/share/nginx/html
# Wichtig für React Router Support (verhindert 404 bei Refresh)
RUN printf "server { listen 80; location / { root /usr/share/nginx/html; index index.html; try_files \$uri \$uri/ /index.html; } }" > /etc/nginx/conf.d/default.conf
#Das ist der Port für das frontend von der Docker seite aus bzw von der Image seite aus. Bei uns lass ich es auf 5173 bevor noch irgendwelche Probleme auftreten
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]