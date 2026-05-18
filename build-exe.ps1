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
Write-Host "=== Scrum Poker EXE build ===" -ForegroundColor Cyan

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Frontend = Join-Path $Root "frontend"
$Backend = Join-Path $Root "backend"
$Static = Join-Path $Backend "src\main\resources\static"
$Dist = Join-Path $Root "dist"
$AppName = "ScrumPoker"
$JarName = "scrum-poker-backend-1.0.0.jar"

Write-Host ""
Write-Host "Checking Java..." -ForegroundColor Yellow
java -version

Write-Host ""
Write-Host "Checking Maven..." -ForegroundColor Yellow
mvn -version

Write-Host ""
Write-Host "Checking Node..." -ForegroundColor Yellow
node -v
npm -v

Write-Host ""
Write-Host "Checking jpackage..." -ForegroundColor Yellow
$jpackage = Get-Command jpackage -ErrorAction SilentlyContinue
if ($null -eq $jpackage) {
    throw "jpackage not found. Install JDK 17+ and make sure JAVA_HOME/bin is in PATH."
}

Invoke-Step "1/5 Installing frontend dependencies..." {
    Push-Location $Frontend
    npm install
    Pop-Location
}

Invoke-Step "2/5 Building frontend..." {
    Push-Location $Frontend
    npm run build
    Pop-Location
}

$FrontendDist = Join-Path $Frontend "dist"
if (!(Test-Path $FrontendDist)) {
    throw "Frontend dist folder not found: $FrontendDist"
}

Write-Host ""
Write-Host "3/5 Copying frontend build into Spring Boot static resources..." -ForegroundColor Green
if (Test-Path $Static) {
    Remove-Item $Static -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Static | Out-Null
Copy-Item -Path (Join-Path $FrontendDist "*") -Destination $Static -Recurse -Force

Invoke-Step "4/5 Building backend jar..." {
    Push-Location $Backend
    mvn clean package -DskipTests
    Pop-Location
}

$JarPath = Join-Path $Backend "target\$JarName"
if (!(Test-Path $JarPath)) {
    throw "Jar not found: $JarPath"
}

Write-Host ""
Write-Host "5/5 Creating portable Windows app image..." -ForegroundColor Green
if (Test-Path $Dist) {
    Remove-Item $Dist -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Dist | Out-Null

jpackage `
  --type app-image `
  --name $AppName `
  --app-version "1.0.0" `
  --input (Join-Path $Backend "target") `
  --main-jar $JarName `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --dest $Dist `
  --win-console

if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed"
}

$ExePath = Join-Path $Dist "$AppName\$AppName.exe"

Write-Host ""
Write-Host "DONE" -ForegroundColor Cyan
Write-Host "EXE:" -ForegroundColor Green
Write-Host $ExePath
Write-Host ""
Write-Host "Run it and open:" -ForegroundColor Green
Write-Host "http://localhost:8080"
Write-Host ""
