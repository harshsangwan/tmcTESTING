# Multi-stage build for Angular app
FROM node:18-alpine as build

# Set working directory
WORKDIR /app

# Copy package files first for better layer caching
COPY package*.json ./

# Clear npm cache and install dependencies
# Using npm ci for production builds and --platform=linux/amd64 to avoid platform issues
RUN npm cache clean --force && \
    npm ci --omit=dev --platform=linux/amd64

# Copy source code
COPY . .

# Build the application (remove --prod flag as it's deprecated in Angular 19)
RUN npm run build

# Production stage with nginx
FROM nginx:1.25-alpine

# Remove default nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built application
COPY --from=build /app/dist/task-management/browser /usr/share/nginx/html

# Set proper permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Create required directories
RUN mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]