FROM golang:1.23-alpine AS build
WORKDIR /app
COPY go.mod go.su[m] ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o /bin/logistica ./cmd/servidor

FROM gcr.io/distroless/static-debian12
COPY --from=build /bin/logistica /logistica
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["/logistica"]
