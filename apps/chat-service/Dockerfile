# Stage 1: Build
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build

WORKDIR /app

# Restore dependencies first so this layer is cached until .sln/.csproj changes
COPY src/webApplication.sln ./src/
COPY src/webApplication.csproj ./src/
RUN dotnet restore src/webApplication.sln

# Copy remaining source and publish
COPY . .
WORKDIR /app/src
RUN dotnet publish webApplication.csproj -c Release -o /app/publish

# Stage 2: Runtime
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app

EXPOSE 29294
ENV ASPNETCORE_URLS=http://+:29294

COPY --from=build /app/publish .
COPY --from=build /app/src/migrations ./migrations

ENTRYPOINT ["dotnet", "webApplication.dll"]