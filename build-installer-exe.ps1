$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=== Scrum Poker Windows installer EXE build ===" -ForegroundColor Cyan
Write-Host "This script requires WiX Toolset installed and available for jpackage." -ForegroundColor Yellow

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Backend = Join-Path $Root "backend"
$Dist = Join-Path $Root "dist-installer"
$JarName = "scrum-poker-backend-1.0.0.jar"

& (Join-Path $Root "build-jar.ps1")

if (Test-Path $Dist) {
    Remove-Item $Dist -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Dist | Out-Null

jpackage `
  --type exe `
  --name "ScrumPoker" `
  --app-version "1.0.0" `
  --input (Join-Path $Backend "target") `
  --main-jar $JarName `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --dest $Dist `
  --win-console

Write-Host ""
Write-Host "DONE. Installer is in:" -ForegroundColor Green
Write-Host $Dist
Write-Host ""
