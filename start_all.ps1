# One-click Startup Script

# Clear BOM characters
Write-Host "Clearing BOM characters..." -ForegroundColor Cyan
python "D:\project\data1\fix_all_bom.py"

# Stop old services
Write-Host "Stopping old services..." -ForegroundColor Cyan
try {
    Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Get-Process -Name python -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  Old services stopped" -ForegroundColor Green
} catch {
    Write-Host "  Error stopping services: $($_.Exception.Message)" -ForegroundColor Red
}

# Start backend service
Write-Host "\nStarting backend service..." -ForegroundColor Cyan
Start-Process "cmd.exe" -ArgumentList "/c cd /d d:\project\data1 && title SpringBoot Backend && mvn spring-boot:run" -WindowStyle Normal

# Start FastAPI service
Write-Host "\nStarting FastAPI service..." -ForegroundColor Cyan
Start-Process "cmd.exe" -ArgumentList "/c cd /d d:\project\data1\fastapi && title FastAPI Service && python main.py" -WindowStyle Normal

# Start frontend service
Write-Host "\nStarting frontend service..." -ForegroundColor Cyan
Start-Process "cmd.exe" -ArgumentList "/c cd /d d:\project\data1\frontend && title Vue Frontend && npm run dev" -WindowStyle Normal

# Show service status
Write-Host "\n========================================" -ForegroundColor Cyan
Write-Host "Services started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Frontend: http://localhost:5173" -ForegroundColor White
Write-Host "  Backend: http://localhost:8080" -ForegroundColor White
Write-Host "  FastAPI: http://localhost:8000" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

# Wait for user input
Write-Host "\nPress Enter to exit..." -ForegroundColor Gray
Read-Host