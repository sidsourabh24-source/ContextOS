# ContextOS Automatic Maven & Backend Runner
$mavenDir = "$PSScriptRoot\.maven"
$mvnCmd = "$mavenDir\apache-maven-3.9.6\bin\mvn.cmd"

if (-not (Test-Path $mvnCmd)) {
    Write-Host "======================================================" -ForegroundColor Cyan
    Write-Host " Downloading portable Apache Maven (one-time setup)..." -ForegroundColor Cyan
    Write-Host "======================================================" -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $mavenDir | Out-Null
    $zipPath = "$mavenDir\maven.zip"
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile $zipPath
    Write-Host "Extracting Maven..." -ForegroundColor Cyan
    Expand-Archive -Path $zipPath -DestinationPath $mavenDir -Force
    Remove-Item $zipPath -Force
    Write-Host "Maven installed successfully in $mavenDir" -ForegroundColor Green
}

Write-Host "Starting ContextOS Spring Boot Backend (Java 23)..." -ForegroundColor Green
& $mvnCmd spring-boot:run
