FROM node:20-alpine
WORKDIR /app
RUN npm install -g pnpm
COPY package*.json pnpm-lock.yaml ./
RUN pnpm install --prod
COPY . .
EXPOSE 10000
CMD ["node", "src/index.js"]