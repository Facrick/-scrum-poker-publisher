$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Title,

        [Parameter(Mandatory = $true)]
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host $Title -ForegroundColor Green

    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $Title"
    }
}

Write-Host ""
Write-Host "=== Scrum Poker single JAR build ===" -ForegroundColor Cyan

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Frontend = Join-Path $Root "frontend"
$Backend = Join-Path $Root "backend"
$Static = Join-Path $Backend "src\main\resources\static"

Invoke-Step "1/4 Installing frontend dependencies..." {
    Push-Location $Frontend
    npm install
    Pop-Location
}

Invoke-Step "2/4 Building frontend..." {
    Push-Location $Frontend
    npm run build
    Pop-Location
}

$FrontendDist = Join-Path $Frontend "dist"
if (!(Test-Path $FrontendDist)) {
    throw "Frontend dist folder not found: $FrontendDist"
}

Write-Host ""
Write-Host "3/4 Copying frontend build into Spring Boot static resources..." -ForegroundColor Green
if (Test-Path $Static) {
    Remove-Item $Static -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Static | Out-Null
Copy-Item -Path (Join-Path $FrontendDist "*") -Destination $Static -Recurse -Force

Invoke-Step "4/4 Building backend jar..." {
    Push-Location $Backend
    mvn clean package -DskipTests
    Pop-Location
}

Write-Host ""
Write-Host "DONE:" -ForegroundColor Green
Write-Host "backend\target\scrum-poker-backend-1.0.0.jar"
Write-Host ""
Write-Host "Run:"
Write-Host "java -jar backend\target\scrum-poker-backend-1.0.0.jar"
Write-Host ""
