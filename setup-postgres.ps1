#!/usr/bin/env pwsh
# =============================================================================
# setup-postgres.ps1  –  On-the-fly PostgreSQL setup for Inventory System
#
# This script:
#   1. Checks Docker Desktop is running
#   2. Starts the PostgreSQL container via docker-compose
#   3. Waits until PostgreSQL is ready to accept connections
#   4. Prints connection details
#
# Usage:  .\setup-postgres.ps1
# =============================================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Inventory System – PostgreSQL Setup" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# --- 1. Check Docker is available -------------------------------------------
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "  Found: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: Docker is not installed or not in PATH." -ForegroundColor Red
    Write-Host "  Download Docker Desktop from: https://www.docker.com/products/docker-desktop/" -ForegroundColor Red
    exit 1
}

# --- 2. Check Docker daemon is running --------------------------------------
Write-Host "Checking Docker daemon..." -ForegroundColor Yellow
try {
    docker info 2>&1 | Out-Null
    Write-Host "  Docker daemon is running." -ForegroundColor Green
} catch {
    Write-Host "  ERROR: Docker daemon is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}

# --- 3. Start PostgreSQL container ------------------------------------------
Write-Host ""
Write-Host "Starting PostgreSQL container..." -ForegroundColor Yellow
docker-compose up -d postgres

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ERROR: docker-compose failed." -ForegroundColor Red
    exit 1
}

# --- 4. Wait for PostgreSQL to be ready -------------------------------------
Write-Host ""
Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$ready = $false

while ($attempt -lt $maxAttempts -and -not $ready) {
    $attempt++
    Start-Sleep -Seconds 2
    $result = docker exec inventory-postgres pg_isready -U inventory_user -d inventory_db 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
    } else {
        Write-Host "  Attempt $attempt/$maxAttempts – waiting..." -ForegroundColor DarkGray
    }
}

if (-not $ready) {
    Write-Host "  ERROR: PostgreSQL did not become ready in time." -ForegroundColor Red
    exit 1
}

# --- 5. Done ----------------------------------------------------------------
Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "  PostgreSQL is ready!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Host     : localhost" -ForegroundColor White
Write-Host "  Port     : 5432" -ForegroundColor White
Write-Host "  Database : inventory_db" -ForegroundColor White
Write-Host "  Username : inventory_user" -ForegroundColor White
Write-Host "  Password : inventory_pass" -ForegroundColor White
Write-Host ""
Write-Host "  Run the app with profile: -Dspring.profiles.active=postgres" -ForegroundColor Cyan
Write-Host "  (Spring Boot will auto-start the container next time too)" -ForegroundColor DarkGray
Write-Host ""
