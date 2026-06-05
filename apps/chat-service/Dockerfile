# Stage 1: Build
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# Copy everything first (needed for multi-project solutions)
COPY . .

# Restore using the solution file
RUN dotnet restore webApplication.sln

# Publish
RUN dotnet publish webApplication.csproj -c Release -o /app/publish

# Stage 2: Runtime
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app

EXPOSE 29294
ENV ASPNETCORE_URLS=http://+:29294

COPY --from=build /app/publish .
COPY --from=build /src/migrations ./migrations

ENTRYPOINT ["dotnet", "webApplication.dll"]