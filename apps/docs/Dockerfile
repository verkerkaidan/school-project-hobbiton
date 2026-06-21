FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM node:20-bookworm AS pdf-builder

WORKDIR /app

COPY --from=builder /app /app
RUN npx playwright install --with-deps chromium
RUN npm run build:pdf

FROM node:20-alpine

RUN npm install -g serve

WORKDIR /app

COPY --from=pdf-builder /app/build ./build

EXPOSE 3001

CMD ["serve", "-s", "build", "-l", "3001"]
